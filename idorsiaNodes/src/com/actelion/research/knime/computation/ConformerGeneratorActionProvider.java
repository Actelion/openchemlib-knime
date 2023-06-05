package com.actelion.research.knime.computation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.nodes.calculation.OCLConformerGeneratorNodeSettings;

//Author: tl

public class ConformerGeneratorActionProvider extends AbstractStoppableChunkedActionProvider {

	private static NodeLogger LOG = NodeLogger.getLogger(ConformerGeneratorTask.class);

	private final List<MoleculeRow> moleculeRows;

	private final OCLConformerGeneratorNodeSettings settings;
	private final Map<String, List<StereoMolecule>> conformers_out;
	
	private Boolean conformerDefaultCacheInitialized = Boolean.FALSE;
	
	public ConformerGeneratorActionProvider(List<MoleculeRow> moleculeRows,
			OCLConformerGeneratorNodeSettings settings, int chunk_size,
			Map<String, List<StereoMolecule>> conf_out) {
		super(moleculeRows.size(),chunk_size);
		this.moleculeRows = moleculeRows;
		this.settings = settings;
		this.conformers_out = conf_out;
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
			String idcode = molecule.getIDCode();
			LOG.info("Compute conformers for: " + idcode);
			List<StereoMolecule> computed_conformers = new ArrayList<>();
			try {
				computed_conformers = ConformerCalculator.computeConformers(this.settings,molecule);
			} catch (Exception ex) {
				LOG.warn("Exception while generating conformers for: " + idcode);
			}
			this.conformers_out.put(idcode, computed_conformers);
		}

		LOG.info("Done with job");	
		return true;
	}	
	
}
