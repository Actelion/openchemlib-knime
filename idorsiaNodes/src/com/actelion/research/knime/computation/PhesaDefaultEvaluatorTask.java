package com.actelion.research.knime.computation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.PheSAMolecule;
import com.actelion.research.knime.computation.ForceFieldMinimizationTask.MinimizationResult;
import com.actelion.research.knime.data.ConformerListRow;
import com.actelion.research.knime.data.DescriptorRow;
import com.actelion.research.knime.data.PhesaMoleculeRow;
import com.actelion.research.knime.nodes.calculation.OCLForceFieldMinimizationNodeSettings;
import com.actelion.research.knime.nodes.calculation.OCLPhesaEvaluatorNodeSettings;
import com.actelion.research.knime.utils.DescriptorHelpers;

//Author: tl

public class PhesaDefaultEvaluatorTask  extends StoppableRecursiveAction {
	
    private static NodeLogger LOG = NodeLogger.getLogger(PhesaDefaultEvaluatorTask.class);	
	
    private final List<PhesaMoleculeRow> phesaRowsRef;
    private final List<PhesaMoleculeRow> phesaRowsQuery;
    private final int start;
    private final int length;
    private final int maxComputations;
    private final OCLPhesaEvaluatorNodeSettings settings;
    private final Map<Pair<String,String>,Double> similarity_out;
    private final Map<Pair<String,String>,StereoMolecule[]> alignment_out;
      
    // Author: tl
    
    public PhesaDefaultEvaluatorTask(List<PhesaMoleculeRow> phesaRowsRef, List<PhesaMoleculeRow> phesaRowsQuery, OCLPhesaEvaluatorNodeSettings settings, int q_start, int q_length, Map<Pair<String,String>,Double> similarity_out, Map<Pair<String,String>,StereoMolecule[]> alignment_out ) {
    	this(null, phesaRowsRef, phesaRowsQuery, settings, q_start, q_length, similarity_out , alignment_out );    	
    }

    private PhesaDefaultEvaluatorTask(StoppableRecursiveAction parent, List<PhesaMoleculeRow> phesaRowsRef, List<PhesaMoleculeRow> phesaRowsQuery, OCLPhesaEvaluatorNodeSettings settings, int q_start, int q_length, Map<Pair<String,String>,Double> similarity_out , Map<Pair<String,String>,StereoMolecule[]> alignment_out ) {
        super(parent);
        this.phesaRowsRef    = phesaRowsRef;
        this.phesaRowsQuery  = phesaRowsQuery;
        this.settings = settings;
        this.start = q_start;
        this.length = q_length;
        this.similarity_out = similarity_out;
        this.alignment_out  = alignment_out;
        maxComputations = 128;
    }

    /**
     * Computes the similiarities for all reference phesa molecules
     * vs. the query phesa molecules in the given range.
     * 
     */
    protected void computeDirectly() {
    	DescriptorHandlerShape dhs = DescriptorHandlerShape.getDefaultInstance().getThreadSafeCopy();
    	    	
    	System.out.println("Compute batch: rows ref="+phesaRowsRef.size()+" rows query="+start+":"+(start+length-1));
    	
    	for( int ri = 0; ri < phesaRowsRef.size(); ri++ ) {
    		PheSAMolecule ref = phesaRowsRef.get(ri).getPhesaMolecule();    		
    		for (int idx = start; idx < start + length; idx++) {		
    			LOG.info("phesa eval: "+ri+" x "+idx);
    			if (isStopRequested()) {    				
    				completeExceptionally(new CanceledExecutionException());
    				return;
    			}
    			PheSAMolecule query = phesaRowsQuery.get(idx).getPhesaMolecule();
    			double similarity = Double.NaN;
    			StereoMolecule[] alignment = null;
    			try {
    				similarity = dhs.getSimilarity(query, ref);    				
    				//alignment = dhs.getPreviousAlignment();
    				StereoMolecule arr_ali[] = dhs.getPreviousAlignment();
    				//arr_ali[0].validate();
    				//arr_ali[1].validate();
    				//arr_ali[0].removeExplicitHydrogens(true);
    				//arr_ali[1].removeExplicitHydrogens(true);
    				
    				try {
	    				arr_ali[0].validate();
	    				arr_ali[1].validate();
    				}
    				catch(Exception ex1) {
    					System.out.println("Failed to validate the resulting alignment molecule");
    					ex1.printStackTrace();
    				}
    				
    				alignment = new StereoMolecule[] { new StereoMolecule(arr_ali[0]) , new StereoMolecule(arr_ali[1]) };
    			}    			
    			catch(Exception ex) {
    				LOG.warn("PheSA comparison failed:"
    						+ "a= "+phesaRowsRef.get(ri).getPhesaMolecule().getMolecule().getIDCode() + "    "
    						+ "b= "+phesaRowsQuery.get(idx).getPhesaMolecule().getMolecule().getIDCode());
    				
    				ex.printStackTrace();
    			}
    			if(alignment != null) {
	    			LOG.info("phesa eval: "+ri+" x "+idx+"  sim = "+similarity);
	    			Pair<String,String> key = new Pair<String,String>( phesaRowsRef.get(ri).getRowKey() , phesaRowsQuery.get(idx).getRowKey() );
	    			this.similarity_out.put( key , similarity );
	    			this.alignment_out.put( key , alignment);
    			}
    			else {
    				LOG.warn("Exception in PheSA similarity evaluation..");
    				Pair<String,String> key = new Pair<String,String>( phesaRowsRef.get(ri).getRowKey() , phesaRowsQuery.get(idx).getRowKey() );
	    			this.similarity_out.put( key , similarity );
	    			alignment = new StereoMolecule[] { new StereoMolecule() , new StereoMolecule() };
	    			this.alignment_out.put( key , alignment);    				
    			}
    		}
    	}
    	//System.out.println("done with job");
    	LOG.info("Done with job");
    	LOG.info("Still queued: " + getPool().getQueuedTaskCount());
    	LOG.info("Still running: " + getPool().getRunningThreadCount());
    }

    @Override
    protected void compute() {
        if (isStopRequested()) {
            completeExceptionally(new CanceledExecutionException());
            return;
        }

        if (length == 1 || (length) <= maxComputations) {
            computeDirectly();
            System.out.println("done computing!");            
            return;
        }
        int split = length / 2;
        invokeAll(new PhesaDefaultEvaluatorTask(this, phesaRowsRef, phesaRowsQuery, settings, start, split, similarity_out, alignment_out),
                new PhesaDefaultEvaluatorTask(this, phesaRowsRef, phesaRowsQuery, settings, start + split, length - split, similarity_out,alignment_out));
        LOG.info("Forking...");
    }    
    
    
}
