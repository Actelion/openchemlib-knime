package com.actelion.research.knime.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.conf.Conformer;
import com.actelion.research.chem.conf.ConformerSet;
import com.actelion.research.chem.phesa.DescriptorHandlerShapeOneConf;
import com.actelion.research.chem.phesa.MolecularVolume;
import com.actelion.research.chem.phesa.PheSAMolecule;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */


public class OCLPheSAMoleculeDataCell extends DataCell implements OCLPheSAMoleculeDataValue { //implements OCLPheSAMoleculeValue {

	//public static final DataType TYPE = DataType.getType(OCLConformerListDataCell.class, OCLConformerDataCell.TYPE );
	public static final DataType TYPE = DataType.getType(OCLPheSAMoleculeDataCell.class);
    private static final OCLPheSAMoleculeDataCellSerializer SERIALIZER = new OCLPheSAMoleculeDataCellSerializer();
        
    public static final DataType getCollectionType(final DataType elementType) {
    	return OCLConformerDataCell.TYPE;
    }
    
    private static NodeLogger LOG = NodeLogger.getLogger(OCLPheSAMoleculeDataCell.class);
	
	
	
	//private StereoMolecule stereoMolecule;
	
	private StereoMolecule  stereoMolecule;
	private PheSAMolecule   phesaMolecule;
	
	public OCLPheSAMoleculeDataCell( PheSAMolecule phesa ) {
		this.stereoMolecule = phesa.getMolecule();
		this.phesaMolecule  = phesa;
	}
	
	public OCLPheSAMoleculeDataCell( OCLConformerListDataValue conformer_list ) {
		
//		this.stereoMolecule = conformer_list.getMolecule2D();

//		if(conformer_list.size()==0) {
//			this.stereoMolecule = conformer_list.getMolecule2D();
//			this.phesaMolecule  = new PheSAMolecule(stereoMolecule,new ArrayList<>());
//			
//			return;
//		}
//		
//		this.stereoMolecule = conformer_list.getMolecule2D();
//		
//		ArrayList<MolecularVolume> mvs = new ArrayList<>();				
//		for( int zi = 0; zi < conformer_list.size(); zi++ ) {
//			StereoMolecule mi = conformer_list.getMolecule3D(zi);
//			mvs.add(new MolecularVolume(mi));
//		}
	
//		this.phesaMolecule = new PheSAMolecule(this.stereoMolecule,mvs); 
		this.stereoMolecule = conformer_list.getMolecule2D();
		
		ConformerSet cs = new ConformerSet();
		for(int ci = 0; ci < conformer_list.size(); ci++) {
			StereoMolecule smi = conformer_list.getMolecule3D(ci);
			try {
				smi.validate();
				System.out.println("Success! Validated PhesaInputConformer!");
			}
			catch(Exception ex) {
				System.out.println("Failed to validate PhesaInputConformer..");
				ex.printStackTrace();
			}
			cs.add( new Conformer( smi ) );
		}
		
		this.phesaMolecule = DescriptorHandlerShapeOneConf.getDefaultInstance().getThreadSafeCopy().createDescriptor(cs);
	}
	
	//@Override
	public StereoMolecule getMolecule2D() {
		return phesaMolecule.getMolecule();
	}

	//@Override
	public StereoMolecule getMolecule3D(int idx) {
		return phesaMolecule.getConformer(phesaMolecule.getVolumes().get(idx));
	}

	private OCLConformerListDataCell getAsConformerListDataCell() {
		StereoMolecule[] conformers = new StereoMolecule[this.size()];
		for(int zi=0;zi<this.size();zi++) {
			conformers[zi] = phesaMolecule.getConformer( phesaMolecule.getVolumes().get(zi) );
		}
		
		OCLConformerListDataCell dc = new OCLConformerListDataCell(stereoMolecule.getIDCode(),conformers);
		return dc;
	}
	
	//@Override
	public String getCoordinateIDCodesInOneString() {			
		return getAsConformerListDataCell().getCoordinateIDCodesInOneString();
	}

	//@Override
	public String getStringValue() {		
		return phesaMolecule.toString();
	}

	//@Override
	public PheSAMolecule getPhesaMolecule() {
		// TODO Auto-generated method stub
		return phesaMolecule;
	}

	//@Override
	public String toString() {
		// TODO Auto-generated method stub
		return phesaMolecule.toString();
	}

	//@Override
	protected boolean equalsDataCell(DataCell dc) {
		// TODO Auto-generated method stub
		if( !(dc instanceof OCLPheSAMoleculeDataValue) ) {
			return false;
		}
		
		OCLPheSAMoleculeDataValue pdc = (OCLPheSAMoleculeDataValue) dc;
		
		return false;
	}

	//@Override
	public int hashCode() {
		return ("ph"+phesaMolecule.toString()).hashCode();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return phesaMolecule.getVolumes().size();
	}

	@Override
	public Iterator<DataCell> iterator() {
		// TODO Auto-generated method stub
		return this.getAsConformerListDataCell().iterator();
	}

	
	
}
