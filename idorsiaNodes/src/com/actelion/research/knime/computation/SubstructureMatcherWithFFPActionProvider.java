package com.actelion.research.knime.computation;

import java.util.BitSet;
import java.util.Map;
import java.util.Vector;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.SSSearcher;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.MoleculeRow;

public class SubstructureMatcherWithFFPActionProvider extends AbstractStoppableChunkedActionProvider {

	private static NodeLogger LOG = NodeLogger.getLogger(SubstructureMatcherActionProvider.class);

	
	private final MoleculeRow[] records;
	private final Object[] records_ffps;
	private final MoleculeRow[] fragments;
	private final Object[] fragments_ffps;

	Map<String, Vector<String>> matching;
	
	public SubstructureMatcherWithFFPActionProvider(MoleculeRow[] records, Object[] records_ffps, MoleculeRow[] fragments, Object[] fragments_ffps, int chunk_size, Map<String, Vector<String>> matching) {
		super(records.length, chunk_size);
		
		this.records = records;
		this.records_ffps = records_ffps;
		this.fragments = fragments;
		this.fragments_ffps = fragments_ffps;
		this.matching = matching;
	}
	
	@Override
	protected boolean processChunk(int start, int end) throws Exception {
		SSSearcher ssSearcher = new SSSearcher();

        for (int idx = start; idx < end; idx++) {
            if (isStopRequested()) {
            	throw new CanceledExecutionException();
                //completeExceptionally(new CanceledExecutionException());
                //return;
            }

            MoleculeRow record = records[idx];
            StereoMolecule molecule = record.getMolecule();
            Object ffp_mol = records_ffps[idx];
            int frag_cnt = 0;
            for (MoleculeRow fragmentRow : fragments) {
            	Object ffp_frag = fragments_ffps[frag_cnt++];
            	if(ffp_mol!=null && ffp_frag!=null) {
            		if(ffp_mol instanceof long[] && ffp_frag instanceof long[]) {
            			BitSet fs_ffp_mol  = BitSet.valueOf((long[]) ffp_mol);
            			BitSet fs_ffp_frag = BitSet.valueOf((long[]) ffp_frag);
            			if(!testSuperset(fs_ffp_mol,fs_ffp_frag)) {
            				continue;
            			}
            		}
            		if(ffp_mol instanceof int[] && ffp_frag instanceof int[]) {
            			if(!testSuperset( (int[]) ffp_mol, (int[]) ffp_frag)) {
            				continue;
            			}
            		}
            	}
                ssSearcher.setFragment(fragmentRow.getMolecule());
                ssSearcher.setMolecule(molecule);
                if (ssSearcher.isFragmentInMolecule(SSSearcher.cDefaultMatchMode)) {
                    matching.get(record.getRowKey()).add(fragmentRow.getRowKey());
                }
            }
        }
        LOG.info("Done with job");
        //LOG.info("Still queued: " + getPool().getQueuedTaskCount());
        //LOG.info("Still running: " + getPool().getRunningThreadCount());
        
        return true; 
	}
	
	
    /**
     * checks if a is superset of b
     * @param bs_a
     * @param bs_b
     * @return
     */
    public static boolean testSuperset(BitSet bs_a, BitSet bs_b) {
    	 BitSet ti = (BitSet) bs_a.clone();
         ti.or(bs_b);
         return ti.equals(bs_a);
    }
    
    /**
     * checks if a is superset of b
     * @param bs_a
     * @param bs_b
     * @return
     */
    public static boolean testSuperset(int bs_a[], int bs_b[]) {
    	boolean is_superset = true;
    	for(int zi=0;zi<bs_b.length;zi++) {
    		is_superset = is_superset && ( (bs_a[zi] & bs_b[zi]) == bs_a[zi] );
    	}
    	return is_superset;
    }
	
}
