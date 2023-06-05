package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.utils.DescriptorHelpers;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLDiverseSelectionNodeSettings extends AbstractOCLNodeSettings {
    private static final String DEFAULT_DIVERSITY_SELECTION_RANK = "Diversity Selection Rank";
    private static final String DEFAULT_EXCLUSION_COLUMN_NAME = "";
    private static final String DEFAULT_INPUT_COLUMN_NAME = "";
    private static final int DEFAULT_NO_COMPOUNDS = 1000;
    private static final String DESCRIPTOR = "descriptor";
    private static final String DIVERSITY_RANK_COLUMN_NAME = "diversityRankColumnName";
    private static final String EXCLUSION_COLUMN_NAME = "exclusionColumnName";
    private static final String INPUT_COLUMN_NAME = "inputColumnName";
    private static final String NUMBER_OF_CPDS = "numberOfCompounds";
    private static final DescriptorInfo DEFAULT_DESCRIPTOR = DescriptorConstants.DESCRIPTOR_FFP512;
    
    //public static final String DESCRIPTOR_ID_A                   = "Descriptor_A";

    //~--- fields -------------------------------------------------------------

    private DescriptorInfo descriptor;
    //private DescriptorSettings descriptorSettings;
    private String diversityRankColumnName;
    private String exclusionColumnName;
    private String inputColumnName;
    private int noOfCompounds;

    //~--- constructors -------------------------------------------------------

    public OCLDiverseSelectionNodeSettings() {
        this.inputColumnName = DEFAULT_INPUT_COLUMN_NAME;
        this.exclusionColumnName = DEFAULT_EXCLUSION_COLUMN_NAME;
        this.descriptor = DEFAULT_DESCRIPTOR;
        //this.descriptorSettings = new DescriptorSettings();
        this.noOfCompounds = DEFAULT_NO_COMPOUNDS;
        this.diversityRankColumnName = DEFAULT_DIVERSITY_SELECTION_RANK;
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
        settings.addString(INPUT_COLUMN_NAME, inputColumnName);
        settings.addString(EXCLUSION_COLUMN_NAME, exclusionColumnName);
        settings.addInt(NUMBER_OF_CPDS, noOfCompounds);
        settings.addString(DESCRIPTOR, descriptor.shortName);
        //this.descriptorSettings.saveSettings(DESCRIPTOR_ID_A, settings);
        settings.addString(DIVERSITY_RANK_COLUMN_NAME, diversityRankColumnName);
    }

    //~--- get methods --------------------------------------------------------

    public DescriptorInfo getDescriptor() {
        return this.descriptor;
    }

    public String getDiversityRankColumnName() {
        return diversityRankColumnName;
    }

    public String getExclusionColumnName() {
        return exclusionColumnName;
    }

    public String getInputColumnName() {
        return inputColumnName;
    }

    public int getNoOfCompounds() {
        return noOfCompounds;
    }

    //~--- set methods --------------------------------------------------------

    public void setDescriptor(DescriptorInfo descriptor) {
        this.descriptor = descriptor;
    }

    public void setDiversityRankColumnName(String diversityRankColumnName) {
        this.diversityRankColumnName = diversityRankColumnName;
    }

    public void setExclusionColumnName(String exclusionColumnName) {
        this.exclusionColumnName = exclusionColumnName;
    }

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }
    
    //public void setDescriptorSettings(DescriptorSettings dsc) {
    //	this.descriptorSettings = dsc;
    //}

    public void setNoOfCompounds(int noOfCompounds) {
        this.noOfCompounds = noOfCompounds;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, DEFAULT_INPUT_COLUMN_NAME);
        this.exclusionColumnName = settings.getString(EXCLUSION_COLUMN_NAME, DEFAULT_EXCLUSION_COLUMN_NAME);
        this.noOfCompounds = settings.getInt(NUMBER_OF_CPDS, DEFAULT_NO_COMPOUNDS);
        this.diversityRankColumnName = settings.getString(DIVERSITY_RANK_COLUMN_NAME, DEFAULT_DIVERSITY_SELECTION_RANK);
        //this.descriptorSettings.loadSettings(DESCRIPTOR_ID_A, null);
        this.descriptor = DescriptorHelpers.getDescriptorInfoByShortName(settings.getString(DESCRIPTOR,
                DEFAULT_DESCRIPTOR.shortName));
    }
}
