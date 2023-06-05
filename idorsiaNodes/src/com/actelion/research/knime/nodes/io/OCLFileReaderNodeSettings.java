package com.actelion.research.knime.nodes.io;

import com.actelion.research.chem.io.DWARFileParser;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

import java.io.File;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLFileReaderNodeSettings extends AbstractOCLNodeSettings {
    private static final String INPUT_FILE                = "inputFile";
    private static final String SELECTED_COLUMNS          = "selectedColumns";
    private static final String SELECTED_COLUMNS_SPECIAL  = "selectedSpecialColumns";
    private static final String INCLUDE_ALL_COLUMNS       = "includeAllColumns";

    //~--- fields -------------------------------------------------------------

    private DataCell defaultValue    = DataType.getMissingCell();
    private DataCell mapMissingTo    = DataType.getMissingCell();
    private String   inputFile       = "";
    private boolean includeAllColumns = false;
    private String[] selectedColumns        = new String[0];
    private String[] selectedColumnsSpecial = new String[0];

    //~--- methods ------------------------------------------------------------

    @Override
    public void loadSettingsForDialog(NodeSettingsRO settings) {
        loadSettings(settings);
    }

    @Override
    public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
        loadSettings(settings);
    }

    @Override
    public void saveSettings(NodeSettingsWO settings) {
        settings.addStringArray(SELECTED_COLUMNS, selectedColumns);
        settings.addStringArray(SELECTED_COLUMNS_SPECIAL, selectedColumnsSpecial);
        settings.addString(INPUT_FILE, inputFile);
        settings.addBoolean(INCLUDE_ALL_COLUMNS, includeAllColumns);
    }

    //~--- get methods --------------------------------------------------------

    public String getInputFile() {
        return inputFile;
    }

    public String[] getSelectedColumns(boolean include_all) {
    	if(include_all) {
    		DWARFileParser p = new DWARFileParser( new File(inputFile));
    		String[] names = p.getFieldNames();
    		return names;
    	}
        return selectedColumns;
    }

    public String[] getSelectedColumnsSpecial(boolean include_all) {
    	if(include_all) {
    		DWARFileParser p = new DWARFileParser( new File(inputFile));
    		String[] names = p.getSpecialFieldMap().keySet().stream().toArray( String[]::new );
    		return names;
    	}
        return selectedColumnsSpecial;
    }
        
    //~--- set methods --------------------------------------------------------

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public void setSelectedColumns(String[] selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    public void setSelectedColumnsSpecial(String[] selectedColumnsSpecial) {
        this.selectedColumnsSpecial = selectedColumnsSpecial;
    }

    public void setSelectedColumnsSpecial(String[] selectedColumnsSpecial_a, Map<String,String> resolve_names) {
        String[] selectedColumnsSpecial = new String[selectedColumnsSpecial_a.length];
        for(int zi=0;zi<selectedColumnsSpecial.length;zi++) {
        	System.out.println("selected col special to resolve : " + selectedColumnsSpecial_a[zi] );
        	System.out.println("selected col special: "+resolve_names.get( selectedColumnsSpecial_a[zi]));        	
        	selectedColumnsSpecial[zi] = resolve_names.get( selectedColumnsSpecial_a[zi] ) ;
        }
        setSelectedColumnsSpecial(selectedColumnsSpecial);
    }

    
    public boolean isIncludeAllColumns() {
        return includeAllColumns;
    }

    public void setIncludeAllColumns(boolean includeAllColumns) {
        this.includeAllColumns = includeAllColumns;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.selectedColumns         = settings.getStringArray(SELECTED_COLUMNS, new String[0]);
        this.selectedColumnsSpecial  = settings.getStringArray(SELECTED_COLUMNS_SPECIAL, new String[0]);
        this.inputFile               = settings.getString(INPUT_FILE, "");
        this.includeAllColumns       = settings.getBoolean(INCLUDE_ALL_COLUMNS, false);
    }
}
