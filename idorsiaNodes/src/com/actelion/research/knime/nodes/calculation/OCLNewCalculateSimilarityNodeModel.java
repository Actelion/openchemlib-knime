package com.actelion.research.knime.nodes.calculation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;

/**
 * <code>NodeModel</code> for the "OCLNewCalculateSimilarity" node.
 *
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewCalculateSimilarityNodeModel extends SimilarityNodeModel {
	
	private static final int IN_PORT = 0;

    //~--- fields -------------------------------------------------------------

    private final OCLNewCalculateSimilarityNodeSettings m_settings = new OCLNewCalculateSimilarityNodeSettings();
    
    /**
     * Constructor for the node model.
     */
    protected OCLNewCalculateSimilarityNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	BufferedDataTable inputTable = inData[0];
        //final int moleculeColumnIdx = SpecHelper.getColumnIndex(inputTable.getDataTableSpec(), m_settings.getInputColumnName());
    	final int descriptorColumnIdx = SpecHelper.getColumnIndex(inputTable.getDataTableSpec(), m_settings.getInputColumnName());
        StereoMolecule queryMolecule = new StereoMolecule();
        IDCodeParser icp = new IDCodeParser();
        icp.parse( queryMolecule,m_settings.getFragment());
        queryMolecule.ensureHelperArrays(StereoMolecule.cHelperCIP);
        DescriptorInfo descriptorInfo = m_settings.getDescriptorInfo();

        List<Object> queryDescriptors = new ArrayList<>();
        Object queryDescriptor = DescriptorHelpers.calculateDescriptor(queryMolecule, descriptorInfo);
        queryDescriptors.add(queryDescriptor);

        Map<String, Double[]> similarities = runSimilarityAnalysis2(inputTable, descriptorColumnIdx, descriptorInfo, queryDescriptors, exec);
        ColumnRearranger analysisColumnRearranger = createColumnRearranger(inputTable.getDataTableSpec(), m_settings.getOutputColumnName(), false, similarities);
        BufferedDataTable analysisDataTable = exec.createColumnRearrangeTable(inData[IN_PORT], analysisColumnRearranger, exec);
        return new BufferedDataTable[]{analysisDataTable};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

    	//boolean hasMoleculeColumn = false;
    	boolean inputColumnNameFound = false;
       
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        List<DataColumnSpec> dscSpecs = SpecHelper.getDescriptorColumnSpecsNew(inputSpec);
        if (dscSpecs.isEmpty()) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing descriptors");
        }
        
        // check if we find the column from the settings:
        for (DataColumnSpec dscSpec : dscSpecs) {
            if (Objects.equals(inputColumnName, dscSpec.getName())) {
                inputColumnNameFound = true;
            }
        }

        if (!inputColumnNameFound) {
            throw new InvalidSettingsException("Input table does not contain a column named " + inputColumnName + ". Please "
                    + "(re-)configure the node.");
        }

        DescriptorInfo descriptor = m_settings.getDescriptorInfo();
        if (descriptor == null) {
            throw new InvalidSettingsException("No descriptor info found");
        }


        ColumnRearranger columnRearranger = createColumnRearranger(inputSpec, m_settings.getOutputColumnName(), false);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	 m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_settings.loadSettingsForModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

