package com.actelion.research.knime.computation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.DescriptorRow;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.utils.DescriptorHelpers;

public class SimilarityCalculatorActionProvider extends AbstractStoppableChunkedActionProvider {

	private static NodeLogger LOG = NodeLogger.getLogger(SimilarityCalculator.class);

    private final List<DescriptorRow> descriptorRows;
    private final int maxComputations = 1000;
    private final DescriptorInfo descriptorInfo;
    private final Map<String, Double[]> similarites;
    private List<Object> queryDescriptors;

    public SimilarityCalculatorActionProvider(List<DescriptorRow> descriptorRows, DescriptorInfo descriptorInfo, List<Object> queryDescriptors, int chunk_size, Map<String, Double[]> similarites) {
        super(descriptorRows.size(), chunk_size);
        this.descriptorRows = descriptorRows;
        this.descriptorInfo = descriptorInfo;
        this.queryDescriptors = queryDescriptors;        
        this.similarites = similarites;
    }

    
	@Override
	protected boolean processChunk(int start, int end) throws Exception {
		for (int idx = start; idx < end; idx++) {
            if (isStopRequested()) {
                throw new CanceledExecutionException();
                //return;
            }
            DescriptorRow descriptorRow = descriptorRows.get(idx);
            Double[] similarities = new Double[queryDescriptors.size()];
            Arrays.fill(similarities, Double.NaN);
            for (int i = 0; i < queryDescriptors.size(); i++) {
                similarities[i] = DescriptorHelpers.calculateSimilarity(descriptorInfo, descriptorRow.getDescriptor(), queryDescriptors.get(i));
            }
            this.similarites.put(descriptorRow.getRowKey(), similarities);
        }
        LOG.info("Done with job "+ start +" - "+end);
        //LOG.info("Still queued: " + getPool().getQueuedTaskCount());
        //LOG.info("Still running: " + getPool().getRunningThreadCount());
        return true;
	}
		
}
