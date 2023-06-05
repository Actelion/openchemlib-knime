package com.actelion.research.knime.data;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

import com.actelion.research.chem.StereoMolecule;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class OCLConformerListCellFactory {
	
	/**
	 * Type representing cells implementing the {@link OCLMoleculeDataValue}
	 * interface
	 *
	 * @see DataType#getType(Class)
	 */
	public static final DataType TYPE = OCLConformerListDataCell.TYPE;

//~--- methods ------------------------------------------------------------

	public static DataCell createOCLConformerSetCell(String idCode, String[] coordinates) {
		return new OCLConformerListDataCell(idCode, coordinates);
	}
		

}
