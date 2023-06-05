package com.actelion.research.knime.data;

import com.actelion.research.chem.phesa.PheSAMolecule;
import com.actelion.research.knime.ui.OCLMoleculeValueRenderer;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */


public interface OCLPheSAMoleculeDataValue extends OCLConformerListDataValue {

	public PheSAMolecule getPhesaMolecule();
	
}
