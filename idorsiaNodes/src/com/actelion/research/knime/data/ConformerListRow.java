package com.actelion.research.knime.data;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.RowKey;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.IDCodeParserWithoutCoordinateInvention;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.contrib.HydrogenHandler;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class ConformerListRow {
    private String   rowKey;
    private String   idCode;
    private String[] coordinates;

    public ConformerListRow(RowKey rowKey, String idCode, String[] coordinates) {
        this.rowKey = rowKey.getString();
        this.idCode = idCode;
        this.coordinates = coordinates;
    }

    public String getRowKey() {
        return rowKey;
    }
    
    public String getIDCode() {
    	return this.idCode;
    }

    public List<StereoMolecule> getConformers() {
        StereoMolecule stereoMolecule = new StereoMolecule();
        IDCodeParserWithoutCoordinateInvention parser = new IDCodeParserWithoutCoordinateInvention();
        parser.parse(stereoMolecule, idCode);
        
        List<StereoMolecule> conformers = new ArrayList<>();
        for(int zi=0;zi<this.coordinates.length;zi++) {
        	StereoMolecule si = new StereoMolecule();
        	parser.parse(si,idCode,coordinates[zi]);
        	//HydrogenHandler.addImplicitHydrogens(si);
        	conformers.add(si);
        }        
        return conformers;
    }

}
