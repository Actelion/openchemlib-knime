package com.actelion.research.knime.utils;

import com.actelion.research.chem.io.CompoundTableConstants;
import com.actelion.research.chem.io.DWARFileParser;
import com.actelion.research.chem.io.DWARFileParser.SpecialField;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureFileHelpers {

    public static List<String> extractColumnNames(File file) {
        List<String> columnNames = new ArrayList<String>();
        MultiStructureCompoundParserAdapter parser = Utils.getCompoundFileParser(file);

        if (parser == null) {
            return columnNames;
        }

        String[] fieldNames = parser.getFieldNames();

        for (String fieldName : fieldNames) {
            columnNames.add(fieldName);
        }
        
        parser.close();
        return columnNames;
    }
    
    /**
     * Returns the columnId of the parent column as key, and a map from column name to SpecialType. 
     * 
     * NOTE:  
     * 
     * @param file
     * @return
     */
    public static Map<String,Map<String,DWARFileParser.SpecialField>> extractStructuresWithAdditionalInformation(File file) {    	
    	
    	Map<String,Map<String,DWARFileParser.SpecialField>> result = new HashMap<>();
    	
    	DWARFileParser p = new DWARFileParser(file);    
    	
    	Map<String,SpecialField> specialFields = p.getSpecialFieldMap();
    	
    	// we sort just by parent.
    	// This helps to construct all parent relationships. Even if they are over multiple levels we can
    	// correctly assemble all of them.
    	Map<String,Map<String,SpecialField>> sortedSpecialFields = new HashMap<>();
    	
    	// find out if there is a list of constants for values of SpecialField::type
    	// --> CompoundTableConstants.java
    	// relevant constants:
    	// CompoundTableConstants.cColumnTypeIDCode
    	// 
    	
    	// find structures and corresponding special fields that we are interested in
    	List<String> molecule_columns = new ArrayList<>();
    	
    	for(String idi : specialFields.keySet()) {
    		if( specialFields.get(idi).type.equals( CompoundTableConstants.cColumnTypeIDCode ) ) {
    			// TODO: (maybe at least..) find out if we have a structure or reaction ID code..
    			molecule_columns.add(idi);
    		}
    		
    		if( specialFields.get(idi).parent != null ) {
    			System.out.println("special_field added: "+specialFields.get(idi).name);    			
    			String parent = specialFields.get(idi).parent; 
    			if( ! sortedSpecialFields.containsKey( parent ) ) {
    				sortedSpecialFields.put(parent,new HashMap<>());
    			}
    			sortedSpecialFields.get(parent).put(specialFields.get(idi).name,specialFields.get(idi));
    		}    		    		    		    	
    	}
    	
    	// Assemble all molecule structures..    	
    	for(String moli : molecule_columns) {
    		if(sortedSpecialFields.get(moli)==null) {
    			result.put(moli, new HashMap<>() );
    		}
    		else {
    			result.put(moli, sortedSpecialFields.get(moli) );
    		}
    	}
    	    	    
    	p.close();
    	    	    
    	return result; 
    }
}
