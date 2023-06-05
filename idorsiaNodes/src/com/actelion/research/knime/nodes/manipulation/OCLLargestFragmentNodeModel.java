package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.utils.SpecHelper;
import org.knime.chem.types.SdfValue;
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

public class OCLLargestFragmentNodeModel extends NodeModel {
    public static final int IN_PORT = 0;
    static final String CFGKEY_INPUT_COLUMN = "inputColumn";
    static final String CFGKEY_OUTPUT_COLUMN_NAME = "outputColumnName";
    static final String CFGKEY_REMOVE_INPUT = "removeInputColumn";
    static final String DEFAULT_INPUT_COLUMN = "";
    static final String DEFAULT_OUTPUT_COLUMN_NAME = "Largest Fragment";
    static final boolean DEFAULT_REMOVE_INPUT = false;
    private OCLLargestFragmentNodeSettings m_settings = new OCLLargestFragmentNodeSettings();

    //~--- fields -------------------------------------------------------------


    //~--- constructors -------------------------------------------------------

    public OCLLargestFragmentNodeModel() {
        super(1, 1);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean hasMoleculeColumn = false;
        boolean inputColumnNameFound = false;
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String        inputColumnName      = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        for (int a = 0; a < inputSpec.getNumColumns(); a++) {
            DataColumnSpec columnSpec = inputSpec.getColumnSpec(a);
            DataType dataType = columnSpec.getType();

            if (dataType.isCompatible(OCLMoleculeDataValue.class) || dataType.isCompatible(SdfValue.class)) {
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
    protected void loadInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {}

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        m_settings.loadSettingsForModel(nodeSettingsRO);
    }
    @Override
    protected void reset() {

    }

    @Override
    protected void saveInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {}

    @Override
    protected void saveSettingsTo(NodeSettingsWO nodeSettingsWO) {
        m_settings.saveSettings(nodeSettingsWO);
    }

    @Override
    protected void validateSettings(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        (new OCLLargestFragmentNodeSettings()).loadSettingsForModel(nodeSettingsRO);
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

                StereoMolecule molecule = ((OCLMoleculeDataValue) orgCell).getMolecule().getCompactCopy();

                molecule.stripSmallFragments();

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
