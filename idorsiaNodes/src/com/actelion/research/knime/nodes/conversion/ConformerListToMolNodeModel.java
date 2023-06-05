package com.actelion.research.knime.nodes.conversion;

import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.chem.types.MolCell;
import org.knime.chem.types.MolCellFactory;
import org.knime.chem.types.SdfCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;


/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */


public class ConformerListToMolNodeModel extends NodeModel {
    
    /**
	 * The logger is used to print info/warning/error messages to the KNIME console
	 * and to the KNIME log file. Retrieve it via 'NodeLogger.getLogger' providing
	 * the class of this node model.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(ConformerListToMolNodeModel.class);


	private ConformerListToMolNodeSettings m_Settings = new ConformerListToMolNodeSettings();
	
	/**
	 * Constructor for the node model.
	 */
	protected ConformerListToMolNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(1, 1);
	}


	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		/*
		 * The input data table to work with. The "inData" array will contain as many
		 * input tables as specified in the constructor. In this case it can only be one
		 * (see constructor).
		 */
		BufferedDataTable inputTable = inData[0];

		DataTableSpec outputSpec = createOutputSpec(inputTable.getDataTableSpec() , m_Settings.getInputColumnName() );

		/*
		 * The execution context provides storage capacity, in this case a
		 * data container to which we will add rows sequentially. Note, this container
		 * can handle arbitrary big data tables, it will buffer to disc if necessary.
		 * The execution context is provided as an argument to the execute method by the
		 * framework. Have a look at the methods of the "exec". There is a lot of
		 * functionality to create and change data tables.
		 */
		BufferedDataContainer container = exec.createDataContainer(outputSpec);

		/*
		 * Get the row iterator over the input table which returns each row one-by-one
		 * from the input table.
		 */
		CloseableRowIterator rowIterator = inputTable.iterator();

		// find index of input column:
		int idxIn = inputTable.getDataTableSpec().findColumnIndex(m_Settings.getInputColumnName()); 
		
		/*
		 * A counter for how many rows have already been processed. This is used to
		 * calculate the progress of the node, which is displayed as a loading bar under
		 * the node icon.
		 */
		int currentRowCounter = 0;		
		int currentOutputRowCounter = 0;
		
		IDCodeParser p = new IDCodeParser();
		
		// Iterate over the rows of the input table.
		while (rowIterator.hasNext()) {
			DataRow currentRow = rowIterator.next();
			
			OCLConformerListDataCell conformer_cell = (OCLConformerListDataCell) currentRow.getCell(idxIn);
			
			// extract the idcode:
			String idcode = conformer_cell.getMolecule2D().getIDCode();
//			StereoMolecule smi = new StereoMolecule();
//			p.parse(smi, idcode);
			
			int nc = conformer_cell.size();
			for( int zi=0;zi<nc;zi++) {
				// for each conformer we add a row..
				List<DataCell> cells = new ArrayList<>();				
				
				// 1. add the molecule (2d)
				//StereoMolecule cscmi = new StereoMolecule(smi);
				cells.add( ValueHelper.createMoleculeDataCell(idcode) );
				
				// 2. add the molecule (3d)
				//cells.add( new OCLConformerListDataCell( new StereoMolecule[] { conformer_cell.getMolecule3D(zi) } );
				OCLConformerListDataCell extracted_cell = new OCLConformerListDataCell( conformer_cell.getMolecule3D(zi) );
				cells.add( extracted_cell );
				
				// 3. add the conformer index
				cells.add( ValueHelper.createIntDataCell(zi) );
				
				// 4. add the exported molecule:
				DataCell cell_transformed = DataType.getMissingCell();
				
				if( m_Settings.getOutputType().toString().equals("Sdf") ) {
					cell_transformed = MolCellFactory.create( extracted_cell.getSdfValue() );	
					
				}
				else if ( m_Settings.getOutputType().toString().equals("Mol") ) {
					cell_transformed = MolCellFactory.create( extracted_cell.getMolValue() );
				}
				
				cells.add(cell_transformed);
				
				// all cells added.
				
				// Add the new row to the output data container
				DataRow row = new DefaultRow( RowKey.createRowKey(currentOutputRowCounter) , cells);
				container.addRowToTable(row);

				// We finished processing one row, hence increase the counter
				currentOutputRowCounter++;

				/*
				 * Here we check if a user triggered a cancel of the node. If so, this call will
				 * throw an exception and the execution will stop. This should be done
				 * frequently during execution, e.g. after the processing of one row if
				 * possible.
				 */
				exec.checkCanceled();
								
			}			
			
			currentRowCounter++;
			/*
			 * Calculate the percentage of execution progress and inform the
			 * ExecutionMonitor. Additionally, we can set a message what the node is
			 * currently doing (the message will be displayed as a tooltip when hovering
			 * over the progress bar of the node). This is especially useful to inform the
			 * user about the execution status for long running nodes.
			 */
			exec.setProgress(currentRowCounter / (double) inputTable.size(), "Exporting row " + currentRowCounter);
		}

		/*
		 * Once we are done, we close the container and return its table. Here we need
		 * to return as many tables as we specified in the constructor. This node has
		 * one output, hence return one table (wrapped in an array of tables).
		 */
		container.close();
		BufferedDataTable out = container.getTable();
		return new BufferedDataTable[] { out };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		/*
		 * Check if the node is executable, e.g. all required user settings are
		 * available and valid, or the incoming types are feasible for the node to
		 * execute. In case the node can execute in its current configuration with the
		 * current input, calculate and return the table spec that would result of the
		 * execution of this node. I.e. this method precalculates the table spec of the
		 * output table.
		 * 
		 * Here we perform a sanity check on the entered number format String. In this
		 * case we just try to apply it to some dummy double number. If there is a
		 * problem, an IllegalFormatException will be thrown. We catch the exception and
		 * wrap it in a InvalidSettingsException with an informative message for the
		 * user. The message should make clear what the problem is and how it can be
		 * fixed if this information is available. This will be displayed in the KNIME
		 * console and printed to the KNIME log. The log will also contain the stack
		 * trace.
		 */
		
		// find all conformer list columns:
		List<DataColumnSpec> columns = SpecHelper.getConformerListColumnSpecs(inSpecs[0]);
		//System.out.println();
		
		if(columns.size()==0) {
			throw new InvalidSettingsException("No conformer list columns found.");
		}
		
		List<String> column_names = columns.stream().map( ci -> ci.getName() ).collect(Collectors.toList());
		
		String input = m_Settings.getInputColumnName();
		if(input!=null) {
			if(!column_names.contains(input)) {
				m_Settings.setInputColumnName(column_names.get(0));
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
				
		return new DataTableSpec[] { createOutputSpec(inputTableSpec , m_Settings.getInputColumnName()) };
	}

	/**
	 * Creates the output table spec from the input spec. For each double column in
	 * the input, one String column will be created containing the formatted double
	 * value as String.
	 * 
	 * @p)aram inputTableSpec
	 * @return
	 */
	private DataTableSpec createOutputSpec(DataTableSpec inputTableSpec, String inputCol) {
					
		List<DataColumnSpec> newColumnSpecs = new ArrayList<>();

		newColumnSpecs.add( SpecHelper.createMoleculeColumnSpec("Molecule["+inputCol+"]", OCLMoleculeDataCell.TYPE ) );

		newColumnSpecs.add( SpecHelper.createMoleculeColumnSpec("Molecule3D["+inputCol+"]", OCLConformerListDataCell.TYPE ) );
		
		newColumnSpecs.add( SpecHelper.createIntColumnSpec("ConformerIndex["+inputCol+"]") );
		
		if( m_Settings.getOutputType().toString().equals("Sdf") ) {
			newColumnSpecs.add( SpecHelper.createMoleculeColumnSpec("ConformerAsSdf["+inputCol+"]", SdfCell.TYPE ) );			
		}
		else if(m_Settings.getOutputType().toString().equals("Mol")) {
			newColumnSpecs.add( SpecHelper.createMoleculeColumnSpec("ConformerAsMol["+inputCol+"]", MolCell.TYPE ) );
		}
								
		// Create and return a new DataTableSpec from the list of DataColumnSpecs.
		DataColumnSpec[] newColumnSpecsArray = newColumnSpecs.toArray(new DataColumnSpec[newColumnSpecs.size()]);
		return new DataTableSpec(newColumnSpecsArray);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		/*
		 * Save user settings to the NodeSettings object. SettingsModels already know how to
		 * save them self to a NodeSettings object by calling the below method. In general,
		 * the NodeSettings object is just a key-value store and has methods to write
		 * all common data types. Hence, you can easily write your settings manually.
		 * See the methods of the NodeSettingsWO.
		 */
		m_Settings.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		/*
		 * Load (valid) settings from the NodeSettings object. It can be safely assumed that
		 * the settings are validated by the method below.
		 * 
		 * The SettingsModel will handle the loading. After this call, the current value
		 * (from the view) can be retrieved from the settings model.
		 */
		m_Settings.loadSettingsForModel(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		/*
		 * Check if the settings could be applied to our model e.g. if the user provided
		 * format String is empty. In this case we do not need to check as this is
		 * already handled in the dialog. Do not actually set any values of any member
		 * variables.
		 */
		m_Settings.validateSettings(settings);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		/*
		 * Advanced method, usually left empty. Everything that is
		 * handed to the output ports is loaded automatically (data returned by the execute
		 * method, models loaded in loadModelContent, and user settings set through
		 * loadSettingsFrom - is all taken care of). Only load the internals
		 * that need to be restored (e.g. data used by the views).
		 */
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		/*
		 * Advanced method, usually left empty. Everything
		 * written to the output ports is saved automatically (data returned by the execute
		 * method, models saved in the saveModelContent, and user settings saved through
		 * saveSettingsTo - is all taken care of). Save only the internals
		 * that need to be preserved (e.g. data used by the views).
		 */
	}

	@Override
	protected void reset() {
		/*
		 * Code executed on a reset of the node. Models built during execute are cleared
		 * and the data handled in loadInternals/saveInternals will be erased.
		 */
	}
}

