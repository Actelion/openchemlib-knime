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

import org.knime.base.node.io.filereader.DataCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.doublevector.DoubleVectorCellFactory;
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
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.computation.ComputationPool;
import com.actelion.research.knime.computation.ConformerGeneratorActionProvider;
import com.actelion.research.knime.computation.ConformerGeneratorTask;
import com.actelion.research.knime.computation.ForceFieldMinimizationActionProvider;
import com.actelion.research.knime.computation.ForceFieldMinimizationTask;
import com.actelion.research.knime.computation.ForceFieldMinimizationTask.MinimizationResult;
import com.actelion.research.knime.data.ConformerListRow;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.nodes.calculation.OCLConformerGeneratorNodeModel.ConformerListCellFactory;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;


/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 */

public class OCLForceFieldMinimizationNodeModel extends NodeModel {
    
	private static final NodeLogger LOGGER = NodeLogger.getLogger(OCLForceFieldMinimizationNodeModel.class);

	private OCLForceFieldMinimizationNodeSettings m_Settings = new OCLForceFieldMinimizationNodeSettings();

	/**
	 * Constructor for the node model.
	 */
	protected OCLForceFieldMinimizationNodeModel() {
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

		
		LOGGER.info("OCLForceFieldMinimizationNodeModel::execute(..) entered");

        CloseableRowIterator rowIterator = null;
        //List<List<>>> descriptorRows = new ArrayList<>();

        BufferedDataTable dataTable = inData[0];

        int col_idx = dataTable.getSpec().findColumnIndex(m_Settings.getInputColumnName());        
        List<ConformerListRow> molecules = new ArrayList<>();
        
        try {
            rowIterator = dataTable.iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();
                DataCell cell = row.getCell(col_idx);

                //StereoMolecule smi      = ((OCLMoleculeDataCell)cell).getMolecule();
                String         smidcode             = ((OCLConformerListDataCell)cell).getIDCode();
                String[]       smidcode_coordinates = ((OCLConformerListDataCell)cell).getCoordinateIDCodes();
                
                molecules.add( new ConformerListRow(row.getKey(),smidcode,smidcode_coordinates) );
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }

        
        Map<String, List<StereoMolecule>> computedConformers = new ConcurrentHashMap<>();
        Map<String, List<MinimizationResult>> optimResult    = new ConcurrentHashMap<>();
        
//        ForceFieldMinimizationTask cgt = new ForceFieldMinimizationTask( molecules, m_Settings , 0, molecules.size(),computedConformers,optimResult);
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
        int chunk_size = (int) Math.max( 1 , Math.min(  molecules.size()*0.01 ,32) );
        //System.out.println("chunk size: "+chunk_size);
        //LOG.info("chunk size: "+chunk_size);
        ForceFieldMinimizationActionProvider taskprovider = new ForceFieldMinimizationActionProvider(molecules, m_Settings, chunk_size, computedConformers,optimResult);
        //pool.invoke(fb);
        taskprovider.getTasks().forEach( ti -> pool.enqueue(ti) );
        
        try {
			pool.waitForTermination();			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pool.shutdown();
	
        ColumnRearranger columnRearranger = createColumnRearranger(inData[0].getDataTableSpec(), inData[0].getDataTableSpec().getColumnSpec(m_Settings.getInputColumnName()), computedConformers, optimResult);
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
		 */
		// if no input colum name is set, take the first molecular column..
		
		boolean tryToFindConformerColumn = false;
		if(m_Settings.getInputColumnName()==null) {
			tryToFindConformerColumn = true;
		}
		else {
			if(m_Settings.getInputColumnName().isEmpty()) {
				tryToFindConformerColumn = true;
			}
		}
		
		if(tryToFindConformerColumn) {
			//System.out.println("::configure() find conformer set column..");
			LOGGER.debug("::configure() find conformer set column..");
			List<DataColumnSpec> mol_cols = SpecHelper.getConformerListColumnSpecs(inSpecs[0]);
			if(mol_cols.size()==0) {
				throw new InvalidSettingsException("No conformer column found..");
			}
			m_Settings.setInputColumnName(mol_cols.get(0).getName());
		}
		
		//System.out.println("Try to configure. InputColumnName="+m_Settings.getInputColumnName());
		LOGGER.debug("Try to configure. InputColumnName="+m_Settings.getInputColumnName());
		
		if(! SpecHelper.isConformerListSpec(inSpecs[0].getColumnSpec(m_Settings.getInputColumnName() ) ) ) {
			throw new InvalidSettingsException("input column must be Conformer List!");
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
		
		ColumnRearranger rearranger = createColumnRearranger(inputTableSpec,inputTableSpec.getColumnSpec(m_Settings.getInputColumnName()) , null , null);
		
		return new DataTableSpec[] { rearranger.createSpec() };
	}


	/**
	 * Creates the column rearranger.
	 * NOTE: The created column rearranger depends on the m_Settings object. If overwriteInput is false,
	 * a new column of type conformer set will be added.
	 * 
	 * @param inputTableSpec
	 * @param parentColumnSpec is the column spec of the conformer set column to be minimized
	 * @param m_proto: StereoMolecule that has to be constructed from the IDCode of the ConformerList.
	 * @return
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inputTableSpec, DataColumnSpec inputColumnSpec, Map<String,List<StereoMolecule>> minimized_conformers, Map<String,List<ForceFieldMinimizationTask.MinimizationResult>> optim_results) throws InvalidSettingsException {
		String columnTitle_pre = "OptimizedConformers["+inputColumnSpec.getName()+"]";		
		String columnTitle = SpecHelper.getUniqueColumnName(inputTableSpec,columnTitle_pre);

		
        ColumnRearranger c = new ColumnRearranger(inputTableSpec);

        // remove in case of overwrite
    	if(m_Settings.isOverwriteInput()) {
    		c.remove( inputColumnSpec.getName() );
    	}
        
        
        DataColumnSpec newSpec = null;
        
        // determine new column title:        
        if(m_Settings.isOverwriteInput()) {
        	columnTitle = inputColumnSpec.getName();
        }

    	newSpec = SpecHelper.createConformerSetColumnSpec( columnTitle , inputColumnSpec);
        
        CellFactory factory = null;
        if(minimized_conformers==null) {
        	//System.out.println("::createColumnRearranger(..) -> conformers are null, create dummy factory");
        	LOGGER.debug("::createColumnRearranger(..) -> conformers are null, create dummy factory");
	        factory = new MinimizedConformerListCellFactory(newSpec, 0, null, null);
	        c.append(factory);
	        

	        if(m_Settings.isOutEnergy()) {
	    		String st_pre = "OptimizedConformers_EnergyFinal["+inputColumnSpec.getName()+"]";        		
	    		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);
	
	    		DataColumnSpec  dcs  = SpecHelper.createDoubleVectorColumnSpec(st);
	    		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), null, null, OptimResultCellFactory.MODE_OUT_ENERGY_FINAL);
	    		c.append(f);
	        }
	        
	        if(m_Settings.isOutEnergyAtStart()) {
	    		String st_pre = "OptimizedConformers_EnergyAtStart["+inputColumnSpec.getName()+"]";        		
	    		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);
	
	    		DataColumnSpec  dcs  = SpecHelper.createDoubleVectorColumnSpec(st);
	    		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), null, null, OptimResultCellFactory.MODE_OUT_ENERGY_START);
	    		c.append(f);	        	
	        }
	        
	        if(m_Settings.isOutOptIter()) {
	    		String st_pre = "OptimizedConformers_Opt_Iter["+inputColumnSpec.getName()+"]";        		
	    		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);
	
	    		DataColumnSpec  dcs  = SpecHelper.createDoubleVectorColumnSpec(st);
	    		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), null, null, OptimResultCellFactory.MODE_OUT_OPT_ITER);
	    		c.append(f);	        	
	        }
	        
	        if(m_Settings.isOutOptResult()) {
	    		String st_pre = "OptimizedConformers_Opt_Result["+inputColumnSpec.getName()+"]";        		
	    		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);
	
	    		DataColumnSpec  dcs  = SpecHelper.createStringColumnSpec(st);
	    		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), null, null, OptimResultCellFactory.MODE_OUT_OPT_RESULT);
	    		c.append(f);	        	
	        }
	        
	        
	        
        }
        else {        	
        	//System.out.println("::createColumnRearranger(..) -> received conformers, create cell factory");
        	LOGGER.debug("::createColumnRearranger(..) -> received conformers, create cell factory");
        	// bring into right format:
        	Map<String,String> new_idcodes = new HashMap<>();
        	Map<String,List<String>> new_coordinates = new HashMap<>();
        	    
        	IDCodeParser   p       = new IDCodeParser();
        	StereoMolecule m_proto = new StereoMolecule();
        	
        	for(String si : minimized_conformers.keySet()) {        		
        		p.parse(m_proto, si);        		
        		List<StereoMolecule> ci_list = minimized_conformers.get(si);        		
        		if(ci_list.size()>0) {
        			Canonizer canonizer = new Canonizer( m_proto , Canonizer.COORDS_ARE_3D);
        			// weird.. this one has sometimes different stereo chemistry: like flipped abs. up/down bonds.. can/should this be?)
        			//new_idcodes.put(si,canonizer.getIDCode());
        			new_idcodes.put(si,si);
        			//System.out.println("Two values should be equal: " + si +" =?= " + canonizer.getIDCode());
        			LOGGER.debug("Two values should be equal: " + si +" =?= " + canonizer.getIDCode());
        			List<String> coordinates = new ArrayList<>();
        			for(StereoMolecule sci : ci_list) {
        				Canonizer canonizer_i = new Canonizer(sci,Canonizer.COORDS_ARE_3D);
        				coordinates.add(canonizer_i.getEncodedCoordinates(true));
        			}
        			new_coordinates.put(si,coordinates);
        		}
        		else {
        			// that's ok, if entry is missing the CellFactory handles it anyway..
        		}
        	}
        	
        	factory = new MinimizedConformerListCellFactory(newSpec, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), new_idcodes, new_coordinates);        	        	        	        	        	     
        	c.append(factory);
        	
        	
        	// add additional output columns..        	
        	//if(m_Settings.isOutEnergy()) {
        	if(m_Settings.isOutEnergy()) {
        		String st_pre = "OptimizedConformers_EnergyFinal["+inputColumnSpec.getName()+"]";        		
        		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);

        		DataColumnSpec  dcs  = SpecHelper.createDoubleVectorColumnSpec(st);
        		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), new_idcodes, optim_results, OptimResultCellFactory.MODE_OUT_ENERGY_FINAL);
        		c.append(f);
        	}
        	
	        if(m_Settings.isOutEnergyAtStart()) {
	    		String st_pre = "OptimizedConformers_EnergyAtStart["+inputColumnSpec.getName()+"]";        		
	    		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);
	
	    		DataColumnSpec  dcs  = SpecHelper.createDoubleVectorColumnSpec(st);
	    		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), new_idcodes, optim_results, OptimResultCellFactory.MODE_OUT_ENERGY_START);
	    		c.append(f);	        	
	        }

	        if(m_Settings.isOutOptIter()) {
	    		String st_pre = "OptimizedConformers_Opt_Iter["+inputColumnSpec.getName()+"]";        		
	    		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);
	
	    		DataColumnSpec  dcs  = SpecHelper.createDoubleVectorColumnSpec(st);
	    		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), new_idcodes, optim_results, OptimResultCellFactory.MODE_OUT_OPT_ITER);
	    		c.append(f);	        	
	        }
	        
	        if(m_Settings.isOutOptResult()) {
	    		String st_pre = "OptimizedConformers_Opt_Result["+inputColumnSpec.getName()+"]";        		
	    		String st     = SpecHelper.getUniqueColumnName(inputTableSpec,st_pre);
	
	    		DataColumnSpec  dcs  = SpecHelper.createStringColumnSpec(st);
	    		CellFactory f        = new OptimResultCellFactory(dcs, inputTableSpec.findColumnIndex(inputColumnSpec.getName()), new_idcodes, optim_results, OptimResultCellFactory.MODE_OUT_OPT_RESULT);
	    		c.append(f);	        	
	        }
	        	        	        	                	        	     
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
	
	
	
	class MinimizedConformerListCellFactory extends SingleCellFactory {

		
		private int srcCol;
		
		private Map<String,String> allIDCodes;
		
		private Map<String,List<String>> allCoordinates;	
		
		public MinimizedConformerListCellFactory(DataColumnSpec columnSpec, int srcCol, Map<String,String> allIDCodes , Map<String,List<String>> coordinates) {
            super(columnSpec);
            this.srcCol = srcCol;
            this.allIDCodes = allIDCodes;
            this.allCoordinates = coordinates;           
		}
		
		@Override
		public DataCell getCell(DataRow row) {
			//String mol = ((OCLMoleculeDataCell) row.getCell( srcCol ) ).getIdCode();
			String mol = ((OCLConformerListDataCell) row.getCell( srcCol ) ).getIDCode();
						
			//System.out.println("Create Cell for Mol: " +mol);
			LOGGER.debug("Create Cell for Mol: " +mol);
			
			if(this.allIDCodes==null || this.allCoordinates==null) {
				LOGGER.debug("We are in dummy factory, cannot create any cells..");
				return DataType.getMissingCell();
			}
			
			
//			String mol_new = allIDCodes.get(mol);			
//			if(mol_new==null) {
//				LOGGER.warn("WARNING! Optimization apparently failed! IDCode = "+mol);
//				OCLConformerListDataCell cell = new OCLConformerListDataCell(mol,new String[0]);
//				return cell;
//			}

			// IDCode of optimized molecule has to agree with non-optimized molecule, but NOT with the
			// IDCode for the ConformerList (as specific conformers may have more specific stereo chemistry
			// compared to the IDCode of the conformer set!)
//			if(!mol.equals(mol_new)) {
//				LOGGER.warn("WARNING! IDCode changed during optimization!");
//			}
			
			List<String> coordinates = allCoordinates.get(mol);
			String[] a_coordinates   = new String[0];
			if(coordinates!=null) {
				 a_coordinates = coordinates.stream().toArray(String[]::new);
			}
			else {
				// something did not work..
			}
			
			OCLConformerListDataCell cell = new OCLConformerListDataCell(mol,a_coordinates);
			return cell;
		}
		
	}	
	
	
	class OptimResultCellFactory extends SingleCellFactory {
		
	
		public static final int MODE_OUT_ENERGY_START = 1;
		
		public static final int MODE_OUT_ENERGY_FINAL = 2;
		
		public static final int MODE_OUT_OPT_ITER = 3;
		
		public static final int MODE_OUT_OPT_RESULT = 4;
		
		
		private int srcCol;
		
		private Map<String,String> allIDCodes;
		
		private Map<String,List<ForceFieldMinimizationTask.MinimizationResult>> allOptimResults;
		
		private int mode;
		
		public OptimResultCellFactory(DataColumnSpec columnSpec, int srcCol, Map<String,String> allIDCodes , Map<String,List<ForceFieldMinimizationTask.MinimizationResult>> optim_results, int mode) {
            super(columnSpec);
            this.srcCol = srcCol;
            this.allIDCodes = allIDCodes;
            this.allOptimResults = optim_results;
            
            this.mode = mode;
		}
		
		@Override
		public DataCell getCell(DataRow row) {
			
			if(this.allOptimResults==null) {
				return DataType.getMissingCell();
			}
			
			String mol = ((OCLConformerListDataCell) row.getCell( srcCol ) ).getIDCode();			
			
			switch (this.mode) {
			case MODE_OUT_ENERGY_START:
				double e_out_values[] = this.allOptimResults.get(mol).stream().mapToDouble( ri -> ri.energyBeforeOptim ).toArray();				
				return ValueHelper.createDoubleVectorDataCell(e_out_values);
				
			case MODE_OUT_ENERGY_FINAL:
				double ef_out_values[] = this.allOptimResults.get(mol).stream().mapToDouble( ri -> ri.energy ).toArray();				
				return ValueHelper.createDoubleVectorDataCell(ef_out_values);				
				
				
			case MODE_OUT_OPT_ITER:
				double it_out_values[] = this.allOptimResults.get(mol).stream().mapToDouble( ri -> ri.iterations ).toArray();
				return ValueHelper.createDoubleVectorDataCell(it_out_values);				
				
			case MODE_OUT_OPT_RESULT:
				StringBuilder sb = new StringBuilder();
				for( ForceFieldMinimizationTask.MinimizationResult mi : this.allOptimResults.get(mol)) {
					if(mi.errorMessage!=null) { 
						sb.append("ERR:"+mi.errorMessage);
					}
					else {
						sb.append(mi.result);
					}
					sb.append("\n");
				}
				return ValueHelper.createDataCell(sb.toString());
			}
						
			// 
			return DataType.getMissingCell();
		}		
	}
	
	
}

