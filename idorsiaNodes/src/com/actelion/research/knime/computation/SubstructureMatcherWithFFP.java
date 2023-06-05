package com.actelion.research.knime.computation;

import com.actelion.research.chem.SSSearcher;
import com.actelion.research.chem.SSSearcherWithIndex;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorHandlerFFP512;
import com.actelion.research.chem.descriptor.DescriptorHandlerLongFFP512;
import com.actelion.research.knime.data.MoleculeRow;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.Map;
import java.util.Vector;

public class SubstructureMatcherWithFFP extends StoppableRecursiveAction {
    private static NodeLogger LOG = NodeLogger.getLogger(SubstructureMatcher.class);

    private final MoleculeRow[] records;
    private final Object[] records_ffps;
    private final MoleculeRow[] fragments;
    private final Object[] fragments_ffps;
    
    private final int start;
    private final int length;
    private final Map<String, Vector<String>> matching;
    private final int maxSearches = 1000;

    public SubstructureMatcherWithFFP(MoleculeRow[] records, MoleculeRow[] fragments, Object[] records_ffps, Object[] fragments_ffps, int start, int length, Map<String, Vector<String>> matching) {
        this(null, records, fragments, records_ffps, fragments_ffps, start, length, matching);
    }

    private SubstructureMatcherWithFFP(StoppableRecursiveAction parent, MoleculeRow[] records, MoleculeRow[] fragments, Object[] records_ffps, Object[] fragments_ffps, int start, int length, Map<String, Vector<String>> matching) {
        super(parent);
        this.records = records;
        this.fragments = fragments;
        this.records_ffps = records_ffps;
        this.fragments_ffps = fragments_ffps;
        this.start = start;
        this.length = length;
        this.matching = matching;
    }

    protected void computeDirectly() {
        SSSearcher ssSearcher = new SSSearcher();

        for (int idx = start; idx < start + length; idx++) {
            if (isStopRequested()) {
                completeExceptionally(new CanceledExecutionException());
                return;
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
        LOG.info("Still queued: " + getPool().getQueuedTaskCount());
        LOG.info("Still running: " + getPool().getRunningThreadCount());
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
    
    
    
    
    @Override
    protected void compute() {
        if (isStopRequested()) {
            completeExceptionally(new CanceledExecutionException());
            return;
        }

        if (length == 1 || (length * fragments.length) <= maxSearches) {
            computeDirectly();
            return;
        }
        int split = length / 2;
        invokeAll(new SubstructureMatcherWithFFP(records, fragments, records_ffps, fragments_ffps, start, split, matching),
                new SubstructureMatcherWithFFP(records, fragments, records_ffps, fragments_ffps, start + split, length - split, matching));
        LOG.info("Forking...");
    }
}
