package com.actelion.research.knime.computation;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.utils.DescriptorHelpers;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.actelion.research.chem.descriptor.DescriptorConstants.DESCRIPTOR_Flexophore;

public class DescriptorCalculator extends StoppableRecursiveAction {
    private static NodeLogger LOG = NodeLogger.getLogger(DescriptorCalculator.class);

    private final List<MoleculeRow> moleculeRows;
    private final int start;
    private final int length;
    private final int maxComputations;
    private final List<DescriptorInfo> descriptorInfos;
    private final Map<String, Map<DescriptorInfo, byte[]>> descriptors;

    public DescriptorCalculator(List<MoleculeRow> moleculeRows, List<DescriptorInfo> descriptorInfos, int start, int length, Map<String, Map<DescriptorInfo, byte[]>> descriptors) {
        this(null, moleculeRows, descriptorInfos, start, length, descriptors);
    }

    private DescriptorCalculator(StoppableRecursiveAction parent, List<MoleculeRow> moleculeRows, List<DescriptorInfo> descriptorInfos, int start, int length, Map<String, Map<DescriptorInfo, byte[]>> descriptors) {
        super(parent);
        this.moleculeRows = moleculeRows;
        this.descriptorInfos = descriptorInfos;
        this.start = start;
        this.length = length;
        this.descriptors = descriptors;
        if (descriptorInfos.contains(DESCRIPTOR_Flexophore)) {
            maxComputations = 50;
        } else {
            maxComputations = 500;
        }
    }

    protected void computeDirectly() {
        for (int idx = start; idx < start + length; idx++) {
            if (isStopRequested()) {
                completeExceptionally(new CanceledExecutionException());
                return;
            }
            MoleculeRow moleculeRow = moleculeRows.get(idx);
            StereoMolecule molecule = moleculeRow.getMolecule();
            for (DescriptorInfo descriptorInfo : descriptorInfos) {
                Map<DescriptorInfo, byte[]> map = descriptors.get(moleculeRow.getRowKey());
                if (map == null) {
                    map = new HashMap<>();
                    descriptors.put(moleculeRow.getRowKey(), map);
                }
                Object d = DescriptorHelpers.calculateDescriptor(molecule, descriptorInfo);
                if (d != null) {
                    map.put(descriptorInfo, DescriptorHelpers.encode(d, descriptorInfo).getBytes());
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

        if (length == 1 || (length) <= maxComputations) {
            computeDirectly();
            return;
        }
        int split = length / 2;
        invokeAll(new DescriptorCalculator(this, moleculeRows, descriptorInfos, start, split, descriptors),
                new DescriptorCalculator(this, moleculeRows, descriptorInfos, start + split, length - split, descriptors));
        LOG.info("Forking...");
    }
}
