package com.actelion.research.knime.computation;

import com.actelion.research.chem.SSSearcher;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.MoleculeRow;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import java.util.Map;
import java.util.Vector;

public class SubstructureMatcher extends StoppableRecursiveAction {
    private static NodeLogger LOG = NodeLogger.getLogger(SubstructureMatcher.class);

    private final MoleculeRow[] records;
    private final MoleculeRow[] fragments;
    private final int start;
    private final int length;
    private final Map<String, Vector<String>> matching;
    private final int maxSearches = 1000;

    public SubstructureMatcher(MoleculeRow[] records, MoleculeRow[] fragments, int start, int length, Map<String, Vector<String>> matching) {
        this(null, records, fragments, start, length, matching);
    }

    private SubstructureMatcher(StoppableRecursiveAction parent, MoleculeRow[] records, MoleculeRow[] fragments, int start, int length, Map<String, Vector<String>> matching) {
        super(parent);
        this.records = records;
        this.fragments = fragments;
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
            for (MoleculeRow fragmentRow : fragments) {
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
        invokeAll(new SubstructureMatcher(records, fragments, start, split, matching),
                new SubstructureMatcher(records, fragments, start + split, length - split, matching));
        LOG.info("Forking...");
    }
}
