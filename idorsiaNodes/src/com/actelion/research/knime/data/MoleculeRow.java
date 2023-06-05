package com.actelion.research.knime.data;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;

import org.knime.core.data.RowKey;

public class MoleculeRow {
    private String rowKey;
    private String idCode;

    public MoleculeRow(RowKey rowKey, String idCode) {
        this.rowKey = rowKey.getString();
        this.idCode = idCode;
    }

    public String getRowKey() {
        return rowKey;
    }

    public StereoMolecule getMolecule() {
        StereoMolecule stereoMolecule = new StereoMolecule();
        IDCodeParser parser = new IDCodeParser();
        parser.parse(stereoMolecule, idCode);
        return stereoMolecule;
    }
}