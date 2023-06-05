package com.actelion.research.knime.computation;

import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.PheSAMolecule;
import com.actelion.research.knime.data.PhesaMoleculeRow;
import com.actelion.research.knime.nodes.calculation.OCLPhesaEvaluatorNodeSettings;

// Author: tl

public class PhesaDefaultEvaluatorActionProvider extends AbstractStoppableChunkedActionProvider {
	
private static NodeLogger LOG = NodeLogger.getLogger(PhesaDefaultEvaluatorActionProvider.class);	
	
    private final List<PhesaMoleculeRow> phesaRowsRef;
    private final List<PhesaMoleculeRow> phesaRowsQuery;
    
    private final OCLPhesaEvaluatorNodeSettings settings;
    private final Map<Pair<String,String>,Double> similarity_out;
    private final Map<Pair<String,String>,StereoMolecule[]> alignment_out;
        

    public PhesaDefaultEvaluatorActionProvider(List<PhesaMoleculeRow> phesaRowsRef, List<PhesaMoleculeRow> phesaRowsQuery, OCLPhesaEvaluatorNodeSettings settings, int chunk_size, Map<Pair<String,String>,Double> similarity_out , Map<Pair<String,String>,StereoMolecule[]> alignment_out ) {
        super(phesaRowsQuery.size(),chunk_size); // parallelization is over the query (i.e. library molecules!)
        this.phesaRowsRef    = phesaRowsRef;
        this.phesaRowsQuery  = phesaRowsQuery;
        this.settings = settings;
        this.similarity_out = similarity_out;
        this.alignment_out  = alignment_out;
    }


	@Override
	protected boolean processChunk(int start, int end) throws Exception {
		DescriptorHandlerShape dhs = DescriptorHandlerShape.getDefaultInstance().getThreadSafeCopy();
    	
    	LOG.info("Compute batch: rows ref="+phesaRowsRef.size()+" rows query="+start+":"+end);
    	
    	for( int ri = 0; ri < phesaRowsRef.size(); ri++ ) {
    		PheSAMolecule ref = phesaRowsRef.get(ri).getPhesaMolecule();    		
    		for (int idx = start; idx < end; idx++) {		
    			LOG.info("phesa eval: "+ri+" x "+idx);
    			if (isStopRequested()) {    				
    				throw new CanceledExecutionException();
    				//return;
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
    	return true;
	}
	

}
