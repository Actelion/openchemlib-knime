package com.actelion.research.knime.nodes.conversion;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;
import org.knime.chem.types.MolCell;
import org.knime.chem.types.MolCellFactory;
import org.knime.chem.types.SdfCell;
import org.knime.chem.types.SdfCellFactory;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.io.File;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

public class OCLToMolNodeModel extends NodeModel {
    public static final int IN_PORT = 0;

    //~--- fields -------------------------------------------------------------

    private OCLToMolNodeSettings m_settings = new OCLToMolNodeSettings();
    MolfileParser parser = new MolfileParser();

    //~--- constructors -------------------------------------------------------

    public OCLToMolNodeModel() {
        super(1, 1);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean hasMoleculeColumn = false;
        boolean inputColumnNameFound = false;
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        for (int a = 0; a < inputSpec.getNumColumns(); a++) {
            DataColumnSpec columnSpec = inputSpec.getColumnSpec(a);
            DataType dataType = columnSpec.getType();

            if (dataType.isCompatible(SdfValue.class) || dataType.isCompatible(SdfValue.class)) {
                hasMoleculeColumn = true;
            }

            if (dataType.isCompatible(OCLMoleculeDataValue.class) || dataType.isAdaptable(OCLMoleculeDataValue.class)) {
                hasMoleculeColumn = true;
            }

            if ((inputColumnName != null) && inputColumnName.equals(columnSpec.getName())) {
                inputColumnNameFound = true;
            }
        }

        if (!hasMoleculeColumn) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing a suitable molecule representation");
        }

        if (!inputColumnNameFound) {
            throw new InvalidSettingsException("Input table contains not the " + "column " + inputColumnName + ". Please "
                    + "(re-)configure the node.");
        }

        ColumnRearranger columnRearranger = createColumnRearranger(inputSpec);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        ColumnRearranger c = createColumnRearranger(inData[IN_PORT].getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[IN_PORT], c, exec);

        return new BufferedDataTable[]{out};
    }

    @Override
    protected void loadInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        m_settings.loadSettingsForModel(nodeSettingsRO);
    }

    @Override
    protected void reset() {
    }

    @Override
    protected void saveInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO nodeSettingsWO) {
        m_settings.saveSettings(nodeSettingsWO);
    }

    @Override
    protected void validateSettings(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        (new OCLToMolNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec in) {
        ColumnRearranger c = new ColumnRearranger(in);
        String newColumnName = m_settings.getNewColumnName();

        newColumnName = SpecHelper.getUniqueColumnName(in, newColumnName);

        DataColumnSpec newColSpec = createNewColumnSpec(newColumnName);
        final int orgMolColumnIdx = SpecHelper.getColumnIndex(in, m_settings.getInputColumnName());

        // utility object that performs the calculation
        CellFactory factory = new SingleCellFactory(newColSpec) {
            public DataCell getCell(DataRow row) {
                DataCell orgCell = row.getCell(orgMolColumnIdx);

                if (orgCell.isMissing()) {
                    return DataType.getMissingCell();
                }

                DataType type = orgCell.getType();
                OCLToMolNodeSettings.OutputType outputType = m_settings.getOutputType();

                try {
                    if (type.isCompatible(OCLMoleculeDataValue.class) || type.isAdaptable(OCLMoleculeDataValue.class)) {
                        OCLMoleculeDataValue value = ValueHelper.getOCLCell(orgCell);

                        switch (outputType) {
                            case SDF:
                                String sdfValue = value.getSdfValue();

                                return SdfCellFactory.create(sdfValue);

                            case MOL:
                                String molValue = value.getMolValue();

                                return MolCellFactory.create(molValue);

                            case SMILES:
                            	String smilesValue = value.getSmilesValue();
                            	
                            	return SmilesCellFactory.create(smilesValue);
                            
                            default:
                                return DataType.getMissingCell();
                        }
                    } else {
                        return DataType.getMissingCell();
                    }
                } catch (Throwable ignored) {
                    return DataType.getMissingCell();
                }
            }
        };

        if (m_settings.isRemoveInputColumn()) {
            c.replace(factory, orgMolColumnIdx);
        } else {
            c.append(factory);
        }

        return c;
    }

    private DataColumnSpec createNewColumnSpec(String newColumnName) {
        OCLToMolNodeSettings.OutputType outputType = m_settings.getOutputType();
        switch (outputType) {
            case MOL:
                return SpecHelper.createMoleculeColumnSpec(newColumnName, MolCell.TYPE);
            case SDF:
                return SpecHelper.createMoleculeColumnSpec(newColumnName, SdfCell.TYPE);
            case SMILES:
            	return SpecHelper.createMoleculeColumnSpec(newColumnName, SmilesCell.TYPE);
        }
        return null;
    }
}
