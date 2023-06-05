package com.actelion.research.knime.data;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.PheSAMolecule;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */


public class OCLPheSAMoleculeDataCellSerializer implements DataCellSerializer<OCLPheSAMoleculeDataCell> {

	
	
	@Override
	public void serialize(OCLPheSAMoleculeDataCell cell, DataCellDataOutput output) throws IOException {
		
		DescriptorHandlerShape dhs = new DescriptorHandlerShape();
		String encoded = dhs.encode(cell.getPhesaMolecule());		
		output.write( (encoded+"\n").getBytes() );		
	}

	@Override
	public OCLPheSAMoleculeDataCell deserialize(DataCellDataInput input) throws IOException {
       
        String phesaString = input.readLine();		
		
		DescriptorHandlerShape dhs = new DescriptorHandlerShape();			
		PheSAMolecule p            = dhs.decode(phesaString);		  
			
		//StereoMolecule m = p.getMolecule();		
		return new OCLPheSAMoleculeDataCell(p);
	}

}
