package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.utils.DescriptorHelpers;

public class OCLNewDiverseSelectionNodeSettings extends AbstractOCLNodeSettings {
	
	private static final String DEFAULT_DIVERSITY_SELECTION_RANK = "Diversity Selection Rank";
    private static final String DEFAULT_EXCLUSION_COLUMN_NAME = "";
    private static final String DEFAULT_INPUT_COLUMN_NAME = "";
    private static final int DEFAULT_NO_COMPOUNDS = 1000;
    private static final String DESCRIPTOR_TYPE = "descriptorType";
    private static final String DIVERSITY_RANK_COLUMN_NAME = "diversityRankColumnName";
    //private static final String EXCLUSION_COLUMN_NAME = "exclusionColumnName";
    //private static final String INPUT_COLUMN_NAME = "inputColumnName";
    public  static final String         DESCRIPTOR_ID_A                   = "DescriptorA";
    public  static final String         DESCRIPTOR_ID_B                   = "DescriptorB";
    public  static final String		DESCRIPTOR_B_ACTIVE       	        = "DescrB_Active"; 
    private static final String NUMBER_OF_CPDS = "numberOfCompounds";
    private static final DescriptorInfo DEFAULT_DESCRIPTOR = DescriptorConstants.DESCRIPTOR_FFP512;

    //~--- fields -------------------------------------------------------------

    //private DescriptorInfo descriptor;
    private String descriptorType;
    private DescriptorSettings descriptorA;
    private DescriptorSettings descriptorB;
    private String diversityRankColumnName;
    //private String exclusionColumnName;
    //private String inputColumnName;
    
    private boolean descriptorB_Active;
    private int noOfCompounds;

    //~--- constructors -------------------------------------------------------

    public OCLNewDiverseSelectionNodeSettings() {
        //this.inputColumnName = DEFAULT_INPUT_COLUMN_NAME;
        //this.exclusionColumnName = DEFAULT_EXCLUSION_COLUMN_NAME;
        this.descriptorType = DEFAULT_DESCRIPTOR.shortName;
        this.noOfCompounds = DEFAULT_NO_COMPOUNDS;
        this.diversityRankColumnName = DEFAULT_DIVERSITY_SELECTION_RANK;
        this.descriptorA = new DescriptorSettings();
        this.descriptorB = new DescriptorSettings();
        this.descriptorB_Active = false;
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

    @Override
    public void saveSettings(NodeSettingsWO settings) {
        //settings.addString(INPUT_COLUMN_NAME, inputColumnName);
        //settings.addString(EXCLUSION_COLUMN_NAME, exclusionColumnName);
    	this.descriptorA.saveSettings(DESCRIPTOR_ID_A,settings);
    	settings.addBoolean(DESCRIPTOR_B_ACTIVE,descriptorB_Active);
    	if(descriptorB_Active) {
    		this.descriptorB.saveSettings(DESCRIPTOR_ID_B,settings);
    	}
        settings.addInt(NUMBER_OF_CPDS, noOfCompounds);
        settings.addString(DESCRIPTOR_TYPE, descriptorType);
        settings.addString(DIVERSITY_RANK_COLUMN_NAME, diversityRankColumnName);
    }

    //~--- get methods --------------------------------------------------------

    public DescriptorInfo getDescriptor() {
        return this.descriptorA.getDescriptorInfo();
    }

    public String getDiversityRankColumnName() {
        return diversityRankColumnName;
    }

    public String getExclusionColumnName() {
    	if(!this.descriptorB_Active) {
    		return null;
    	}
        return this.descriptorB.getColumnName();
    }

    public String getInputColumnName() {
        return this.descriptorA.getColumnName();
    }

    public int getNoOfCompounds() {
        return noOfCompounds;
    }
    
    public boolean isDescriptorB_Active() {
    	return this.descriptorB_Active;
    }

    //~--- set methods --------------------------------------------------------

    
    public void setDescriptorType(String dsct) {
    	this.descriptorType = dsct;
    }
    
    public String getDescriptorType() {
    	return this.descriptorType;
    }
     
    public void setDescriptorA(DescriptorSettings descriptor) {
        this.descriptorA = descriptor;
        this.descriptorType = descriptor.getDescriptorInfo().shortName;
        
    }

    public void setDescriptorB(DescriptorSettings descriptor) {
        this.descriptorB = descriptor;
    }

    public void setDescriptorB_Active(boolean active) {
    	this.descriptorB_Active = active;
    }
    
    public void setDiversityRankColumnName(String diversityRankColumnName) {
        this.diversityRankColumnName = diversityRankColumnName;
    }

//    public void setExclusionColumnName(String exclusionColumnName) {
//        this.exclusionColumnName = exclusionColumnName;
//    }
//
//    public void setInputColumnName(String inputColumnName) {
//        this.inputColumnName = inputColumnName;
//    }

    public void setNoOfCompounds(int noOfCompounds) {
        this.noOfCompounds = noOfCompounds;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        //this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, DEFAULT_INPUT_COLUMN_NAME);
        //this.exclusionColumnName = settings.getString(EXCLUSION_COLUMN_NAME, DEFAULT_EXCLUSION_COLUMN_NAME);
    	this.descriptorA.loadSettings(DESCRIPTOR_ID_A, settings);
    	this.descriptorB_Active = settings.getBoolean(DESCRIPTOR_B_ACTIVE,false);
    	if(this.descriptorB_Active) {
    		if(this.descriptorB==null) {this.descriptorB = new DescriptorSettings();}
    		this.descriptorB.loadSettings(DESCRIPTOR_ID_B, settings);
    	}
    	else {
    		this.descriptorB = null;
    	}
        this.noOfCompounds = settings.getInt(NUMBER_OF_CPDS, DEFAULT_NO_COMPOUNDS);
        this.diversityRankColumnName = settings.getString(DIVERSITY_RANK_COLUMN_NAME, DEFAULT_DIVERSITY_SELECTION_RANK);

        if(this.descriptorA.getDescriptorInfo()!=null) {
        	this.descriptorType = this.descriptorA.getDescriptorInfo().shortName;
        }
        else {
        	this.descriptorType = null;
        }
    }

}
