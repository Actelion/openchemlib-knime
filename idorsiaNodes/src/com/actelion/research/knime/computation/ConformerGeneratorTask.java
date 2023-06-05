package com.actelion.research.knime.computation;

import static com.actelion.research.chem.descriptor.DescriptorConstants.DESCRIPTOR_Flexophore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;
import org.openmolecules.chem.conf.gen.ConformerGenerator;
import org.openmolecules.chem.conf.gen.RigidFragmentCache;
import org.openmolecules.chem.conf.so.ConformationSelfOrganizer;
import org.openmolecules.chem.conf.so.SelfOrganizedConformer;

import com.actelion.research.calc.Matrix;
import com.actelion.research.calc.SingularValueDecomposition;
import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.Coordinates;
import com.actelion.research.chem.StereoMolecule;
//import com.actelion.research.chem.calculator.SuperposeCalculator;
import com.actelion.research.chem.conf.TorsionDB;
import com.actelion.research.chem.conf.TorsionDescriptor;
import com.actelion.research.chem.conf.TorsionDescriptorHelper;
import com.actelion.research.chem.contrib.HydrogenHandler;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.nodes.calculation.OCLConformerGeneratorNodeSettings;
import com.actelion.research.knime.utils.DescriptorHelpers;

// Author: tl

public class ConformerGeneratorTask extends StoppableRecursiveAction {
	private static NodeLogger LOG = NodeLogger.getLogger(ConformerGeneratorTask.class);

	private final List<MoleculeRow> moleculeRows;
	private final int start;
	private final int length;
	private final int maxComputations;
	private final OCLConformerGeneratorNodeSettings settings;
	private final Map<String, List<StereoMolecule>> conformers_out;
	
	private static Boolean conformerDefaultCacheInitialized = Boolean.FALSE;

	public ConformerGeneratorTask(List<MoleculeRow> moleculeRows, OCLConformerGeneratorNodeSettings settings, int start,
			int length, Map<String, List<StereoMolecule>> conf_out) {
		this(null, moleculeRows, settings, start, length, conf_out);
	}

	private ConformerGeneratorTask(StoppableRecursiveAction parent, List<MoleculeRow> moleculeRows,
			OCLConformerGeneratorNodeSettings settings, int start, int length,
			Map<String, List<StereoMolecule>> conf_out) {
		super(parent);
		this.moleculeRows = moleculeRows;
		this.settings = settings;
		this.start = start;
		this.length = length;
		this.conformers_out = conf_out;
		maxComputations = 16;
	}

	protected void computeDirectly() {

		for (int idx = start; idx < start + length; idx++) {
			if (isStopRequested()) {
				completeExceptionally(new CanceledExecutionException());
				return;
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
		invokeAll(new ConformerGeneratorTask(this, moleculeRows, settings, start, split, conformers_out),
				new ConformerGeneratorTask(this, moleculeRows, settings, start + split, length - split,
						conformers_out));
		LOG.info("Forking...");
	}
}