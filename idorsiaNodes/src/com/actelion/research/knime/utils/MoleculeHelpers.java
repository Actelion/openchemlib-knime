package com.actelion.research.knime.utils;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;

import java.util.Arrays;

public class MoleculeHelpers {

    /**
     * Converts the molecule to a reaction molecule string, i.e. the IDCode plus the atom mapping
     *
     * @param molecule
     * @return
     */
    public static String toReactionMoleculeString(StereoMolecule molecule) {
        return molecule.getIDCode() + "\t" + getAtomMapNumbersAsString(molecule);
    }

    public static StereoMolecule fromReactionMoleculeString(String string) {
        IDCodeParser idCodeParser = new IDCodeParser();
        String[] parts = string.split("\t");
        String idCode = parts[0];
        StereoMolecule mol = idCodeParser.getCompactMolecule(idCode);
        int[] atomMapNo = new int[mol.getAtoms()];
        Arrays.fill(atomMapNo, -1);
        if (parts.length > 1) {
            String[] atomMapStr = parts[1].split(",");
            for (int i = 0; i < atomMapStr.length; i++) {
                atomMapNo[i] = Integer.parseInt(atomMapStr[i]);
            }
        }


        for (int i = 0; i < mol.getAtoms(); i++) {
            if (atomMapNo[i] > -1) {
                mol.setAtomMapNo(i, atomMapNo[i], true);
            }
        }

        return mol;

    }

    private static String getAtomMapNumbersAsString(StereoMolecule mol) {
        StringBuilder mapping = new StringBuilder();

        for (int i = 0; i < mol.getAtoms(); i++) {
            mapping.append(mol.getAtomMapNo(i) + ",");
        }

        if (mapping.length() > 0) {
            mapping.setLength(mapping.length() - 1);
        }

        return mapping.toString();
    }
}
