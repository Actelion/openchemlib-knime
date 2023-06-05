package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.utils.DescriptorHelpers;

public class OCLNewClusterMoleculesNodeSettings extends AbstractOCLNodeSettings {
	
	private static final String         CLUSTER_COLUMN_NAME               = "clusterColumnName";
    private static final String         CLUSTER_CUTOFF                    = "clusterCutoff";
    private static final String         DEFAULT_CLUSTER_COLUMN_NAME       = "Cluster No";
    private static final int            DEFAULT_CLUSTER_CUTOFF            = -1;
    private static final String         DEFAULT_INPUT_COLUMN_NAME         = "";
    private static final String         DEFAULT_REPRESENTATIV_COLUMN_NAME = "Is representative?";
    private static final float          DEFAULT_SIMILARITY_CUTOFF         = 0.8f;
    public  static final String         DESCRIPTOR_ID_A                   = "Descriptor_A";
    //private static final String         DESCRIPTOR                        = "descriptor";
    //private static final String         INPUT_COLUMN_NAME                 = "inputColumnName";
    //private static final DescriptorInfo DEFAULT_DESCRIPTOR                = DescriptorConstants.DESCRIPTOR_FFP512;
    private static final String         REPRESENTATIVE_COLUMN_NAME        = "representativeColumnName";
    private static final String         SIMILARITY_CUTOFF                 = "similarityCutoff";

    //~--- fields -------------------------------------------------------------

    private String         clusterColumnName;
    private int            clusterCutoff;
    //private DescriptorInfo descriptor;
    private DescriptorSettings descriptorA;
    //private String         inputColumnName;
    private String         representativeColumnName;
    private float          similarityCutoff;

    //~--- constructors -------------------------------------------------------

    public OCLNewClusterMoleculesNodeSettings() {
        //this.inputColumnName          = DEFAULT_INPUT_COLUMN_NAME;
        this.clusterCutoff            = DEFAULT_CLUSTER_CUTOFF;
        this.similarityCutoff         = DEFAULT_SIMILARITY_CUTOFF;
        //this.descriptor               = DEFAULT_DESCRIPTOR;
        this.clusterColumnName        = DEFAULT_CLUSTER_COLUMN_NAME;
        this.descriptorA = new DescriptorSettings();
        this.representativeColumnName = DEFAULT_REPRESENTATIV_COLUMN_NAME;
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
        settings.addInt(CLUSTER_CUTOFF, clusterCutoff);
        settings.addFloat(SIMILARITY_CUTOFF, similarityCutoff);
        //settings.addString(DESCRIPTOR, descriptor.shortName);
        settings.addString(CLUSTER_COLUMN_NAME, clusterColumnName);
        settings.addString(REPRESENTATIVE_COLUMN_NAME, representativeColumnName);       
        descriptorA.saveSettings(DESCRIPTOR_ID_A, settings);                
    }

    public String getClusterColumnName() {
        return clusterColumnName;
    }
    
    public void setDescriptorSettings(DescriptorSettings dst) {
    	this.descriptorA = dst;
    }

    public void setClusterColumnName(String clusterColumnName) {
        this.clusterColumnName = clusterColumnName;
    }

    public int getClusterCutoff() {
        return clusterCutoff;
    }

    public void setClusterCutoff(int clusterCutoff) {
        this.clusterCutoff = clusterCutoff;
    }

    public DescriptorInfo getDescriptor() {
        return this.descriptorA.getDescriptorInfo();
    }

    public String getInputColumnName() {
        return this.descriptorA.getColumnName();
    }

    public String getRepresentativeColumnName() {
        return representativeColumnName;
    }

    public void setRepresentativeColumnName(String representativeColumnName) {
        this.representativeColumnName = representativeColumnName;
    }

    public float getSimilarityCutoff() {
        return similarityCutoff;
    }

    public void setSimilarityCutoff(float similarityCutoff) {
        this.similarityCutoff = similarityCutoff;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        //this.inputColumnName          = settings.getString(INPUT_COLUMN_NAME, DEFAULT_INPUT_COLUMN_NAME);
        this.clusterCutoff            = settings.getInt(CLUSTER_CUTOFF, DEFAULT_CLUSTER_CUTOFF);
        this.similarityCutoff         = settings.getFloat(SIMILARITY_CUTOFF, DEFAULT_SIMILARITY_CUTOFF);
        //this.descriptor               = DescriptorHelpers.getDescriptorInfoByShortName(settings.getString(DESCRIPTOR,
        //        DEFAULT_DESCRIPTOR.shortName));
        this.representativeColumnName = settings.getString(REPRESENTATIVE_COLUMN_NAME, DEFAULT_REPRESENTATIV_COLUMN_NAME);
        this.clusterColumnName        = settings.getString(CLUSTER_COLUMN_NAME, DEFAULT_CLUSTER_COLUMN_NAME);
        this.descriptorA.loadSettings(DESCRIPTOR_ID_A,settings);
    }

}
