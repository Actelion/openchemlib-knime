package com.actelion.research.knime.computation;

import static com.actelion.research.chem.descriptor.DescriptorConstants.DESCRIPTOR_Flexophore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.utils.DescriptorHelpers;

//Author: tl

public class DescriptorCalculatorActionProvider extends AbstractStoppableChunkedActionProvider {

	private static NodeLogger LOG = NodeLogger.getLogger(DescriptorCalculator.class);

    private final List<MoleculeRow> moleculeRows;
    
    private final List<DescriptorInfo> descriptorInfos;
    private final Map<String, Map<DescriptorInfo, byte[]>> descriptors;
    

    public DescriptorCalculatorActionProvider(List<MoleculeRow> moleculeRows, List<DescriptorInfo> descriptorInfos, int chunk_size, Map<String, Map<DescriptorInfo, byte[]>> descriptors) {
        super(moleculeRows.size(),chunk_size);
        this.moleculeRows = moleculeRows;
        this.descriptorInfos = descriptorInfos;

        this.descriptors = descriptors;
//        if (descriptorInfos.contains(DESCRIPTOR_Flexophore)) {
//            maxComputations = 50;
//        } else {
//            maxComputations = 500;
//        }
    }


	@Override
	protected boolean processChunk(int start, int end) throws Exception {
		for (int idx = start; idx < end; idx++) {
            if (isStopRequested()) {
                throw new CanceledExecutionException();
//                return;
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
        return true;
	}

	
}
