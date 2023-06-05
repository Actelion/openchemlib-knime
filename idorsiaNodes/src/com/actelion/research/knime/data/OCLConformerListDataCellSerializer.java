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

public class OCLConformerListDataCellSerializer implements DataCellSerializer<OCLConformerListDataCell>{

	String separator = " ";
	
	@Override
	public void serialize(OCLConformerListDataCell cell, DataCellDataOutput output) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		sb.append(cell.getIDCode());		
		for(String ci : cell.getCoordinateIDCodes()) {
			sb.append(separator);
			sb.append(ci);
		}
			
		output.write( (sb.toString() +"\n").getBytes());			
	}

	@Override
	public OCLConformerListDataCell deserialize(DataCellDataInput input) throws IOException {
		
		String    conformerset = input.readLine();
		String[]  splits = conformerset.split(separator); 
		
		String idCode         = splits[0];
		String[] coordinates  = new String[splits.length-1];
		for(int zi=0;zi<splits.length-1;zi++) { coordinates[zi] = splits[zi+1]; }
		
		return new OCLConformerListDataCell(idCode,coordinates);
	}
	
}
