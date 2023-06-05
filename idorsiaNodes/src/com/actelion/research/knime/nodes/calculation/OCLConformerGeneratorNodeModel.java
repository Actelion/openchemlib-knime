package com.actelion.research.knime.nodes.calculation;

import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
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
import org.knime.core.util.ThreadPool;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.computation.ComputationPool;
import com.actelion.research.knime.computation.ConformerCalculator;
import com.actelion.research.knime.computation.ConformerGeneratorActionProvider;
import com.actelion.research.knime.computation.ConformerGeneratorTask;
import com.actelion.research.knime.computation.SimilarityCalculator;
import com.actelion.research.knime.computation.SimilarityCalculatorActionProvider;
import com.actelion.research.knime.data.DescriptorRow;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLConformerListDataValue;
import com.actelion.research.knime.data.OCLDescriptorDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;


/**
 * 
 * @author Idorsia Pharmaceuticals Ltd
 */
public class OCLConformerGeneratorNodeModel extends NodeModel {
    
    /**
	 * The logger is used to print info/warning/error messages to the KNIME console
	 * and to the KNIME log file. Retrieve it via 'NodeLogger.getLogger' providing
	 * the class of this node model.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(OCLConformerGeneratorNodeModel.class);


	
	private OCLConformerGeneratorNodeSettings m_Settings = new OCLConformerGeneratorNodeSettings();

	/**
	 * Constructor for the node model.
	 */
	protected OCLConformerGeneratorNodeModel() {
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
		 * The functionality of the node is implemented in the execute method. This
		 * implementation will format each double column of the input table using a user
		 * provided format String. The output will be one String column for each double
		 * column of the input containing the formatted number from the input table. For
		 * simplicity, all other columns are ignored in this example.
		 * 
		 * Some example log output. This will be printed to the KNIME console and KNIME
		 * log.
		 */
		LOGGER.info("OCLConformerGeneratorNodeModel::execute(..) entered");

        CloseableRowIterator rowIterator = null;
        //List<List<>>> descriptorRows = new ArrayList<>();

        BufferedDataTable dataTable = inData[0];

        int col_idx = dataTable.getSpec().findColumnIndex(m_Settings.getInputColumnName());
        
        List<MoleculeRow> molecules = new ArrayList<>();
        
        try {
            rowIterator = dataTable.iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();
                DataCell cell = row.getCell(col_idx);
                
                if(!cell.isMissing()) {
	                //StereoMolecule smi      = ((OCLMoleculeDataCell)cell).getMolecule();
	                String         smidcode = ((OCLMoleculeDataCell)cell).getIdCode();               
	                molecules.add( new MoleculeRow(row.getKey(),smidcode) );
                }
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }

        
        Map<String, List<StereoMolecule>> computedConformers = new ConcurrentHashMap<>();
        
//        ConformerGeneratorTask cgt = new ConformerGeneratorTask( molecules, m_Settings , 0, molecules.size(),computedConformers);
//        ComputationPool pool = ComputationPool.getInstance();
//        pool.execute(cgt);
//        while (!cgt.isDone() && !cgt.isCancelled()) {
//            try {
//                exec.checkCanceled();
//            } catch (CanceledExecutionException e) {
//                cgt.requestStop();
//                throw e;
//            }
//        }
	
        ThreadPool pool = org.knime.core.util.ThreadPool.currentPool().createSubPool();
        //int min_elements_per_job = ()
        int chunk_size = (int) Math.max( 1 , Math.min(  molecules.size()*0.01 ,64) );
        //System.out.println("chunk size: "+chunk_size);
        //LOG.info("chunk size: "+chunk_size);
        ConformerGeneratorActionProvider taskprovider = new ConformerGeneratorActionProvider(molecules, m_Settings, chunk_size, computedConformers);
        //pool.invoke(fb);
        taskprovider.getTasks().forEach( ti -> pool.enqueue(ti) );
        
        try {
			pool.waitForTermination();			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pool.shutdown();
        
        ColumnRearranger columnRearranger = createColumnRearranger(inData[0].getDataTableSpec(), inData[0].getDataTableSpec().getColumnSpec(m_Settings.getInputColumnName()), computedConformers);
        BufferedDataTable outputTable = exec.createColumnRearrangeTable(inData[0], columnRearranger, exec);
        return new BufferedDataTable[]{outputTable};
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

		// if no input colum name is set, take the first molecular column..
		if(m_Settings.getInputColumnName()==null) {
			//System.out.println("::configure() find molecular column..");
			LOGGER.debug("::configure() find molecular column..");
			List<DataColumnSpec> mol_cols = SpecHelper.getMoleculeColumnSpecs(inSpecs[0]);
			if(mol_cols.size()==0) {
				throw new InvalidSettingsException("No molecular column found..");
			}
			m_Settings.setInputColumnName(mol_cols.get(0).getName());
		}
		
		//System.out.println("Try to configure. InputColumnName="+m_Settings.getInputColumnName());
		LOGGER.debug("Try to configure. InputColumnName="+m_Settings.getInputColumnName());
		
		if(! SpecHelper.isMoleculeSpec(inSpecs[0].getColumnSpec(m_Settings.getInputColumnName() ) ) ) {
			throw new InvalidSettingsException("input column must be Molecule!");
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
		ColumnRearranger colRearranger = createColumnRearranger( inputTableSpec , inputTableSpec.getColumnSpec(m_Settings.getInputColumnName()) , null );
		return new DataTableSpec[] { colRearranger.createSpec() };
	}
	
	/**
	 * Creates the output table spec from the input spec. For each double column in
	 * the input, one String column will be created containing the formatted double
	 * value as String.
	 * 
	 * @param inputTableSpec
	 * @return
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inputTableSpec, DataColumnSpec parentColumnSpec, Map<String,List<StereoMolecule>> conformers) throws InvalidSettingsException {
		String columnTitle = "Conformers["+parentColumnSpec.getName()+"]";
		
        ColumnRearranger c = new ColumnRearranger(inputTableSpec);
        DataColumnSpec newSpec = null;

    	newSpec = SpecHelper.createConformerSetColumnSpec( columnTitle , parentColumnSpec);
        
        CellFactory factory = null;
        if(conformers==null) {
        	//System.out.println("::createColumnRearranger(..) -> conformers are null, create dummy factory");
        	LOGGER.debug("::createColumnRearranger(..) -> conformers are null, create dummy factory");
	        factory = new ConformerListCellFactory(newSpec, 0, null, null);
	        c.append(factory);
        }
        else {        	
        	System.out.println("::createColumnRearranger(..) -> received conformers, create cell factory");
        	//LOGGER.debug("::createColumnRearranger(..) -> conformers are null, create dummy factory");
        	// bring into right format:
        	Map<String,String> new_idcodes = new HashMap<>();
        	Map<String,List<String>> new_coordinates = new HashMap<>();
        	
        	for(String si : conformers.keySet()) {
        		List<StereoMolecule> ci_list = conformers.get(si);
        		if(ci_list.size()>0) {   
        			try {
        				ci_list.get(0).validate();
        			}
        			catch(Exception ex) {
        				System.out.println("Validating generated conformer failed.. :(");
        				ex.printStackTrace();
        			}
        			Canonizer canonizer = new Canonizer(ci_list.get(0),Canonizer.COORDS_ARE_3D);
        			//new_idcodes.put(si,canonizer.getIDCode());
        			new_idcodes.put(si,si);
        			List<String> coordinates = new ArrayList<>();
        			for(StereoMolecule sci : ci_list) {
        				Canonizer canonizer_i = new Canonizer(sci,Canonizer.COORDS_ARE_3D);
        				coordinates.add(canonizer_i.getEncodedCoordinates());
        				String idc_i = canonizer_i.getIDCode();
        				if(!idc_i.equals(si)) {
        					System.out.println("idcode problem: main= "+si+" conf_i= "+idc_i);
        				}
        			}
        			new_coordinates.put(si,coordinates);
        		}
        		else {
        			// that's ok, if entry is missing the CellFactory handles it anyway..
        		}
        	}
        	
        	int source_col = inputTableSpec.findColumnIndex(parentColumnSpec.getName());
        	
        	factory = new ConformerListCellFactory(newSpec, source_col, new_idcodes, new_coordinates);
        	c.append(factory);
        }
        return c;
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
		//m_numberFormatSettings.validateSettings(settings);
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
	
	
	class ConformerListCellFactory extends SingleCellFactory {

		private int srcCol;
		
		private Map<String,String> allIDCodes;
		
		private Map<String,List<String>> allCoordinates;
		
		public ConformerListCellFactory(DataColumnSpec columnSpec, int srcCol, Map<String,String> allIDCodes , Map<String,List<String>> coordinates) {
            super(columnSpec);
            this.srcCol = srcCol;
            this.allIDCodes = allIDCodes;
            this.allCoordinates = coordinates; 
		}
		
		@Override
		public DataCell getCell(DataRow row) {
			DataCell ci = row.getCell( srcCol );
			if(ci.isMissing()) {
				return DataType.getMissingCell();
			}
			String mol = ((OCLMoleculeDataCell) ci ).getIdCode();
			
			if(this.allIDCodes==null || this.allCoordinates==null) {
				//System.out.println("We are in dummy factory, cannot create any cells..");
				LOGGER.debug("::createColumnRearranger(..) -> conformers are null, create dummy factory");
				return DataType.getMissingCell();
			}
			
			//String mol_new = allIDCodes.get(mol);
			String mol_new = mol;
			
//			if(mol_new==null) {
//				System.out.println("WARNING! Conformation generation failed! IDCode = "+mol);
//				OCLConformerListDataCell cell = new OCLConformerListDataCell(mol,new String[0]);
//				return cell;
//			}
			
//			if(!mol.equals(mol_new)) {
//				System.out.println("WARNING! IDCode changed during conformer generation! "+mol+" vs. "+mol_new);
//			}
			
			List<String> coordinates = allCoordinates.get(mol);
			String[] a_coordinates   = new String[0];
			if(coordinates!=null) {
				 a_coordinates = coordinates.stream().toArray(String[]::new);
			}
			else {
				// something did not work..
			}
			
			OCLConformerListDataCell cell = new OCLConformerListDataCell(mol_new,a_coordinates);
			return cell;
		}
		
	}
	
}

