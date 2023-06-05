package com.actelion.research.knime.computation;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.DescriptorRow;
import com.actelion.research.knime.utils.DescriptorHelpers;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SimilarityCalculator extends StoppableRecursiveAction {
    private static NodeLogger LOG = NodeLogger.getLogger(SimilarityCalculator.class);

    private final List<DescriptorRow> descriptorRows;
    private final int start;
    private final int length;
    private final int maxComputations = 1000;
    private final DescriptorInfo descriptorInfo;
    private final Map<String, Double[]> similarites;
    private List<Object> queryDescriptors;

    public SimilarityCalculator(List<DescriptorRow> descriptorRows, DescriptorInfo descriptorInfo, List<Object> queryDescriptors, int start, int length, Map<String, Double[]> similarites) {
        this(null, descriptorRows, descriptorInfo, queryDescriptors, start, length, similarites);
    }

    public SimilarityCalculator(StoppableRecursiveAction parent, List<DescriptorRow> descriptorRows, DescriptorInfo descriptorInfo, List<Object> queryDescriptors, int start, int length, Map<String, Double[]> similarites) {
        super(parent);
        this.descriptorRows = descriptorRows;
        this.descriptorInfo = descriptorInfo;
        this.queryDescriptors = queryDescriptors;
        this.start = start;
        this.length = length;
        this.similarites = similarites;
    }

    protected void computeDirectly() {
        for (int idx = start; idx < start + length; idx++) {
            if (isStopRequested()) {
                completeExceptionally(new CanceledExecutionException());
                return;
            }
            DescriptorRow descriptorRow = descriptorRows.get(idx);
            Double[] similarities = new Double[queryDescriptors.size()];
            Arrays.fill(similarities, Double.NaN);
            for (int i = 0; i < queryDescriptors.size(); i++) {
                similarities[i] = DescriptorHelpers.calculateSimilarity(descriptorInfo, descriptorRow.getDescriptor(), queryDescriptors.get(i));
            }
            this.similarites.put(descriptorRow.getRowKey(), similarities);
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
        invokeAll(new SimilarityCalculator(this, descriptorRows, descriptorInfo, queryDescriptors, start, split, similarites),
                new SimilarityCalculator(this, descriptorRows, descriptorInfo, queryDescriptors, start + split, length - split, similarites));
        LOG.info("Forking...");
    }
}
