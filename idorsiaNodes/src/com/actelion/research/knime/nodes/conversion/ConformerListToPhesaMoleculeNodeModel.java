package com.actelion.research.knime.nodes.conversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.chem.types.MolCell;
import org.knime.chem.types.MolCellFactory;
import org.knime.chem.types.SdfCell;
import org.knime.chem.types.SdfCellFactory;
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
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLConformerListDataValue;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.data.OCLPheSAMoleculeDataCell;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class ConformerListToPhesaMoleculeNodeModel extends NodeModel {
    
	
	private static final NodeLogger LOGGER = NodeLogger.getLogger(ConformerListToPhesaMoleculeNodeModel.class);
		
	private ConformerListToPhesaMoleculeNodeSettings m_Settings = new ConformerListToPhesaMoleculeNodeSettings();
	
	
    /**
     * Constructor for the node model.
     */
    protected ConformerListToPhesaMoleculeNodeModel() {
           
        // TODO: Specify the amount of input and output ports needed.
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        ColumnRearranger c = createColumnRearranger(inData[0].getDataTableSpec(),m_Settings.getInputColumnName());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], c, exec);            	
        return new BufferedDataTable[]{out};
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

		// find all conformer list columns:
		List<DataColumnSpec> columns = SpecHelper.getConformerListColumnSpecs(inSpecs[0]);
		System.out.println();
		
		if(columns.size()==0) {
			throw new InvalidSettingsException("No conformer list columns found.");
		}
		
		List<String> column_names = columns.stream().map( ci -> ci.getName() ).collect(Collectors.toList());
		
		String input = m_Settings.getInputColumnName();
		
		if(input!=null) {
			if(!column_names.contains(input)) {
				m_Settings.setInputColumnName(column_names.get(0));
			}
			else {
			}
		}
		else{
			m_Settings.setInputColumnName(column_names.get(0));
		}

				
		/*
		 * Similar to the return type of the execute method, we need to return an array
		 * of DataTableSpecs with the length of the number of outputs ports of the node
		 * (as specified in the constructor). The resulting table created in the execute
		 * methods must match the spec created in this method. As we will need to
		 * calculate the output table spec again in the execute method in order to
		 * create a new data container, we create a new method to do that.
		 */
		DataTableSpec inputTableSpec = inSpecs[0];				
		return new DataTableSpec[] { createColumnRearranger(inputTableSpec , m_Settings.getInputColumnName()).createSpec() };
	}

	/**
	 * Creates the output table spec from the input spec. For each double column in
	 * the input, one String column will be created containing the formatted double
	 * value as String.
	 * 
	 * @p)aram inputTableSpec
	 * @return
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inputTableSpec, String inputCol) {
					
        ColumnRearranger c = new ColumnRearranger(inputTableSpec);

        String newColumnName = "PheSA["+inputCol+"]";
        
        newColumnName = SpecHelper.getUniqueColumnName(inputTableSpec, newColumnName);

        DataColumnSpec newColSpec = SpecHelper.createPhesaMoleculeColumnSpec(newColumnName);
        final int orgMolColumnIdx = SpecHelper.getColumnIndex(inputTableSpec, m_Settings.getInputColumnName());

        // utility object that performs the calculation
        CellFactory factory = new SingleCellFactory(newColSpec) {
            public DataCell getCell(DataRow row) {
                DataCell orgCell = row.getCell(orgMolColumnIdx);

                if (orgCell.isMissing()) {
                    return DataType.getMissingCell();
                }                
                return new OCLPheSAMoleculeDataCell( (OCLConformerListDataValue) orgCell );                
            }
        };

        c.append(factory);
        
//        if (m_settings.isRemoveInputColumn()) {
//            c.replace(factory, orgMolColumnIdx);
//        } else {
//            c.append(factory);
//        }

        return c;	
    }
	
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_Settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_Settings.loadSettingsForModel(settings);    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
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

