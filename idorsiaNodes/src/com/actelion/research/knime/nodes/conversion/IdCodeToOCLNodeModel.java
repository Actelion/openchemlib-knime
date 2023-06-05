package com.actelion.research.knime.nodes.conversion;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
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

public class IdCodeToOCLNodeModel extends NodeModel {
    public static final int IN_PORT = 0;

    private IdCodeToOCLNodeSettings m_settings = new IdCodeToOCLNodeSettings();
    MolfileParser parser = new MolfileParser();
    //~--- fields -------------------------------------------------------------


    //~--- constructors -------------------------------------------------------

    public IdCodeToOCLNodeModel() {
        super(1, 1);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean hasStringColumn = false;
        boolean inputColumnNameFound = false;
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        for (int a = 0; a < inputSpec.getNumColumns(); a++) {
            DataColumnSpec columnSpec = inputSpec.getColumnSpec(a);
            DataType dataType = columnSpec.getType();

            if (dataType.isCompatible(StringValue.class) || dataType.isAdaptable(StringValue.class)) {
                hasStringColumn = true;
            }

            if ((inputColumnName != null) && inputColumnName.equals(columnSpec.getName())) {
                inputColumnNameFound = true;
            }
        }

        if (!hasStringColumn) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing a suitable molecule representation");
        }

        if (!inputColumnNameFound) {
            throw new InvalidSettingsException("Input table contains not the " + "column " + inputColumnName
                    + ". Please " + "(re-)configure the node.");
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
        (new IdCodeToOCLNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec in) {
        ColumnRearranger c = new ColumnRearranger(in);
        String newColumnName = m_settings.getNewColumnName();
        newColumnName = SpecHelper.getUniqueColumnName(in, newColumnName);
        DataColumnSpec newColSpec = SpecHelper.createMoleculeColumnSpec(newColumnName);
        final int orgMolColumnIdx = SpecHelper.getColumnIndex(in, m_settings.getInputColumnName());

        // utility object that performs the calculation
        CellFactory factory = new SingleCellFactory(newColSpec) {
            public DataCell getCell(DataRow row) {
                DataCell orgCell = row.getCell(orgMolColumnIdx);

                if (orgCell.isMissing()) {
                    return DataType.getMissingCell();
                }

                DataType type = orgCell.getType();
                StereoMolecule molecule = null;
                try {
                    if (type.isCompatible(StringValue.class) || type.isAdaptable(StringValue.class)) {
                        StringValue value = ValueHelper.getStringCell(orgCell);
                        String stringValue = value.getStringValue();
                        IDCodeParser parser = new IDCodeParser();
                        molecule = new StereoMolecule();
                        parser.parse(molecule, stringValue);
                    }
                } catch (Throwable ignored) {
                }
                if (molecule == null)
                    return DataType.getMissingCell();
                return new OCLMoleculeDataCell(molecule);
            }
        };

        if (m_settings.isRemoveInputColumn()) {
            c.replace(factory, orgMolColumnIdx);
        } else {
            c.append(factory);
        }

        return c;
    }


}
