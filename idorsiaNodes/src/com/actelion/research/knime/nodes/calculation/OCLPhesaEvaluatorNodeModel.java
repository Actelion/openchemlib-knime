package com.actelion.research.knime.nodes.calculation;

import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.knime.core.data.RowIterator;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.doublevector.DoubleVectorCellFactory;
import org.knime.core.data.vector.doublevector.DoubleVectorValue;
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
import org.knime.core.util.Pair;
import org.knime.core.util.ThreadPool;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.computation.ComputationPool;
import com.actelion.research.knime.computation.ForceFieldMinimizationActionProvider;
import com.actelion.research.knime.computation.PhesaDefaultEvaluatorTask;
import com.actelion.research.knime.computation.ForceFieldMinimizationTask.MinimizationResult;
import com.actelion.research.knime.computation.PhesaDefaultEvaluatorActionProvider;
import com.actelion.research.knime.data.ConformerListRow;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLConformerListDataValue;
import com.actelion.research.knime.data.OCLMoleculeCellFactory;
import com.actelion.research.knime.data.OCLPheSAMoleculeDataCell;
import com.actelion.research.knime.data.PhesaMoleculeRow;
import com.actelion.research.knime.utils.SpecHelper;


/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */


public class OCLPhesaEvaluatorNodeModel extends NodeModel {
    
	private static final NodeLogger LOGGER = NodeLogger.getLogger(OCLPhesaEvaluatorNodeModel.class);


	OCLPhesaEvaluatorNodeSettings m_Settings = new OCLPhesaEvaluatorNodeSettings();
	
	/**
	 * Constructor for the node model.
	 */
	protected OCLPhesaEvaluatorNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(2, 1);
	}


	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		LOGGER.info("Phesa Eval");
		
		
		
        int col_idx_r = inData[0].getSpec().findColumnIndex(m_Settings.getInputColNameRef());
        int col_idx_q = inData[1].getSpec().findColumnIndex(m_Settings.getInputColNameQuery());
        
        List<PhesaMoleculeRow> molecules_r = new ArrayList<>();
        List<PhesaMoleculeRow> molecules_q = new ArrayList<>();
        
        CloseableRowIterator rowIterator = null;
        
        try {
            rowIterator = inData[0].iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();
                DataCell cell = row.getCell(col_idx_r);                
                molecules_r.add( new PhesaMoleculeRow(row.getKey(), ((OCLPheSAMoleculeDataCell) cell).getPhesaMolecule() ) );
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }
        
        try {
            rowIterator = inData[1].iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();
                DataCell cell = row.getCell(col_idx_q);                
                molecules_q.add( new PhesaMoleculeRow(row.getKey(), ((OCLPheSAMoleculeDataCell) cell).getPhesaMolecule() ) );
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }
                                     
        System.out.println("start phesa eval: mq="+molecules_q.size()+" mr="+molecules_r.size());
        Map<Pair<String,String>, Double> similarity_out = new ConcurrentHashMap<>();
        Map<Pair<String,String>, StereoMolecule[]> alignments_out = new ConcurrentHashMap<>();
        
//		PhesaDefaultEvaluatorTask phesaComputeTask = new PhesaDefaultEvaluatorTask(molecules_r, molecules_q, m_Settings, 0, molecules_q.size(), similarity_out, alignments_out);
//        ComputationPool pool = ComputationPool.getInstance();
//        pool.execute(phesaComputeTask);
//        while (!phesaComputeTask.isDone() && !phesaComputeTask.isCancelled()) {
//            try {
//                exec.checkCanceled();
//            } catch (CanceledExecutionException e) {
//            	phesaComputeTask.requestStop();
//                throw e;
//            }
//        }
        
        ThreadPool pool = org.knime.core.util.ThreadPool.currentPool().createSubPool();
        //int min_elements_per_job = ()
        int chunk_size = (int) Math.max( 1 , Math.min(  molecules_r.size()*0.01 ,256) );
        //System.out.println("chunk size: "+chunk_size);
        //LOG.info("chunk size: "+chunk_size);
        PhesaDefaultEvaluatorActionProvider taskprovider = new PhesaDefaultEvaluatorActionProvider(molecules_r,molecules_q, m_Settings, chunk_size, similarity_out, alignments_out);
        //pool.invoke(fb);
        taskprovider.getTasks().forEach( ti -> pool.enqueue(ti) );
        
        try {
			pool.waitForTermination();			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pool.shutdown();
					
		ColumnRearranger columnRearranger = createColumnRearranger(inData[0].getDataTableSpec(),inData[1].getDataTableSpec(),molecules_r,similarity_out,alignments_out);	
		BufferedDataTable outputTable     = exec.createColumnRearrangeTable(inData[1], columnRearranger, exec);
        return new BufferedDataTable[]{outputTable};			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		// if no input colum name is set, take the first phesa column..
		
		boolean has_ref   = false;
		boolean has_query = false;
		
		if(m_Settings.getInputColNameRef()!=null) {
			if(inSpecs[0].getColumnSpec(m_Settings.getInputColNameRef()) != null) {
				has_ref = true;				
			}
		}
		
		if(m_Settings.getInputColNameRef()!=null) {
			if(inSpecs[1].getColumnSpec(m_Settings.getInputColNameQuery()) != null) {
				has_query = true;				
			}
		}
						
		if( !has_ref ) {
			//System.out.println("::configure() find phesa column..");
			LOGGER.debug("::configure() find phesa column..");
			List<DataColumnSpec> mol_cols = SpecHelper.getPhesaMoleculeColumnSpecs(inSpecs[0]);
			if(mol_cols.size()==0) {
				throw new InvalidSettingsException("No PhesaMolecule column found (Reference)");
			}
			m_Settings.setInputColNameRef(mol_cols.get(0).getName());
		}
		
		
		if( !has_query) {
			//System.out.println("::configure() find phesa column..");
			LOGGER.debug("::configure() find phesa column..");
			List<DataColumnSpec> mol_cols = SpecHelper.getPhesaMoleculeColumnSpecs(inSpecs[1]);
			if(mol_cols.size()==0) {
				throw new InvalidSettingsException("No PhesaMolecule column found..");
			}
			else {
				if(mol_cols.size()==0) {
					throw new InvalidSettingsException("No PhesaMolecule column found (Library)");
				}
				else {
					m_Settings.setInputColNameQuery(mol_cols.get(0).getName());
				}
			}
		}			
		
		if(! SpecHelper.isPhesaMoleculeSpec(inSpecs[0].getColumnSpec(m_Settings.getInputColNameRef() ) ) ) {
			throw new InvalidSettingsException("Input Column Ref must be PhesaMolecule!");
		}
		if(! SpecHelper.isPhesaMoleculeSpec(inSpecs[1].getColumnSpec(m_Settings.getInputColNameQuery() ) ) ) {
			throw new InvalidSettingsException("Input Column Query must be PhesaMolecule!");
		}
							
		ColumnRearranger c = createColumnRearranger(inSpecs[0],inSpecs[1],null,null,null);		
		return new DataTableSpec[] { c.createSpec() };
	}

	/**
	 * @return
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inputTableSpecR, DataTableSpec inputTableSpecQ,  List<PhesaMoleculeRow> reference_rows , Map<Pair<String,String>,Double> allSimilarities , Map<Pair<String,String>,StereoMolecule[]> allMolecules ) {
		
        ColumnRearranger c = new ColumnRearranger(inputTableSpecQ);

        String newColumnName = "Similarity[Phesa]["+m_Settings.getInputColNameRef()+"x" + m_Settings.getInputColNameQuery() + "]";        
        newColumnName = SpecHelper.getUniqueColumnName(inputTableSpecQ, newColumnName);
        DataColumnSpec newColSpec = SpecHelper.createDoubleVectorColumnSpec(newColumnName);
        
        
        final int orgMolColumnIdx_R = SpecHelper.getColumnIndex(inputTableSpecR, m_Settings.getInputColNameRef());
        final int orgMolColumnIdx_Q = SpecHelper.getColumnIndex(inputTableSpecQ, m_Settings.getInputColNameQuery());

        
        //PhesaDefaultEvaluatorTask compute = new PhesaDefaultEvaluatorTask(phesaRowsRef, phesaRowsQuery, settings, q_start, q_length, similarity_out)
              
        
        CellFactory f = new PhesaEvaluationCellFactory( newColSpec ,orgMolColumnIdx_R,orgMolColumnIdx_Q , reference_rows, allSimilarities);       
        c.append(f);        
        
        // store optimal alignment molecules..
        if(this.m_Settings.isOutputMolRef()) {
        	
            String newColumnName_mr = "RefMol_Aligned[Phesa]["+m_Settings.getInputColNameRef()+"x" + m_Settings.getInputColNameQuery() + "]";        
            newColumnName_mr = SpecHelper.getUniqueColumnName(inputTableSpecQ, newColumnName_mr);
            
        	DataColumnSpec newColSpec_mr = null;
        	newColSpec_mr = SpecHelper.createConformerSetColumnSpec(newColumnName_mr,null);
//            try {
//            newColSpec_mr = SpecHelper.createConformerSetColumnSpec(newColumnName_mr,null);
//            }
//            catch(InvalidSettingsException ex) {
//            	ex.printStackTrace();
//            }
        	        
            CellFactory f_mr = null;
            if(reference_rows==null) {
            	f_mr = new PhesaAlignmentCellFactory(newColSpec_mr, true, null, null);
            }
            else {
            	f_mr = new PhesaAlignmentCellFactory(newColSpec_mr, true, reference_rows.get(0), allMolecules);            	
            }
        	c.append(f_mr);
        }
        
        if(this.m_Settings.isOutputMolQuery()) {
        	
            String newColumnName_mq = "LibraryMol_Aligned[Phesa]["+m_Settings.getInputColNameRef()+"x" + m_Settings.getInputColNameQuery() + "]";        
            newColumnName_mq = SpecHelper.getUniqueColumnName(inputTableSpecQ, newColumnName_mq);
            
            DataColumnSpec newColSpec_mq = null; //SpecHelper.createMoleculeColumnSpec(newColumnName_mq);
            newColSpec_mq = SpecHelper.createConformerSetColumnSpec(newColumnName_mq,null);
//            try {
//            	newColSpec_mq = SpecHelper.createConformerSetColumnSpec(newColumnName_mq,null);
//            }
//            catch(InvalidSettingsException ex) {
//            	ex.printStackTrace();
//            }
        	        	                        
            CellFactory f_mq = null;
            if(reference_rows==null) {
            	f_mq = new PhesaAlignmentCellFactory(newColSpec_mq, false, null, null);
            }
            else {	            
	        	f_mq = new PhesaAlignmentCellFactory(newColSpec_mq, false, reference_rows.get(0), allMolecules);	        	
            }
            c.append(f_mq);
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
		m_Settings.loadSettingsForModel(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		
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
	
	
	
	
	class PhesaEvaluationCellFactory extends SingleCellFactory {

		private int srcCol_r;
		private int srcCol_q;
		
		private List<PhesaMoleculeRow> referenceRows = null;		
		private Map<Pair<String,String>,Double> allSimilarities = null;
		
		
		public PhesaEvaluationCellFactory(DataColumnSpec columnSpec, int srcCol_R, int srcCol_Q , List<PhesaMoleculeRow> reference_rows , Map<Pair<String,String>,Double> allSimilarities) {
            super(columnSpec);
            this.srcCol_r = srcCol_R;
            this.srcCol_q = srcCol_Q;
            this.referenceRows = reference_rows;
            this.allSimilarities = allSimilarities;                       
		}
		
		@Override
		public DataCell getCell(DataRow row) {
			
			if(this.allSimilarities==null ) {
				LOGGER.debug("We are in dummy factory, cannot create any cells..");
				return DataType.getMissingCell();
			}
							
			double vector[] = new double[this.referenceRows.size()];
			int vi = 0;
			for( PhesaMoleculeRow ri : this.referenceRows ) {
				//System.out.println("find pair: "+ri.getRowKey()+" / "+row.getKey().getString());
				Double vdi = this.allSimilarities.get( new Pair<String,String>(ri.getRowKey(),row.getKey().getString()) );
				if(vdi==null) {
					vdi = Double.NaN;					
				}
				else {
					vector[vi] = vdi.doubleValue();
				}
				vi++;
			}
						
			DataCell cell = DoubleVectorCellFactory.createCell(vector);			
			return cell;
		}		
	}	
		
	/**
	 * Creates column storing the optimal alignment of the molecule in the supplied
	 * "reference_row", and the query molecule of the given row.
	 * Depending on the parameter showRefMolecule it stores the optimally 
	 * aligned StereoMolecule of the reference or query compound.  
	 * 
	 * @author liphath1
	 *
	 */
	class PhesaAlignmentCellFactory extends SingleCellFactory {
		
		private PhesaMoleculeRow referenceRow = null;		
		private Map<Pair<String,String>,StereoMolecule[]> allMolecules = null;
				
		private boolean showRefMolecule;
		
		public PhesaAlignmentCellFactory(DataColumnSpec columnSpec, boolean showRefMolecule , PhesaMoleculeRow reference_row , Map<Pair<String,String>,StereoMolecule[]> allMolecules ) {
            super(columnSpec);
            this.referenceRow     = reference_row;
            this.showRefMolecule  = showRefMolecule;
            this.allMolecules     = allMolecules;                       
		}
		
		@Override
		public DataCell getCell(DataRow row) {
			
			if( this.referenceRow == null || this.allMolecules==null ) {
				LOGGER.debug("We are in dummy factory, cannot create any cells..");
				return DataType.getMissingCell();
			}
			
			String query_key = row.getKey().getString();
			String ref_key   = this.referenceRow.getRowKey();
			
			StereoMolecule alignment[] = this.allMolecules.get( new Pair<String,String>(ref_key,query_key) );
			if(alignment==null) {
				LOGGER.warn("Missing alignment for molecule pair a= "+ref_key +" b= "+query_key);
				alignment = new StereoMolecule[] { new StereoMolecule() , new StereoMolecule() };
			}
			
			StereoMolecule molecule = null;
			
			if(this.showRefMolecule) {
				//molecule = alignment[0];
				molecule = alignment[1];
			}
			else {
				//molecule = alignment[0];
				molecule = alignment[0];
			}		
			
			if(true && !this.showRefMolecule) {
				System.out.println("LibMol..\n");
				for(int zi=0;zi<molecule.getAllAtoms();zi++) {
					System.out.println("MolType: "+molecule.getAtomicNo(zi));
					System.out.println("ref_mol coords: "+zi+ " -> " +
							molecule.getCoordinates(zi).x + " , "+ 
							molecule.getCoordinates(zi).y + " , "+ 
							molecule.getCoordinates(zi).z);
				}
			}
			
			DataCell cell = new OCLConformerListDataCell(molecule.getIDCode(), new StereoMolecule[] {molecule} );			
			return cell;
		}					
	}

	
	
}

