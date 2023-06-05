package com.actelion.research.knime.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.ListCell;
import org.knime.core.node.NodeLogger;
import org.openmolecules.chem.conf.gen.ConformerGenerator;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.Coordinates;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.IDCodeParserWithoutCoordinateInvention;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.conf.AtomAssembler;
import com.actelion.research.chem.conf.Conformer;
import com.actelion.research.chem.conf.ConformerSet;
import com.actelion.research.chem.contrib.HydrogenHandler;
import com.actelion.research.knime.utils.SpecHelper;


/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */


/**
 * 
 * NOTE w.r.t. stereo chemistry: The ConformerListDataCell can be initialized with IDCodes that describe
 *                               racemates. In this case the StereoMolecules contained in this list
 *                               of conformers may have different IDCodes themselves, as they have
 *                               defined stereo chemistry. However, they are "compatible" with the
 *                               original IDCode, in the sense that they belong to a subset of structures 
 *                               described by the original IDCode.
 * 
 *
 */

public class OCLConformerListDataCell extends DataCell implements OCLConformerListDataValue , SdfValue , MolValue {

	//public static final DataType TYPE = DataType.getType(OCLConformerListDataCell.class, OCLConformerDataCell.TYPE );
	public static final DataType TYPE = DataType.getType(OCLConformerListDataCell.class);
    private static final OCLConformerListDataCellSerializer SERIALIZER = new OCLConformerListDataCellSerializer();
    
    
    public static final DataType getCollectionType(final DataType elementType) {
    	return OCLConformerDataCell.TYPE;
    }
    
    private static NodeLogger LOG = NodeLogger.getLogger(OCLConformerListDataCell.class);
	
	
	private String idCode;
	private String[] coordinates; 
	
	private StereoMolecule[] conformers;	
	
	public OCLConformerListDataCell(String idCode, String[] coordinates) {
		this.idCode = idCode;
		this.coordinates = coordinates;
			
		if(true) {
			LOG.debug("Init OCLConformerSetDataCell..");
			LOG.debug("IDCode = "+idCode);					
			for(int zi=0;zi<coordinates.length;zi++) {
				LOG.debug("Coords: "+coordinates[zi]);
			}
		}
		
		// init conformers field from data
		IDCodeParserWithoutCoordinateInvention p = new IDCodeParserWithoutCoordinateInvention();
		
		StereoMolecule sm_proto = new StereoMolecule();
//		p.parse(sm_proto,idCode);		
		
		conformers = new StereoMolecule[coordinates.length];
		for(int zi=0;zi<coordinates.length;zi++) {
			//StereoMolecule smi = new StereoMolecule();
			StereoMolecule smi = new StereoMolecule();
			p.parse(smi, this.idCode, this.coordinates[zi]);
			smi.ensureHelperArrays(StereoMolecule.cHelperCIP);
			
//			if( !testAllHydrogensExplicit(smi) ) {
//				LOG.info("Implicit Hydrogens! --> we will fix this!");
//				LOG.info("getAllAtoms: "+smi.getAllAtoms()+" getAtoms: "+smi.getAtoms());				
//				//ConformerGenerator.addHydrogenAtoms(smi);
//				HydrogenHandler.addImplicitHydrogens(smi);
// 			LOG.info("tried to add hydrogens.. getAllAtoms: "+smi.getAllAtoms()+" getAtoms: "+smi.getAtoms());				
//				LOG.debug("Create new coordinates IDCode because of new hydrogen atoms!");
//				
//				Canonizer c = new Canonizer(smi,Canonizer.COORDS_ARE_3D);
//                this.coordinates[zi] = c.getEncodedCoordinates(true);				
//			}			
//			HydrogenHandler.addImplicitHydrogens(smi);
//			coordinates[zi] = smi.getCanonizer().getEncodedCoordinates(true);
			
			try {
				smi.ensureHelperArrays(StereoMolecule.cHelperCIP);
				validateDistances(smi);			
				smi.validate();
				//System.out.println("Conformer successfully validated!");
			}
			catch(Exception ex) {
				System.out.println("Validation of conformer failed..");
				System.out.println( "idcode      = "+smi.getIDCode() );
				System.out.println( "coordinates = "+smi.getIDCoordinates() );
				ex.printStackTrace();
			}						
			
			validateDistances(smi);						
			
			conformers[zi] = smi;
		}				
	}
	
	/**
	 * Takes a single stereo molecule and assumes that the StereoMolecule has
	 * 3D coordinates. This initializes a OCLConformerSet of size 1. 
	 * 
	 * @param mol
	 * @return
	 */
	public OCLConformerListDataCell(StereoMolecule mol) {
		this(mol.getIDCode(),new StereoMolecule[] {mol});
	}
	
		
	/**
	 * Takes multiple stereo molecules and assumes that the StereoMolecule all
	 * share the same IDCode and have 3D coordinates.
	 * 
	 * NOTE: Reason why we need the idcode: the different StereoMolecule objects may have
	 * different IDCodes, due to different stereo chemistry features. We currently do
	 * not have a procedure to automatically determine the most precise stereo chemistry
	 * description compatible with all supplied molecules.
	 * 
	 * @param mol
	 * @return
	 */	
	public OCLConformerListDataCell(String idcode, StereoMolecule[] mol) {
		IDCodeParser p = new IDCodeParser();
					
		LOG.debug("Init new OCLConformerListDataCell of size: "+mol.length);
		
		// sanity check that is WRONG: "all id codes must be the same" THIS IS NOT TRUE, as generated conformers of
		// IDCodes with undefined stereo chemistry will have a different IDCode.
		
		this.idCode = idcode; //mol[0].getIDCode();		
		this.conformers = new StereoMolecule[mol.length];
		
		//for(int zi=1;zi<mol.length;zi++) {
		for(int zi=0;zi<mol.length;zi++) {
			StereoMolecule smi = new StereoMolecule(mol[zi]);
			
			smi.ensureHelperArrays(StereoMolecule.cHelperCIP);
			
			if( !testAllHydrogensExplicit(smi) ) {
				System.out.println("Implicit Hydrogens! --> we will fix this!");
				LOG.info("Implicit Hydrogens! --> we will fix this!");
				LOG.info("getAllAtoms: "+smi.getAllAtoms()+" getAtoms: "+smi.getAtoms());				
				HydrogenHandler.addImplicitHydrogens(smi);
				//ConformerGenerator.addHydrogenAtoms(smi);
				LOG.info("tried to add hydrogens.. getAllAtoms: "+smi.getAllAtoms()+" getAtoms: "+smi.getAtoms());				
				//LOG.debug("Create new coordinates IDCode because of new hydrogen atoms!");
				//Canonizer c = new Canonizer(smi);
				//this.coordinates[zi] = c.getEncodedCoordinates();
                //this.coordinates[zi] = smi.getIDCoordinates();	
			}			
//			if( ! smi.getIDCode().equals(this.idCode) ) {
//				throw new Error("Invalid ConformerSet with different structures..");
//			}
			
			try {
				
				validateDistances(smi);
				
				smi.validate();
				System.out.println("Conformer successfully validated!");
			}
			catch(Exception ex) {
				System.out.println("Validation of conformer failed..");
				ex.printStackTrace();
			}			
			
			conformers[zi] = smi;
		}
		
		// init Conformers IDCodes:		
	    this.coordinates = new String[conformers.length];
		for(int zi=0;zi<conformers.length;zi++) {
			//Canonizer c = new Canonizer(mol[zi],Canonizer.COORDS_ARE_3D);
			Canonizer c = new Canonizer(conformers[zi],Canonizer.COORDS_ARE_3D);
			String sci = c.getEncodedCoordinates(true);					
			coordinates[zi] = sci;
		}	
		LOG.debug("Initialized new OCLConformerListDataCell of size: "+mol.length+ " this.size() = "+this.size());
		
		if(false) {
			System.out.println("conformerlist data: \n");
			System.out.println("idcode= "+this.idCode);
			for(int zi=0;zi<coordinates.length;zi++) { System.out.println("coord_" + zi +" : "+this.coordinates[zi]); }
		}
	}
	
    public static final OCLConformerListDataCellSerializer getCellSerializer() {
        return SERIALIZER;
    }
	
	
	
	
	@Override
	public String toString() {
		
		String separator_a = " ";
		String separator_b = " ";
		
		String result = ""+idCode+separator_a;	
		for(String coi : coordinates) {
			result += coi;
		}		
		return result;
	}

	@Override
	protected boolean equalsDataCell(DataCell dc) {
		
		if(dc instanceof OCLConformerListDataCell) {
			OCLConformerListDataCell ocs = (OCLConformerListDataCell) dc;			
			if(ocs.getIDCode().equals(getIDCode() ) ) {
				
				Set<String> coords_a = new HashSet<>( Arrays.asList( this.getCoordinateIDCodes() ) );
				Set<String> coords_b = new HashSet<>( Arrays.asList( ocs.getCoordinateIDCodes() ) );
				
				return coords_a.equals(coords_b);
			}
			else {
				return false;
			}			
		}
		else {
			return false;
		}			
		
	}

	//@Override
	public int hashCode() {		
		return toString().hashCode();
	}
		
	//@Override
	public DataCell get(int index) {
		return new OCLConformerDataCell(this.idCode , coordinates[index] );
		//return new OCLConformerListDataCell(this.idCode , new String[] {coordinates[index]} );
	}

	/**
	 * Type of list are also OCLConformerSet objects (just of size one)
	 */
	//@Override
	public DataType getElementType() {		
		return OCLConformerDataCell.TYPE;
	}

	//@Override
	public int size() {		
		return coordinates.length;
	}

	//@Override
	public boolean containsBlobWrapperCells() {
		return false;
	}

	//@Override
	public Iterator<DataCell> iterator() {
	
		Iterator<OCLConformerListDataCell> it = new Iterator<OCLConformerListDataCell>() {
			int conformer_idx = 0;			
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return conformer_idx < coordinates.length;
			}

			@Override
			public OCLConformerListDataCell next() {
				OCLConformerListDataCell cell_i = (OCLConformerListDataCell) get(conformer_idx);
				conformer_idx++;
				return cell_i;
			}
			
		};
		
		return null;
	}

	@Override
	public String getStringValue() {
		return this.toString();
	}

	public String getIDCode() { return idCode; }
	
	public String[] getCoordinateIDCodes() { return coordinates; }
	
	@Override
	public StereoMolecule getMolecule2D() {
		
		String idCodeString = new String(idCode);
		
        IDCodeParser idCodeParser = new IDCodeParser();
        StereoMolecule mol = new StereoMolecule();
        idCodeParser.parse(mol, idCodeString);
        
        //mol.ensureHelperArrays(StereoMolecule.cHelperCIP);
        //HydrogenHandler.addImplicitHydrogens(mol);
        
        return mol;				
	}
	
	public ConformerSet getConformerSet() {
		ConformerSet cs = new ConformerSet();
		for(StereoMolecule si : this.conformers) {
			cs.add( new Conformer(si) );
		}
		return cs;
	}
	
	@Override
	public StereoMolecule getMolecule3D(int idx) {		
		String idCodeString = new String(idCode);
		
        IDCodeParserWithoutCoordinateInvention idCodeParser = new IDCodeParserWithoutCoordinateInvention();                      
        
        //System.out.println("getMolecule3D idcode: "+idCodeString+ " coordinates: "+this.coordinates[idx]);
        
        StereoMolecule mol = new StereoMolecule();
        idCodeParser.parse(mol, idCodeString, this.coordinates[idx] );
        
        if(!OCLConformerListDataCell.testAllHydrogensExplicit(mol)) {
        	throw new Error("IDCodeParserWithoutCoordinateInvention created StereoMolecule with implicit hydrogen atoms!!?");
        	//LOG.info("OCLConformerList::getMolecule3D(int idx)");
        	//AtomAssembler asa = new AtomAssembler(mol);
        	//asa.addImplicitHydrogens();
        	
        	
        }
                
        //HydrogenHandler.addImplicitHydrogens(mol);
        
        return mol;						
	}


    @Override
    public String getCoordinateIDCodesInOneString() {
    	
    	if(this.coordinates.length<=0) {
    		return "";
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.coordinates[0]);
    	for(int zi=1;zi<this.coordinates.length;zi++) {
    		sb.append(" "); // TODO!! FIGURE OUT WHAT WE REALLY HAVE TO USE HERE (e.g. which DWARConstants constant..)
    		sb.append(coordinates[zi]);
    	}
    	
    	return sb.toString();
    }
	
    
    public static boolean testAllHydrogensExplicit(StereoMolecule mol) {
    	mol.ensureHelperArrays(StereoMolecule.cHelperCIP);
    	
    	if(true) {
            // count hydrogens:
            int implicitHydrogens = 0;
            int explicitHydrogens = 0;
            int plainHydrogens    = 0;
            for(int zi=0;zi<mol.getAtoms();zi++) {
                implicitHydrogens += mol.getImplicitHydrogens(zi);
                explicitHydrogens += mol.getExplicitHydrogens(zi);
                plainHydrogens    += mol.getPlainHydrogens(zi);
            }
            //System.out.println("Number of implicit hydrogens (by counting): "+implicitHydrogens);
            //System.out.println("Number of explicit hydrogens (by counting): "+explicitHydrogens);
            //System.out.println("Number of plain    hydrogens (by counting): "+plainHydrogens);
    	}
    	
    	int implicitHydrogens = 0;
    	for(int at=0;at<mol.getAtoms();at++) {
    		implicitHydrogens += mol.getImplicitHydrogens(at);
    	}
    	boolean testA = implicitHydrogens==0;
    	return testA;
//    	boolean testB = (mol.getAllAtoms()==mol.getAtoms());
//    	
//    	if(testA!=testB) {
//    		System.out.println("PROBLEM?! testA != testB");
//    	}
//    	
//    	return testB;
    }

    
    private StereoMolecule getFirstConformerAsStereoMolecule() {
		StereoMolecule sm = new StereoMolecule();				
		if(this.size()==0) {
			LOG.warn("Export OCLConformerList to Mol WITHOUT 3D COORDINTATES! (there are none..)");
			IDCodeParser p = new IDCodeParser();
			p.parse(sm, this.idCode);
		}
		else {
			if(this.size()>1) {
				LOG.warn("Export OCLConformerList to Mol of size > 1 --> losing conformers.. size = "+this.size());
			}
			
			IDCodeParserWithoutCoordinateInvention p = new IDCodeParserWithoutCoordinateInvention();
			p.parse(sm, this.idCode, this.coordinates[0]);			
		}		    	
    	
    	return sm;
    }
    
	@Override
	public String getMolValue() {		
		StereoMolecule sm = getFirstConformerAsStereoMolecule();	

        MolfileCreator molfileCreator = new MolfileCreator(sm);
        return molfileCreator.getMolfile();		
	}

	@Override
	public String getSdfValue() {
		StereoMolecule sm = getFirstConformerAsStereoMolecule();
		
		MolfileCreator molfileCreator = new MolfileCreator(sm);
        return molfileCreator.getMolfile() + "\n$$$$";				
	}
	

	public static void validateDistances(StereoMolecule sm) {
		double avbl = sm.getAverageBondLength();
		double minDistanceSquare = avbl * avbl / 16.0;
		for (int atom1=1; atom1<sm.getAllAtoms(); atom1++) {
			for (int atom2=0; atom2<atom1; atom2++) {
				Coordinates co1 = sm.getCoordinates(atom1);
				Coordinates co2 = sm.getCoordinates(atom2);
				double xdif = co2.x - co1.x;
				double ydif = co2.y - co1.y;
				double zdif = co2.z - co1.z;
				double squared_dist = (xdif*xdif + ydif*ydif + zdif*zdif);
				if ( squared_dist < minDistanceSquare) {
					System.out.println("Distance between "+atom1+" and "+atom2+" is too small -> "+ squared_dist+
							" instead of at least "+minDistanceSquare);
				}		
				else {
					//System.out.println("D_ok: "+atom1+" <-> "+atom2+"  = "+squared_dist);
				}
			}
		}
	}

}
