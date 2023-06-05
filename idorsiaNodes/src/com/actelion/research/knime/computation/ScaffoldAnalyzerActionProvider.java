package com.actelion.research.knime.computation;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.FrequencyItem;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.ScaffoldType;

public class ScaffoldAnalyzerActionProvider extends AbstractStoppableChunkedActionProvider {
	
	private static NodeLogger LOG = NodeLogger.getLogger(ScaffoldAnalyzerActionProvider.class);

    private final List<MoleculeRow> moleculeRows;
    //private final IDCodeParser idCodeParser;
    private ScaffoldType scaffoldType;
    private Vector<FrequencyItem> frequencyItems;
    private Map<String, String> fragmentList;
    

    public ScaffoldAnalyzerActionProvider(List<MoleculeRow> moleculeRows, ScaffoldType scaffoldType, int chunk_size, Vector<FrequencyItem> frequencyItems, Map<String, String> fragmentList) {
        super(moleculeRows.size(),chunk_size);
        this.moleculeRows = moleculeRows;
        this.scaffoldType = scaffoldType;
        this.frequencyItems = frequencyItems;
        this.fragmentList = fragmentList;
        //idCodeParser = new IDCodeParser();
    }


	@Override
	protected boolean processChunk(int start, int end) throws Exception {
		for (int idx = start; idx < end; idx++) {
            if (isStopRequested()) {
                throw new CanceledExecutionException();
                //return;
            }

            MoleculeRow moleculeRow = moleculeRows.get(idx);
            StereoMolecule molecule = moleculeRow.getMolecule();
            StereoMolecule[] fragment = ((scaffoldType == scaffoldType.MURCKO_SCAFFOLD)
                    || (scaffoldType == ScaffoldType.MURCKO_SKELETON))
                    ? ScaffoldAnalyzer.getMurckoScaffold(molecule, scaffoldType)
                    : (scaffoldType == ScaffoldType.MOST_CENTRAL_RING_SYSTEM)
                    ? ScaffoldAnalyzer.getMostCentralRingSystem(molecule)
                    : ScaffoldAnalyzer.getRingSystems(molecule, scaffoldType);
            String allFragmentIDCode = "";

            if (fragment != null) {
                for (int i = 0; i < fragment.length; i++) {
                    String fragmentIDCode = new Canonizer(fragment[i]).getIDCode();

                    if (allFragmentIDCode.equals("")) {
                        allFragmentIDCode = fragmentIDCode;
                    } else {
                        allFragmentIDCode += "<NL>" + fragmentIDCode;
                    }
                    synchronized (frequencyItems) {
                        int fragmentIdx = frequencyItems.indexOf(new FrequencyItem(fragmentIDCode));
                        if (fragmentIdx < 0) {
                            frequencyItems.add(new FrequencyItem(fragmentIDCode));
                        } else {
                            FrequencyItem frequencyItem = frequencyItems.get(fragmentIdx);
                            frequencyItem.incrementCount();
                        }
                    }
                }
                fragmentList.put(moleculeRow.getRowKey(), allFragmentIDCode);

//                fragmentList.add(allFragmentIDCode);
            }
        }
        LOG.info("Done with job");
        return true;
	}

}
