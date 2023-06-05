package com.actelion.research.knime.utils;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLConformerListDataValue;
import com.actelion.research.knime.data.OCLDescriptorDataCell;
import com.actelion.research.knime.data.OCLDescriptorDataValue;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.data.OCLPheSAMoleculeDataCell;
import com.actelion.research.knime.data.OCLPheSAMoleculeDataValue;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.doublevector.DoubleVectorCellFactory;
import org.knime.core.node.InvalidSettingsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SpecHelper {

    public static final String COLUMN_ID = "columnId";
    public static final String PARENT_COLUMN_ID = "parentColumnId";
    public static final String DESCRIPTOR_INFO = "descriptorInfo";
    //public static final String PROPERTY_DESCRIPOR_COLUMN = "OCLDescriptor";
    
    public static final String CONFORMER_COLUMN_ID = "conformerColumnId";

    public static int getColumnIndex(DataTableSpec dataTableSpec, String columnName) {
        for (int a = 0; a < dataTableSpec.getNumColumns(); a++) {
            if (dataTableSpec.getColumnSpec(a).getName().equals(columnName)) {
                return a;
            }
        }
        return -1;
    }

    public static DataColumnSpec createColumnSpec(String columnTitle, DataType type, DataColumnProperties properties) {
        DataColumnSpecCreator dataColumnSpecCreator = new DataColumnSpecCreator(columnTitle, type);
        if (properties != null) {
            dataColumnSpecCreator.setProperties(properties);
        }
        return dataColumnSpecCreator.createSpec();
    }

    public static DataColumnSpec createDoubleColumnSpec(String columnTitle) {
        return createColumnSpec(columnTitle, DoubleCell.TYPE, null);
    }
    
    public static DataColumnSpec createDoubleVectorColumnSpec(String columnTitle) {
    	return createColumnSpec(columnTitle, DoubleVectorCellFactory.TYPE, null);
    }

    public static DataColumnSpec createMoleculeColumnSpec(String columnTitle, DataType type) {
        Map<String, String> propMap = new HashMap<>();
        propMap.put(COLUMN_ID, UUID.randomUUID().toString());
        DataColumnProperties properties = new DataColumnProperties(propMap);
        return createColumnSpec(columnTitle, type, properties);

    }
        
    public static DataColumnSpec createMoleculeColumnSpec(String columnTitle) {
        Map<String, String> propMap = new HashMap<>();
        propMap.put(COLUMN_ID, UUID.randomUUID().toString());
        DataColumnProperties properties = new DataColumnProperties(propMap);
        return createColumnSpec(columnTitle, OCLMoleculeDataCell.TYPE, properties);

    }
    
    public static DataColumnSpec createPhesaMoleculeColumnSpec(String columnTitle) {
        Map<String, String> propMap = new HashMap<>();
        propMap.put(COLUMN_ID, UUID.randomUUID().toString());
        DataColumnProperties properties = new DataColumnProperties(propMap);
        return createColumnSpec(columnTitle, OCLPheSAMoleculeDataCell.TYPE, properties);    	
    }

    public static DataColumnSpec createConformerSetColumnSpec(String columnTitle, DataColumnSpec parentColumnSpec) { //throws InvalidSettingsException {
        Map<String, String> propMap = new HashMap<>();
        propMap.put(COLUMN_ID, UUID.randomUUID().toString());
        if(parentColumnSpec!=null) {
        	propMap.put(PARENT_COLUMN_ID, parentColumnSpec.getProperties().getProperty(COLUMN_ID));
        }
        else {
        	propMap.put(PARENT_COLUMN_ID, "");
        	//throw new InvalidSettingsException("Problem with conformer column "+columnTitle+" Please also include the corresponding Structure column!" );
        }
        DataColumnProperties properties = new DataColumnProperties(propMap);  
        //System.out.println("Spec: "+ (columnTitle==null) +" , "+ (OCLConformerSetDataCell.TYPE==null) +" , "+ (properties==null) );
        //System.out.println("Spec: "+ (columnTitle==null) +" , "+ (properties==null) );        
        return createColumnSpec(columnTitle, OCLConformerListDataCell.TYPE, properties);
    }    
    
    public static DataColumnSpec createDescriptorColumnSpec(DescriptorInfo descriptorInfo, DataColumnSpec parentColumnSpec) {
    	return createDescriptorColumnSpec(descriptorInfo,parentColumnSpec,null);
    }
    
    public static DataColumnSpec createDescriptorColumnSpec(DescriptorInfo descriptorInfo, DataColumnSpec parentColumnSpec, DataColumnSpec conformerColumnSpec) {
        Map<String, String> propMap = new HashMap<>();
        propMap.put(DESCRIPTOR_INFO, descriptorInfo.shortName);
        if(parentColumnSpec.getProperties().containsProperty(COLUMN_ID)) {
        	propMap.put(PARENT_COLUMN_ID, parentColumnSpec.getProperties().getProperty(COLUMN_ID));
        }
        else {
        	// no parent..
        	propMap.put(PARENT_COLUMN_ID, "");
        }
        if(conformerColumnSpec==null) {
        	propMap.put(CONFORMER_COLUMN_ID, "" );
        }
        else {
        	propMap.put(CONFORMER_COLUMN_ID, conformerColumnSpec.getProperties().getProperty(COLUMN_ID));
        }
        DataColumnProperties properties = new DataColumnProperties(propMap);
        return createColumnSpec(descriptorInfo.shortName + " [" + parentColumnSpec.getName() + "]", OCLDescriptorDataCell.TYPE, properties);
    }

    public static DataColumnSpec createStringColumnSpec(String column) {
        return createColumnSpec(column, StringCell.TYPE, null);
    }

    public static DataColumnSpec createIntColumnSpec(String column) {
        return createColumnSpec(column, IntCell.TYPE, null);
    }

    public static DataColumnSpec createBooleanColumnSpec(String column) {
        return createColumnSpec(column, BooleanCell.TYPE, null);
    }

    public static String getUniqueColumnName(DataTableSpec dataTableSpec, String columnName) {
        return DataTableSpec.getUniqueColumnName(dataTableSpec, columnName);
    }

    public static boolean isMoleculeSpec(DataColumnSpec columnSpec) {
        DataType type = columnSpec.getType();
        if (type.isCompatible(OCLMoleculeDataValue.class)) {
            return true;
        }
        if (type.isCompatible(SdfValue.class)) {
            return true;
        }
        if (type.isCompatible(MolValue.class)) {
            return true;
        }
        return false;
    }

    public static boolean isDescriptorSpec(DataColumnSpec columnSpec) {
        DataType type = columnSpec.getType();
        if (type.isCompatible(OCLDescriptorDataValue.class)) {
            return true;
        }
        return false;
    }
    
    public static boolean isConformerListSpec(DataColumnSpec columnSpec) {
        DataType type = columnSpec.getType();
        System.out.println("isConformerListSpec(..) type = "+type.getName() + " isCompatible: "+type.isCompatible(OCLConformerListDataValue.class));        
        if (type.isCompatible(OCLConformerListDataValue.class)) {
            return true;
        }
        return false;    	
    }
    
    public static boolean isPhesaMoleculeSpec(DataColumnSpec columnSpec) {
        DataType type = columnSpec.getType();            
        System.out.println("isPhesaMoleculeSpec(..) type = "+type.getName() + " isCompatible: "+type.isCompatible(OCLPheSAMoleculeDataValue.class));
        if (type.isCompatible(OCLPheSAMoleculeDataValue.class)) {
            return true;
        }
        return false;    	    	
    }


    public static boolean isDescriptorSpecOfType(DataColumnSpec columnSpec, DescriptorInfo descriptorInfo) {
        return isDescriptorSpec(columnSpec) && columnSpec.getProperties().getProperty(DESCRIPTOR_INFO).equalsIgnoreCase(descriptorInfo.shortName);
    }

    public static boolean isParentOf(DataColumnSpec childColumn, DataColumnSpec parentColumn) {
        return childColumn.getProperties().getProperty(PARENT_COLUMN_ID).equalsIgnoreCase(parentColumn.getProperties().getProperty(COLUMN_ID));
    }

    
    public static int getDescriptorColumnIndex(DataTableSpec tableSpecs, int moleculeColumnIdx, DescriptorInfo descriptorInfo) {
        int numColumns = tableSpecs.getNumColumns();
        for (int columnIdx = 0; columnIdx < numColumns; columnIdx++) {
            DataColumnSpec columnSpec = tableSpecs.getColumnSpec(columnIdx);
            if (SpecHelper.isDescriptorSpecOfType(columnSpec, descriptorInfo) && SpecHelper.isParentOf(columnSpec, tableSpecs.getColumnSpec(moleculeColumnIdx))) {
                return columnIdx;
            }
        }
        return -1;
    }

    public static DataColumnSpec getParentColumnSpec(DataTableSpec tableSpecs, DataColumnSpec columnSpec) {
        String parentColumnId = columnSpec.getProperties().getProperty(PARENT_COLUMN_ID);
        for (int i = 0; i < tableSpecs.getNumColumns(); i++) {
            DataColumnSpec possibleMatch = tableSpecs.getColumnSpec(i);
            String columnId = possibleMatch.getProperties().getProperty(COLUMN_ID);
            if (Objects.equals(columnId, parentColumnId)) {
                return possibleMatch;
            }
        }
        return null;
    }

    public static List<DataColumnSpec> getMoleculeColumnSpecs(DataTableSpec inputSpecs) {
        List<DataColumnSpec> result = new ArrayList<>();
        for (int i = 0; i < inputSpecs.getNumColumns(); i++) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(i);
            if (isMoleculeSpec(columnSpec)) {
                result.add(columnSpec);
            }
        }
        return result;
    }

    public static List<DataColumnSpec> getDescriptorColumnSpecs(DataTableSpec inputSpecs) {
        List<DataColumnSpec> result = new ArrayList<>();
        for (int i = 0; i < inputSpecs.getNumColumns(); i++) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(i);
            if (isDescriptorSpec(columnSpec)) {
                result.add(columnSpec);
            }
        }
        return result;
    }
    
    public static List<DataColumnSpec> getDescriptorColumnSpecs(DataTableSpec inputSpecs, DataColumnSpec moleculeColumnSpec) {
        List<DataColumnSpec> result = new ArrayList<>();
        for (int i = 0; i < inputSpecs.getNumColumns(); i++) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(i);
            if (isDescriptorSpec(columnSpec) && getParentColumnSpec(inputSpecs, columnSpec) == moleculeColumnSpec) {
                result.add(columnSpec);
            }
        }
        return result;
    }
    
    public static List<DataColumnSpec> getDescriptorColumnSpecsNew(DataTableSpec inputSpecs) {
    	List<DataColumnSpec> result = new ArrayList<>();
        for (int i = 0; i < inputSpecs.getNumColumns(); i++) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(i);
            if (isDescriptorSpec(columnSpec) ) {
                result.add(columnSpec);
            }
        }
        return result;
    }
    
    public static List<DataColumnSpec> getConformerListColumnSpecs(DataTableSpec inputSpecs) {
        List<DataColumnSpec> result = new ArrayList<>();
        System.out.println("Number of input columns: "+inputSpecs.getNumColumns());
        for (int i = 0; i < inputSpecs.getNumColumns(); i++) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(i);
            if (isConformerListSpec(columnSpec)) {
            	System.out.println("add Col!");
                result.add(columnSpec);
            }
        }
        System.out.println( "return columns: " + result.size() );
        return result;
    }
    
    public static List<DataColumnSpec> getPhesaMoleculeColumnSpecs(DataTableSpec inputSpecs) {
        List<DataColumnSpec> result = new ArrayList<>();
        System.out.println("Number of input columns: "+inputSpecs.getNumColumns());
        for (int i = 0; i < inputSpecs.getNumColumns(); i++) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(i);
            if (isPhesaMoleculeSpec(columnSpec)) {
                result.add(columnSpec);
            }
        }
        System.out.println( "return columns: " + result.size() );
        return result;    	
    }
    
    
    /**
     * Keys in the outer map are the identifiers of the different 2d descriptors.
     * The inner map maps FROM the descriptor columns TO their parent columns.
     * 
     * @param inputSpecs
     * @return
     */
    public static Map<String,Map<DataColumnSpec,DataColumnSpec>> getAllDescriptorColumnsSorted(DataTableSpec inputSpecs) {
        
    	Map<String,Map<DataColumnSpec,DataColumnSpec>> sorted_descriptors = new HashMap<>();
    	
        for (int i = 0; i < inputSpecs.getNumColumns(); i++) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(i);
            if(isDescriptorSpec(columnSpec)) {
            	String shortInfo = columnSpec.getProperties().getProperty(DESCRIPTOR_INFO);
            	DataColumnSpec parent = getParentColumnSpec(inputSpecs, columnSpec);            		
            	
            	if(!sorted_descriptors.containsKey(shortInfo)) {
            		sorted_descriptors.put(shortInfo, new HashMap<>());
            	}
            	sorted_descriptors.get(shortInfo).put(columnSpec,parent);            	
            }
        }
        return sorted_descriptors;
    }

    public static DescriptorInfo getDescriptorInfo(DataColumnSpec columnSpec) {
        String shortInfo = columnSpec.getProperties().getProperty(DESCRIPTOR_INFO);
        if (shortInfo != null) {
            return DescriptorHelpers.getDescriptorInfoByShortName(shortInfo);
        }
        return null;
    }
}
