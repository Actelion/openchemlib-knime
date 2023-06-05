package com.actelion.research.knime.nodes.calculation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.OCLDescriptorDataValue;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;

/**
 * <code>NodeModel</code> for the "OCLNewCalculateSimilarityList" node.
 *
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewCalculateSimilarityListNodeModel extends SimilarityNodeModel {
    private static final int IN_PORT = 0;
    private static final int COMPARE_PORT = 1;

    //~--- fields -------------------------------------------------------------

    private final OCLNewCalculateSimilarityListNodeSettings m_settings = new OCLNewCalculateSimilarityListNodeSettings();

    //~--- constructors -------------------------------------------------------

    public OCLNewCalculateSimilarityListNodeModel() {
        super(2, 1);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean inputColumnNameFound = false;
        boolean compareToColumnNameFound = false;
        boolean descriptorColumnNameFound = false;

        DataTableSpec inputSpec = inSpecs[IN_PORT];
        DataTableSpec compareToSpec = inSpecs[COMPARE_PORT];

        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        String compareToColumnName = (m_settings == null)
                ? null
                : m_settings.getCompareToColumnName();

        List<DataColumnSpec> moleculeColumnSpecs = SpecHelper.getDescriptorColumnSpecs(inputSpec);
        if (moleculeColumnSpecs.isEmpty()) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing a suitable molecule representation");
        }
        for (DataColumnSpec moleculeColumnSpec : moleculeColumnSpecs) {
            if (Objects.equals(inputColumnName, moleculeColumnSpec.getName())) {
                inputColumnNameFound = true;
            }
        }

        if (!inputColumnNameFound) {
            throw new InvalidSettingsException("Input table does not contain a column named " + inputColumnName + ". Please "
                    + "(re-)configure the node.");
        }

        List<DataColumnSpec> compareToColumnSpecs = SpecHelper.getDescriptorColumnSpecs(compareToSpec);
        if (compareToColumnSpecs.isEmpty()) {

            throw new InvalidSettingsException("Compare table must contain at "
                    + "least one column containing a suitable molecule representation");

        }
        for (DataColumnSpec moleculeColumnSpec : compareToColumnSpecs) {
            if (Objects.equals(compareToColumnName, moleculeColumnSpec.getName())) {
                compareToColumnNameFound = true;
            }
        }

        if (!compareToColumnNameFound) {
            throw new InvalidSettingsException("Compare table does not contain a column named " + inputColumnName + ". Please "
                    + "(re-)configure the node.");
        }

        DescriptorInfo descriptor = m_settings.getDescriptor();
        if (descriptor == null) {
            throw new InvalidSettingsException("No descriptor for similarity calculation selected");
        }


        ColumnRearranger columnRearranger = createColumnRearranger(inputSpec, m_settings.getColumnName(), true);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputTable = inData[0];
        BufferedDataTable compareToTable = inData[1];
        final int moleculeColumnIdx = SpecHelper.getColumnIndex(inputTable.getDataTableSpec(), m_settings.getInputColumnName());
        final int compareToColumnIdx = SpecHelper.getColumnIndex(compareToTable.getDataTableSpec(), m_settings.getCompareToColumnName());
        DescriptorInfo descriptorInfo = m_settings.getDescriptor();

        List<Object> queryDescriptors = new ArrayList<>();
        CloseableRowIterator iterator = compareToTable.iterator();

        while (iterator.hasNext()) {
            exec.checkCanceled();
            DataRow row = iterator.next();
            DataCell cell = row.getCell(compareToColumnIdx);
            if(!cell.isMissing()) {
	            //StereoMolecule queryMolecule = ValueHelper.getMolecule(cell);
	            //Object queryDescriptor = DescriptorHelpers.calculateDescriptor(queryMolecule, descriptorInfo);
	            OCLDescriptorDataValue descriptorCell = (OCLDescriptorDataValue) row.getCell(compareToColumnIdx);
	            Object queryDescriptor = descriptorCell.getDescriptor();
	            queryDescriptors.add(queryDescriptor);
            }
        }

        Map<String, Double[]> similarities = runSimilarityAnalysis2(inputTable, moleculeColumnIdx, descriptorInfo, queryDescriptors, exec);
        ColumnRearranger analysisColumnRearranger = createColumnRearranger(inputTable.getDataTableSpec(), m_settings.getColumnName(), true, similarities);
        BufferedDataTable analysisDataTable = exec.createColumnRearrangeTable(inData[IN_PORT], analysisColumnRearranger, exec);
        return new BufferedDataTable[]{analysisDataTable};
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
        (new OCLCalculateSimilarityListNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }
}