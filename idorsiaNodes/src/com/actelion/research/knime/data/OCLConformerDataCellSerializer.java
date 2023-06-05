package com.actelion.research.knime.data;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class OCLConformerDataCellSerializer implements DataCellSerializer<OCLConformerDataCell> {

	String separator = " ";
	
	@Override
	public void serialize(OCLConformerDataCell cell, DataCellDataOutput output) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		sb.append(cell.getIDCode());		
		sb.append(separator);
		sb.append(cell.getCoordinatesIDCode());
			
		output.write( (sb.toString() +"\n").getBytes());			
	}

	@Override
	public OCLConformerDataCell deserialize(DataCellDataInput input) throws IOException {		
		String    conformerset = input.readLine();
		String[]  splits = conformerset.split(separator); 		
		String idCode        = splits[0];
		String coordinates   = splits[1];				
		return new OCLConformerDataCell(idCode,coordinates);
	}	
	
	
}
