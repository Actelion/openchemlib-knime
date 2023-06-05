package com.actelion.research.knime.computation;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.ScaffoldHelper;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.FrequencyItem;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.ScaffoldType;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ScaffoldAnalyzer extends StoppableRecursiveAction {
    private static NodeLogger LOG = NodeLogger.getLogger(ScaffoldAnalyzer.class);

    private final List<MoleculeRow> moleculeRows;
    private final int start;
    private final int length;
    private final int maxComputations = 1000;
    private final IDCodeParser idCodeParser;
    private ScaffoldType scaffoldType;
    private Vector<FrequencyItem> frequencyItems;
    private Map<String, String> fragmentList;

    public ScaffoldAnalyzer(List<MoleculeRow> moleculeRows, ScaffoldType scaffoldType, int start, int length, Vector<FrequencyItem> frequencyItems, Map<String, String> fragmentList) {
        this(null, moleculeRows, scaffoldType, start, length, frequencyItems, fragmentList);
    }

    private ScaffoldAnalyzer(StoppableRecursiveAction parent, List<MoleculeRow> moleculeRows, ScaffoldType scaffoldType, int start, int length, Vector<FrequencyItem> frequencyItems, Map<String, String> fragmentList) {
        super(parent);
        this.moleculeRows = moleculeRows;
        this.scaffoldType = scaffoldType;
        this.start = start;
        this.length = length;
        this.frequencyItems = frequencyItems;
        this.fragmentList = fragmentList;
        idCodeParser = new IDCodeParser();
    }

    protected void computeDirectly() {
        for (int idx = start; idx < start + length; idx++) {
            if (isStopRequested()) {
                completeExceptionally(new CanceledExecutionException());
                return;
            }

            MoleculeRow moleculeRow = moleculeRows.get(idx);
            StereoMolecule molecule = moleculeRow.getMolecule();
            StereoMolecule[] fragment = ((scaffoldType == scaffoldType.MURCKO_SCAFFOLD)
                    || (scaffoldType == ScaffoldType.MURCKO_SKELETON))
                    ? getMurckoScaffold(molecule, scaffoldType)
                    : (scaffoldType == ScaffoldType.MOST_CENTRAL_RING_SYSTEM)
                    ? getMostCentralRingSystem(molecule)
                    : getRingSystems(molecule, scaffoldType);
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
        invokeAll(new ScaffoldAnalyzer(this, moleculeRows, scaffoldType, start, split, frequencyItems, fragmentList),
                new ScaffoldAnalyzer(this, moleculeRows, scaffoldType, start + split, length - split, frequencyItems, fragmentList));
        LOG.info("Forking...");
    }

    public static StereoMolecule[] getMostCentralRingSystem(StereoMolecule mol) {
        ScaffoldHelper.createMostCentralRingSystem(mol);

        if (mol.getAllAtoms() == 0) {
            return null;
        }

        StereoMolecule[] scaffold = new StereoMolecule[1];

        scaffold[0] = mol;

        return scaffold;
    }

    public static StereoMolecule[] getMurckoScaffold(StereoMolecule mol, ScaffoldType scaffoldType) {
        ScaffoldHelper.createMurckoScaffold(mol, scaffoldType == ScaffoldType.MURCKO_SKELETON);

        if (mol.getAllAtoms() == 0) {
            return null;
        }

        StereoMolecule[] scaffold = new StereoMolecule[1];

        scaffold[0] = mol;

        return scaffold;
    }

    public static StereoMolecule[] getRingSystems(StereoMolecule mol, ScaffoldType scaffoldType) {
        mol.ensureHelperArrays(Molecule.cHelperRings);

        if (mol.getRingSet().getSize() == 0) {
            return null;
        }

        // mark all non-ring atoms for deletion
        for (int atom = 0; atom < mol.getAtoms(); atom++) {
            if (!mol.isRingAtom(atom)) {
                mol.setAtomMarker(atom, true);
            }
        }

        boolean checkFurther = true;

        while (checkFurther) {    // extend ring systems by atoms connected via non-single bonds
            checkFurther = false;

            for (int bond = 0; bond < mol.getBonds(); bond++) {
                if (mol.getBondOrder(bond) > 1) {
                    for (int i = 0; i < 2; i++) {
                        int atom1 = mol.getBondAtom(i, bond);
                        int atom2 = mol.getBondAtom(1 - i, bond);

                        if (mol.isMarkedAtom(atom1) && !mol.isMarkedAtom(atom2)) {
                            mol.setAtomMarker(atom1, false);
                            checkFurther = true;
                        }
                    }
                }
            }
        }

        if (scaffoldType != ScaffoldType.RING_SYSTEMS) {
            for (int atom = 0; atom < mol.getAtoms(); atom++) {
                if (!mol.isMarkedAtom(atom)) {
                    mol.setAtomQueryFeature(atom, Molecule.cAtomQFNoMoreNeighbours, true);
                }
            }

            for (int bond = 0; bond < mol.getBonds(); bond++) {
                for (int i = 0; i < 2; i++) {
                    int atom1 = mol.getBondAtom(i, bond);
                    int atom2 = mol.getBondAtom(1 - i, bond);

                    if ((!mol.isMarkedAtom(atom1) && mol.isMarkedAtom(atom2))
                            || (!mol.isMarkedAtom(atom1) && !mol.isMarkedAtom(atom2) && !mol.isRingBond(bond)
                            && (mol.getBondOrder(bond) == 1))) {
                        if (scaffoldType == ScaffoldType.RING_SYSTEMS_SUBSTITUTION) {
                            mol.setAtomQueryFeature(atom1, Molecule.cAtomQFNoMoreNeighbours, false);
                            mol.setAtomQueryFeature(atom1, Molecule.cAtomQFMoreNeighbours, true);
                        } else {
                            int newAtom = mol.addAtom(mol.getAtomicNo(atom2));

                            mol.setAtomQueryFeature(newAtom, Molecule.cAtomQFNoMoreNeighbours, false);
                            mol.addBond(atom1, newAtom, (i == 0)
                                    ? mol.getBondType(bond)
                                    : 1);    // retain stereo bond if applicable

                            if (scaffoldType == ScaffoldType.RING_SYSTEMS_HETERO_SUBSTITUTION) {
                                if (mol.getAtomicNo(newAtom) != 6) {
                                    int[] carbonList = new int[1];

                                    carbonList[0] = 6;
                                    mol.setAtomList(newAtom, carbonList, true);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (scaffoldType == ScaffoldType.RING_SYSTEMS_SUBSTITUTION) {
            for (int atom = 0; atom < mol.getAtoms(); atom++) {
                if (!mol.isMarkedAtom(atom) && (mol.getAtomQueryFeatures(atom) & Molecule.cAtomQFMoreNeighbours) == 0) {
                    mol.setAtomQueryFeature(atom, Molecule.cAtomQFNoMoreNeighbours, true);
                }
            }
        }

        for (int bond = 0; bond < mol.getBonds(); bond++) {    // mol.getBonds() doesn't consider added bonds!!!
            if (mol.isMarkedAtom(mol.getBondAtom(0, bond)) || mol.isMarkedAtom(mol.getBondAtom(1, bond))
                    || (!mol.isRingBond(bond) && (mol.getBondOrder(bond) == 1))) {
                mol.setBondType(bond, Molecule.cBondTypeDeleted);
            }
        }

        for (int atom = 0; atom < mol.getAtoms(); atom++) {
            if (mol.isMarkedAtom(atom)) {
                mol.markAtomForDeletion(atom);
            }
        }

        mol.deleteMarkedAtomsAndBonds();

        return mol.getFragments();
    }

}
