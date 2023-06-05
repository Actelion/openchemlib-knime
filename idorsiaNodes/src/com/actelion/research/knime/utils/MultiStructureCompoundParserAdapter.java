package com.actelion.research.knime.utils;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.chem.io.CompoundFileParser;
import com.actelion.research.chem.io.DWARFileParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class MultiStructureCompoundParserAdapter {

    private CompoundFileParser parser;
    private List<Structure> structures = new ArrayList<>();
    private List<Descriptor> descriptors = new ArrayList<>();

    public MultiStructureCompoundParserAdapter(CompoundFileParser parser) {
        this.parser = parser;
        init();
    }

    public static void main(String[] args) {
        DWARFileParser dwarFileParser = new DWARFileParser("/Users/finkt/data/knime/multiDesc.dwar");
        MultiStructureCompoundParserAdapter compoundParserAdapter = new MultiStructureCompoundParserAdapter(dwarFileParser);
    }

    public int getStructureCountPerEntry() {
        return structures.size();
    }

    public int getDescriptorCountPerEntry() {
        return descriptors.size();
    }

    public Structure getStructureSummary(int idx) {
        return structures.get(idx);
    }

    public Descriptor getDescriptorSummary(int idx) {
        return descriptors.get(idx);
    }


    public String getIDCode(String columnName) {
        if (parser instanceof DWARFileParser) {
            return getIDCodeFromDwarFile(columnName);
        }
        return parser.getIDCode();

    }


    public String getDescriptorValueAsString(String parentColumnName, DescriptorInfo descriptorInfo) {
        if (parser instanceof DWARFileParser) {
            return getDescriptorValueAsStringFromDWAR(parentColumnName, descriptorInfo.shortName);
        }
        return null;

    }

    public String getCoordinates(String parentColumnName) {
        if (parser instanceof DWARFileParser) {
            return getCoordinatesFromDwarFile(parentColumnName);
        }
        return parser.getCoordinates();
    }

    public StereoMolecule getMolecule(String columnName) {
        if (parser instanceof DWARFileParser) {
            return getMoleculeFromDwarFile(columnName);
        }
        return parser.getMolecule();
    }

    public String[] getFieldNames() {
    	System.out.println("FieldNames in DWAR file:");
    	for(String fi : parser.getFieldNames()) {
    		System.out.println("FieldName:"+fi);
    	}    	    	
        return parser.getFieldNames();
    }

    public boolean next() {
        return parser.next();
    }

    public String getFieldData(int selectedFieldIdx) {
        return parser.getFieldData(selectedFieldIdx);
    }

    public void close() {
        parser.close();
    }


    private String getColumnName(DWARFileParser.SpecialField specialField) {
        return specialField.name;
    }

    private String getIDCodeFromDwarFile(String columnName) {
        DWARFileParser dwarFileParser = (DWARFileParser) this.parser;
        TreeMap<String, DWARFileParser.SpecialField> specialFieldMap = dwarFileParser.getSpecialFieldMap();
        Set<String> fieldNames = specialFieldMap.keySet();
        for (String fieldName : fieldNames) {
            DWARFileParser.SpecialField specialField = specialFieldMap.get(fieldName);
            String specialFieldName = getColumnName(specialField);
            if (isIdCode(specialField) && specialFieldName.equals(columnName)) {
                return dwarFileParser.getSpecialFieldData(specialField.fieldIndex);
            }
        }
        return null;
    }

    private String getCoordinatesFromDwarFile(String parentColumnName) {

        DWARFileParser dwarFileParser = (DWARFileParser) this.parser;
        TreeMap<String, DWARFileParser.SpecialField> specialFieldMap = dwarFileParser.getSpecialFieldMap();
        Set<String> fieldNames = specialFieldMap.keySet();
        for (String fieldName : fieldNames) {
            DWARFileParser.SpecialField specialField = specialFieldMap.get(fieldName);
            if (isCoordinates(specialField) && parentColumnName.equals(specialField.parent)) {
                return dwarFileParser.getSpecialFieldData(specialField.fieldIndex);
            }
        }
        return null;
    }

    private String getDescriptorValueAsStringFromDWAR(String parent, String shortName) {
        DWARFileParser dwarFileParser = (DWARFileParser) this.parser;
        TreeMap<String, DWARFileParser.SpecialField> specialFieldMap = dwarFileParser.getSpecialFieldMap();
        Set<String> fieldNames = specialFieldMap.keySet();
        for (String fieldName : fieldNames) {
            DWARFileParser.SpecialField specialField = specialFieldMap.get(fieldName);
            DescriptorInfo descriptorInfo = getDescriptorInfo(specialField);
            if (descriptorInfo != null && descriptorInfo.shortName.equalsIgnoreCase(shortName) && specialField.parent.equals(parent)) {
                return dwarFileParser.getSpecialFieldData(specialField.fieldIndex);
            }
        }
        return null;
    }


    private StereoMolecule getMoleculeFromDwarFile(String columnName) {
        String idCode = getIDCodeFromDwarFile(columnName);
        String coordinates = getCoordinatesFromDwarFile(columnName);
        if (idCode == null) {
            return null;
        }
        IDCodeParser idCodeParser = new IDCodeParser();
        StereoMolecule mol = new StereoMolecule();
        idCodeParser.parse(mol, idCode, coordinates);
        return mol;
    }

    private void init() {
        if (parser instanceof DWARFileParser) {
            TreeMap<String, DWARFileParser.SpecialField> specialFieldMap = ((DWARFileParser) parser).getSpecialFieldMap();
            if (specialFieldMap != null) {
                for (String fieldName : specialFieldMap.keySet()) {
                    DWARFileParser.SpecialField specialField = specialFieldMap.get(fieldName);
                    if (isIdCode(specialField)) {
                        structures.add(new Structure(specialField.name));
                    } else {
                        DescriptorInfo descriptorInfo = getDescriptorInfo(specialField);
                        if (descriptorInfo != null) {
                            descriptors.add(new Descriptor(specialField.name, descriptorInfo, specialField.parent));
                        }
                    }
                }
            }
        } else {
            structures.add(new Structure("Molecule"));
        }
    }

    private boolean isIdCode(DWARFileParser.SpecialField specialField) {
        return Objects.equals(specialField.type, "idcode");
    }

    private boolean isCoordinates(DWARFileParser.SpecialField specialField) {
        return Objects.equals(specialField.type, "idcoordinates2D");
    }

    private DescriptorInfo getDescriptorInfo(DWARFileParser.SpecialField specialField) {
        String type = specialField.type;
        for (DescriptorInfo descriptorInfo : DescriptorHelpers.getDescriptorInfos()) {
            if (Objects.equals(specialField.type, descriptorInfo.shortName)) {
                return descriptorInfo;
            }
        }
        return null;
    }

    public class Structure {
        private String columnName;

        public Structure(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }
    }

    public class Descriptor {
        private DescriptorInfo descriptorInfo;
        private String columnName;
        private String parentName;

        public Descriptor(String columnName, DescriptorInfo descriptorInfo, String parentName) {
            this.columnName = columnName;
            this.descriptorInfo = descriptorInfo;
            this.parentName = parentName;
        }

        public DescriptorInfo getDescriptorInfo() {
            return descriptorInfo;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getParentName() {
            return parentName;
        }
    }
}
