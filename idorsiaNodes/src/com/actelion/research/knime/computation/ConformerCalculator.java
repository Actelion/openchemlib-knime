package com.actelion.research.knime.computation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Matrix4d;

import org.knime.core.node.NodeLogger;
import org.openmolecules.chem.conf.gen.ConformerGenerator;
import org.openmolecules.chem.conf.gen.RigidFragmentCache;
import org.openmolecules.chem.conf.so.ConformationSelfOrganizer;
import org.openmolecules.chem.conf.so.SelfOrganizedConformer;

import com.actelion.research.calc.Matrix;
import com.actelion.research.calc.SingularValueDecomposition;
import com.actelion.research.chem.Coordinates;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.conf.TorsionDB;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.nodes.calculation.OCLConformerGeneratorNodeSettings;

//Author: tl

public class ConformerCalculator {
	private static NodeLogger LOG = NodeLogger.getLogger(ConformerCalculator.class);
	
	//private OCLConformerGeneratorNodeSettings settings;
	private static Boolean conformerDefaultCacheInitialized = Boolean.FALSE;
	
	public static List<StereoMolecule> computeConformers(OCLConformerGeneratorNodeSettings settings, StereoMolecule mol) {

		// if(mol.getAllAtoms()>mol.getAtoms()) {
		//HydrogenHandler.addImplicitHydrogens(mol);
		// }

		// boolean isOneStereoIsomer = !hasMultipleStereoIsomers(mol);
		synchronized(conformerDefaultCacheInitialized) {
			if(!conformerDefaultCacheInitialized.booleanValue()) {
				LOG.info("Load default conformer cache");
				System.out.println("ConformerGeneratorTask::computeConformers() -> Initialize default conformer cache");
				RigidFragmentCache.getDefaultInstance().loadDefaultCache();
				conformerDefaultCacheInitialized = Boolean.TRUE;
				System.out.println("ConformerGeneratorTask::computeConformers() -> Initialize default conformer cache -> success");
			}
		}

		int mMaxConformers = settings.getMaxConformers();
		int mAlgorithm = settings.getAlgorithm();
		int mTorsionSource = settings.getTorsionSource();

		// FFMolecule ffmol = null;
		org.openmolecules.chem.conf.gen.ConformerGenerator cg = null;
		ConformationSelfOrganizer cs = null;
		// List<FFMolecule> isomerList = null;

		Matrix4d matrix = null;
		Coordinates[] refCoords = null;
		Coordinates[] coords = null;
		int[] superposeAtom = null;

//		TorsionDescriptorHelper torsionHelper = null;
//		ArrayList<TorsionDescriptor> torsionDescriptorList = null;
//		if (mMaxConformers > 1 && false ) {
//			torsionHelper = new TorsionDescriptorHelper(mol);
//			torsionDescriptorList = new ArrayList<>();
//		}

//		StringBuilder coordsBuilder = new StringBuilder();
//		StringBuilder energyBuilder = new StringBuilder();
		
		List<StereoMolecule> conformers = new ArrayList<>();
		
		System.out.println("Start computing conformers!");

		for (int i = 0; i < mMaxConformers; i++) {
			System.out.println("next confi");
			switch (mAlgorithm) {
			case OCLConformerGeneratorNodeSettings.ADAPTIVE_RANDOM:
				if (cg == null) {
					cg = new org.openmolecules.chem.conf.gen.ConformerGenerator();
					cg.initializeConformers(mol,
							org.openmolecules.chem.conf.gen.ConformerGenerator.STRATEGY_ADAPTIVE_RANDOM, 1000,
							mTorsionSource == OCLConformerGeneratorNodeSettings.TORSION_SOURCE_6_STEPS);
				}
				mol = cg.getNextConformerAsMolecule(mol);
				break;
			case OCLConformerGeneratorNodeSettings.SYSTEMATIC:
				if (cg == null) {
					cg = new org.openmolecules.chem.conf.gen.ConformerGenerator();
					cg.initializeConformers(mol,
							org.openmolecules.chem.conf.gen.ConformerGenerator.STRATEGY_LIKELY_SYSTEMATIC, 1000,
							mTorsionSource == OCLConformerGeneratorNodeSettings.TORSION_SOURCE_6_STEPS);
				}
				mol = cg.getNextConformerAsMolecule(mol);
				break;
			case OCLConformerGeneratorNodeSettings.LOW_ENERGY_RANDOM:
				if (cg == null) {
					cg = new org.openmolecules.chem.conf.gen.ConformerGenerator();
					cg.initializeConformers(mol,
							org.openmolecules.chem.conf.gen.ConformerGenerator.STRATEGY_LIKELY_RANDOM, 1000,
							mTorsionSource == OCLConformerGeneratorNodeSettings.TORSION_SOURCE_6_STEPS);
				}
				mol = cg.getNextConformerAsMolecule(mol);
				break;
			case OCLConformerGeneratorNodeSettings.PURE_RANDOM:
				if (cg == null) {
					cg = new org.openmolecules.chem.conf.gen.ConformerGenerator();
					cg.initializeConformers(mol,
							org.openmolecules.chem.conf.gen.ConformerGenerator.STRATEGY_PURE_RANDOM, 1000,
							mTorsionSource == OCLConformerGeneratorNodeSettings.TORSION_SOURCE_6_STEPS);
				}
				mol = cg.getNextConformerAsMolecule(mol);
				break;
			case OCLConformerGeneratorNodeSettings.SELF_ORGANIZED:
				if (cs == null) {
					org.openmolecules.chem.conf.gen.ConformerGenerator.addHydrogenAtoms(mol);
					cs = new ConformationSelfOrganizer(mol, true);
					cs.initializeConformers(0, mMaxConformers);
				}
				SelfOrganizedConformer soc = cs.getNextConformer();
				if (soc == null) {
					mol = null;
				} else {
					soc.toMolecule(mol);
				}
				break;
			}

			if (mol == null) {
				System.out.println("break, enough conformers");
				// in this case we already computed a representative set of low-strain
				// conformers!
				break;
			}

//			case ACTELION3D:
//				// from here AdvancedTools based
//				try {
//					if (isomerList == null) {
//						ConformerGenerator.addHydrogenAtoms(mol);
//						isomerList = TorsionCalculator.createAllConformations(new FFMolecule(mol));
//						}
//					if (isomerList.size() > i)
//						ffmol = isomerList.get(i);
//					else
//						mol = null;
//					}
//				catch (Exception e) {
//					e.printStackTrace();
//					}
//				break;
//				}

			// If is first conformer, then prepare matrix and coords for superpositioning
			if (matrix == null) {
				LOG.debug("first conformer -> compute superpositioning");
				centerConformer(mol);
				matrix = new Matrix4d();
				superposeAtom = suggestSuperposeAtoms(mol);
				coords = new Coordinates[superposeAtom.length];
				refCoords = new Coordinates[superposeAtom.length];
				for (int j = 0; j < superposeAtom.length; j++)
					refCoords[j] = new Coordinates(mol.getCoordinates(superposeAtom[j]));
			} else { // superpose onto first conformer
				// TODO: readd superposing..
				//superpose(mol, superposeAtom, coords, refCoords, matrix);
			}

			LOG.debug("StereoMolecule with 3d is ready");
			StereoMolecule molToStore = new StereoMolecule(mol);

			if (!OCLConformerListDataCell.testAllHydrogensExplicit(molToStore)) {
				LOG.warn("Huh!!?? Add explicit hydrogen atoms AGAIN?");
				LOG.debug("Huh!!?? Add explicit hydrogen atoms AGAIN?");
				// HydrogenHandler.addImplicitHydrogens(molToStore);
				ConformerGenerator.addHydrogenAtoms(molToStore);
			}
			
			if(true) {
				try {
					molToStore.validate();
					LOG.debug("Conformer successfully validated!");
				}
				catch(Exception ex) {
					LOG.debug("Failed to validate conformer",ex);
					//ex.printStackTrace();
				}
			}			

			conformers.add(molToStore);
		}

		return conformers;
	}
	
	private void superpose(StereoMolecule mol, Coordinates[] coords, Coordinates[] refCoords, Coordinates refCOG) {
		Coordinates cog = kabschCOG(coords);
		double[][] matrix = kabschAlign(refCoords, coords, refCOG, cog);
		for (int atom=0; atom<mol.getAllAtoms(); atom++) {
			Coordinates c = mol.getCoordinates(atom);
			c.sub(cog);
			c.rotate(matrix);
			c.add(refCOG);
		}
	}
	
	public static Coordinates kabschCOG(Coordinates[] coords) {
		int counter = 0;
		Coordinates cog = new Coordinates();
		for(Coordinates c:coords) {
			cog.add(c);
			counter++;
		}
		cog.scale(1.0/counter);
		return cog;
	}

	public static double[][] kabschAlign(Coordinates[] coords1, Coordinates[] coords2, Coordinates cog1, Coordinates cog2) {
		double[][] m = new double[3][3];
		double[][] c1 = Arrays.stream(coords1).map(e -> new double[] {e.x-cog1.x,e.y-cog1.y,e.z-cog1.z}).toArray(double[][]::new);
		double[][] c2 = Arrays.stream(coords2).map(e -> new double[] {e.x-cog2.x,e.y-cog2.y,e.z-cog2.z}).toArray(double[][]::new);
		for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
				double rij = 0.0;
				for(int a=0; a<c1.length; a++)
					rij+= c2[a][i]* c1[a][j];
				m[i][j] = rij;
			}
		}

		SingularValueDecomposition svd = new SingularValueDecomposition(m,null,null);

		Matrix u = new Matrix(svd.getU());
		Matrix v = new Matrix(svd.getV());
		Matrix ut = u.getTranspose();
		Matrix vut = v.multiply(ut);
		double det = vut.det();

		Matrix ma = new Matrix(3,3);
		ma.set(0,0,1.0);
		ma.set(1,1,1.0);
		ma.set(2,2,det);

		Matrix rot = ma.multiply(ut);
		rot = v.multiply(rot);
		assert(rot.det()>0.0);
		rot = rot.getTranspose();
		return rot.getArray();
		}
	
	public static void centerConformer(StereoMolecule mol) {
		double x = 0;
		double y = 0;
		double z = 0;
		for (int atom = 0; atom < mol.getAllAtoms(); atom++) {
			x += mol.getAtomX(atom);
			y += mol.getAtomY(atom);
			z += mol.getAtomZ(atom);
		}
		x /= mol.getAllAtoms();
		y /= mol.getAllAtoms();
		z /= mol.getAllAtoms();
		for (int atom = 0; atom < mol.getAllAtoms(); atom++) {
			mol.setAtomX(atom, mol.getAtomX(atom) - x);
			mol.setAtomY(atom, mol.getAtomY(atom) - y);
			mol.setAtomZ(atom, mol.getAtomZ(atom) - z);
		}
	}

	public static int[] suggestSuperposeAtoms(StereoMolecule mol) {
		boolean[] isRotatableBond = new boolean[mol.getAllBonds()];
		int count = TorsionDB.findRotatableBonds(mol, true, isRotatableBond);
		if (count == 0) {
			int[] coreAtom = new int[mol.getAllAtoms()];
			for (int atom = 0; atom < mol.getAllAtoms(); atom++)
				coreAtom[atom] = atom;
			return coreAtom;
		}

		int[] fragmentNo = new int[mol.getAllAtoms()];
		int fragmentCount = mol.getFragmentNumbers(fragmentNo, isRotatableBond, true);
		int[] fragmentSize = new int[fragmentCount];
		float[] atad = mol.getAverageTopologicalAtomDistance();
		float[] fragmentATAD = new float[fragmentCount];
		for (int atom = 0; atom < mol.getAtoms(); atom++) {
			fragmentATAD[fragmentNo[atom]] += atad[atom];
			fragmentSize[fragmentNo[atom]]++;
		}
		int bestFragment = -1;
		float bestATAD = Float.MAX_VALUE;
		for (int i = 0; i < fragmentCount; i++) {
			fragmentATAD[i] /= fragmentSize[i];
			if (bestATAD > fragmentATAD[i]) {
				bestATAD = fragmentATAD[i];
				bestFragment = i;
			}
		}
		int fragmentSizeWithNeighbours = fragmentSize[bestFragment];
		for (int atom = 0; atom < mol.getAtoms(); atom++) {
			if (fragmentNo[atom] == bestFragment) {
				for (int i = 0; i < mol.getConnAtoms(atom); i++) {
					int connAtom = mol.getConnAtom(atom, i);
					if (fragmentNo[connAtom] != bestFragment && fragmentNo[connAtom] != fragmentCount) {
						fragmentNo[connAtom] = fragmentCount;
						fragmentSizeWithNeighbours++;
					}
				}
			}
		}
		int[] coreAtom = new int[fragmentSizeWithNeighbours];
		int index = 0;
		for (int atom = 0; atom < mol.getAtoms(); atom++)
			if (fragmentNo[atom] == bestFragment || fragmentNo[atom] == fragmentCount)
				coreAtom[index++] = atom;
		return coreAtom;
	}
	
}
