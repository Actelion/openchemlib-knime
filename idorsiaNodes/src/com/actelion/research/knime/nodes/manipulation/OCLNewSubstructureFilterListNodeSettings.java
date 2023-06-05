package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

public class OCLNewSubstructureFilterListNodeSettings  extends AbstractOCLNodeSettings {
    private static final String INPUT_COLUMN_NAME = "inputColumnName";
    private static final String FILTER_COLUMN_NAME = "filterColumnName";
    
    private static final String INPUT_HAS_FFP = "inputHasFFP";
    private static final String FILTER_HAS_FFP = "filterHasFFP";
    
    public static final String DESCRIPTOR_INPUT_ID  = "descriptor_input";
    public static final String DESCRIPTOR_FILTER_ID = "descriptor_filter";
    

    //~--- fields -------------------------------------------------------------

    private DataCell defaultValue = DataType.getMissingCell();
    private DataCell mapMissingTo = DataType.getMissingCell();

    private String inputColumnName;
    private String filterColumnName;
    
    private boolean hasDescriptorInput = false;
    private boolean hasDescriptorFilter = false;
    
    private DescriptorSettings descriptorSettingsInput  = null;
    private DescriptorSettings descriptorSettingsFilter = null;
    

    //~--- constructors -------------------------------------------------------

    public OCLNewSubstructureFilterListNodeSettings() {
        this.inputColumnName = "";
        this.filterColumnName = "";
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public void loadSettingsForDialog(NodeSettingsRO settings) {
        loadSettings(settings);
    }

    @Override
    public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
        loadSettings(settings);
    }
    
    public void setDescriptorInput(DescriptorSettings dss_input) {
    	if(dss_input==null) {
    		this.hasDescriptorInput=false;
    		this.descriptorSettingsInput=null;
    	}
    	else {
    		this.hasDescriptorInput=true;
    		this.descriptorSettingsInput=dss_input;
    	}
    }
    
    public void setDescriptorFilter(DescriptorSettings dss_filter) {
    	if(dss_filter==null) {
    		this.hasDescriptorFilter=false;
    		this.descriptorSettingsFilter=null;
    	}
    	else {
    		this.hasDescriptorFilter=true;
    		this.descriptorSettingsFilter=dss_filter;
    	}
    }
    

    @Override
    public void saveSettings(NodeSettingsWO settings) {
        settings.addString(INPUT_COLUMN_NAME, inputColumnName);
        settings.addString(FILTER_COLUMN_NAME, filterColumnName);
        settings.addBoolean(INPUT_HAS_FFP, hasDescriptorInput);
        settings.addBoolean(FILTER_HAS_FFP, hasDescriptorFilter);
        
        if(this.hasDescriptorInput) {
        	this.descriptorSettingsInput.saveSettings(DESCRIPTOR_INPUT_ID, settings);
        }
        if(this.hasDescriptorFilter) {
        	this.descriptorSettingsFilter.saveSettings(DESCRIPTOR_FILTER_ID, settings);
        }
    }

    //~--- get methods --------------------------------------------------------

    public String getInputColumnName() {
        return inputColumnName;
    }

    public String getFilterColumnName() {
        return filterColumnName;
    }
    
    public boolean hasFFP_Input() {
    	return this.hasDescriptorInput;
    }
    
    public boolean hasFFP_Filter() {
    	return this.hasDescriptorFilter;
    }
    
    public String getFFPColumn_Input() {
    	if(!this.hasFFP_Input()) {
    		return null;
    	}
    	else {
    		return this.descriptorSettingsInput.getColumnName();
    	}
    }

    public String getFFPColumn_Filter() {
    	if(!this.hasFFP_Filter()) {
    		return null;
    	}
    	else {
    		return this.descriptorSettingsFilter.getColumnName();
    	}
    }
    
    //~--- set methods --------------------------------------------------------


    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    public void setFilterColumnName(String filterColumnName) {
        this.filterColumnName = filterColumnName;
    }

    public void setDescriptorSettings_Input(DescriptorSettings dss) {
    	this.descriptorSettingsInput = dss;
    }
    
    public void setDescriptorSettings_Filter(DescriptorSettings dss) {
    	this.descriptorSettingsFilter = dss;
    }
    
    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");
        this.filterColumnName = settings.getString(FILTER_COLUMN_NAME, "");
        this.hasDescriptorInput  = settings.getBoolean(INPUT_HAS_FFP,false);
        this.hasDescriptorFilter = settings.getBoolean(FILTER_HAS_FFP,false);
        
        if(this.hasDescriptorInput) {
        	if(this.descriptorSettingsInput==null) {this.descriptorSettingsInput = new DescriptorSettings();}
        	this.descriptorSettingsInput.loadSettings(DESCRIPTOR_INPUT_ID, settings);
        }
        if(this.hasDescriptorFilter) {
        	if(this.descriptorSettingsFilter==null) {this.descriptorSettingsFilter = new DescriptorSettings();}
        	this.descriptorSettingsFilter.loadSettings(DESCRIPTOR_FILTER_ID, settings);
        }
    }
}
