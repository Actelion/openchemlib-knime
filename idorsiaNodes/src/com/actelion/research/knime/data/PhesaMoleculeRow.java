package com.actelion.research.knime.data;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.RowKey;

import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.PheSAMolecule;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class PhesaMoleculeRow {
    private String   rowKey;
    private PheSAMolecule phesaMolecule;

    public PhesaMoleculeRow(RowKey rowKey, PheSAMolecule phesaMolecule) {
    	// TODO: find most efficient way to copy PhesaMolecule objects..
    	DescriptorHandlerShape dhs = new DescriptorHandlerShape();
    	this.rowKey = rowKey.getString();
    	this.phesaMolecule = dhs.decode( dhs.encode(phesaMolecule) );
    }

    public String getRowKey() {
        return rowKey;
    }
    
    public PheSAMolecule getPhesaMolecule() {
    	return this.phesaMolecule;
    }
    
}
