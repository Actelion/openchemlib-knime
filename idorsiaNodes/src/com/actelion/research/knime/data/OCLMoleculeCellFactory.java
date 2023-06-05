package com.actelion.research.knime.data;

import com.actelion.research.chem.StereoMolecule;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

public class OCLMoleculeCellFactory {

    /**
     * Type representing cells implementing the {@link OCLMoleculeDataValue}
     * interface
     *
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = OCLMoleculeDataCell.TYPE;

    //~--- methods ------------------------------------------------------------

    public static DataCell createOCLMoleculeCell(final StereoMolecule mol) {
        if (mol == null) {
            throw new NullPointerException("Mol value must not be null.");
        }

        return new OCLMoleculeDataCell(mol);
    }

    public static DataCell createOCLMoleculeCell(final String idCode) {
        return createOCLMoleculeCell(idCode, null, false);
    }

    public static DataCell createOCLMoleculeCell(final String idCode, final boolean fragment) {
        return createOCLMoleculeCell(idCode, null, fragment);
    }

    public static DataCell createOCLMoleculeCell(final String idCode, final String coordinates) {
        return createOCLMoleculeCell(idCode, coordinates, false);
    }

    public static DataCell createOCLMoleculeCell(final String idCode, final String coordinates, final boolean fragment) {
        if (idCode == null) {
            throw new NullPointerException("IDCode must not be null.");
        }

        return new OCLMoleculeAdapterCell(new OCLMoleculeDataCell(idCode, coordinates, fragment));
    }
}
