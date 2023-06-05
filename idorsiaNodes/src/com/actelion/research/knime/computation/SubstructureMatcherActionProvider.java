package com.actelion.research.knime.computation;

import java.util.Map;
import java.util.Vector;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.SSSearcher;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.MoleculeRow;

// Author: tl

public class SubstructureMatcherActionProvider extends AbstractStoppableChunkedActionProvider {
    private static NodeLogger LOG = NodeLogger.getLogger(SubstructureMatcherActionProvider.class);

	
    MoleculeRow[] records;
	MoleculeRow[] fragments;
	Map<String, Vector<String>> matching;
	
	public SubstructureMatcherActionProvider(MoleculeRow[] records, int chunk_size, MoleculeRow[] fragments, Map<String, Vector<String>> matching) {
		super(records.length,chunk_size);
		this.records = records;
		this.fragments = fragments;
		this.matching = matching;
	}

	@Override
	protected boolean processChunk(int start, int end) throws Exception {
		SSSearcher ssSearcher = new SSSearcher();

        for (int idx = start; idx < end; idx++) {
            if (isStopRequested()) {
                //completeExceptionally(new CanceledExecutionException());
            	throw new CanceledExecutionException();
                //return false;
            }

            MoleculeRow record = records[idx];
            StereoMolecule molecule = record.getMolecule();
            for (MoleculeRow fragmentRow : fragments) {
                ssSearcher.setFragment(fragmentRow.getMolecule());
                ssSearcher.setMolecule(molecule);
                if (ssSearcher.isFragmentInMolecule(SSSearcher.cDefaultMatchMode)) {
                    matching.get(record.getRowKey()).add(fragmentRow.getRowKey());
                }
            }
        }
        LOG.info("Done with job "+start+" - "+end);
        //LOG.info("Still queued: " + getPool().getQueuedTaskCount());
        //LOG.info("Still running: " + getPool().getRunningThreadCount());
        
        return true;
	}
	
	
	

}
