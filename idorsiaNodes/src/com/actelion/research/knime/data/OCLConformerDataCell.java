package com.actelion.research.knime.data;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.NodeLogger;
import org.openmolecules.chem.conf.gen.ConformerGenerator;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.IDCodeParserWithoutCoordinateInvention;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.contrib.HydrogenHandler;
import com.actelion.research.chem.io.CompoundTableConstants;


/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class OCLConformerDataCell extends DataCell implements OCLConformerDataValue {

	public static final DataType TYPE = DataType.getType(OCLConformerDataCell.class);	
    private static final OCLConformerDataCellSerializer SERIALIZER = new OCLConformerDataCellSerializer();
    
    private static NodeLogger LOG = NodeLogger.getLogger(OCLConformerDataCell.class);
	
	
	
	private final StereoMolecule molecule;	
	private final String         idCode;
	private final String         coordinates;
	
	
	public OCLConformerDataCell(StereoMolecule m) {
		
		ConformerGenerator.addHydrogenAtoms(m);
		
		Canonizer c =new Canonizer(m,Canonizer.COORDS_ARE_3D);
		idCode      = c.getIDCode();
		coordinates = c.getEncodedCoordinates();
		//HydrogenHandler.addImplicitHydrogens(m);
		
		
//		System.out.println("IDCode original: "+idCode);
//		System.out.println("IDCode withImplicitHydrogen: "+m.getIDCode());
		molecule    = m;
		
	}
	
	
	
	public OCLConformerDataCell(String idCode, String coordinates) {
		this.idCode = idCode;
		this.coordinates = coordinates;
		IDCodeParserWithoutCoordinateInvention p = new IDCodeParserWithoutCoordinateInvention();		
		StereoMolecule mol = new StereoMolecule();		
		p.parse(mol, this.idCode, this.coordinates);
		//HydrogenHandler.addImplicitHydrogens(mol);
//		System.out.println("IDCode original: "+idCode);
//		System.out.println("IDCode withImplicitHydrogen: "+mol.getIDCode());
		molecule = mol;
	}

	
    public static final OCLConformerDataCellSerializer getCellSerializer() {
        return SERIALIZER;
    }
	
	
	
	
	@Override
	public String getStringValue() {			
		String sep = " :: ";
		return this.idCode + sep + this.coordinates;
	}

	@Override
	public StereoMolecule getMolecule2D() {
		// TODO Auto-generated method stub
		return molecule;
	}

	@Override
	public StereoMolecule getMolecule3D() {
		// TODO Auto-generated method stub
		return molecule;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getStringValue();
	}
	
	

	@Override
	public String getIDCode() {
		// TODO Auto-generated method stub
		return idCode;
	}

	@Override
	public String getCoordinatesIDCode() {
		// TODO Auto-generated method stub
		return coordinates;
	}

	@Override
	protected boolean equalsDataCell(DataCell dc) {
		
		if(dc instanceof OCLConformerDataCell) {
			return ((OCLConformerDataCell)dc).getIDCode().equals( this.getIDCode() ) && ((OCLConformerDataCell)dc).getCoordinatesIDCode().equals( this.getCoordinatesIDCode() );  
		}
		
		return false;	
	}

	@Override
	public int hashCode() {		
		return (this.idCode+":x:"+this.getCoordinatesIDCode()).hashCode();
	}
	
	

}
