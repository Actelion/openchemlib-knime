package com.actelion.research.knime.nodes.calculation;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings;

public class OCLNewCalculateSimilarityListNodeSettings extends AbstractOCLNodeSettings {
    //private static final String INPUT_COLUMN_NAME = "inputColumnName";
    //private static final String COMPARE_TO_COLUMN_NAME = "compareToColumnName";
    private static final String COLUMN_NAME = "columnName";
    private static final String DESCRIPTOR = "descriptor";
    public static final String DESCRIPTOR_COL_ID_A = "DescriptorA";
    public static final String DESCRIPTOR_COL_ID_B = "DescriptorB";
    
    private IDCodeParser idCodeParser = new IDCodeParser();

    //~--- fields -------------------------------------------------------------

    private DataCell defaultValue = DataType.getMissingCell();
    private DataCell mapMissingTo = DataType.getMissingCell();

    private DescriptorSettings descriptorA;
    private DescriptorSettings descriptorB;
    private String columnName;
    private DescriptorInfo descriptor;

    //~--- constructors -------------------------------------------------------

    public OCLNewCalculateSimilarityListNodeSettings() {
        this.descriptorA = new DescriptorSettings();
        this.descriptorB = new DescriptorSettings();
        this.columnName = "Similarity";
        this.descriptor = DescriptorConstants.DESCRIPTOR_FFP512;
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
        descriptorA.saveSettings(DESCRIPTOR_COL_ID_A,settings);
        descriptorB.saveSettings(DESCRIPTOR_COL_ID_B,settings);
        settings.addString(COLUMN_NAME, columnName);
        settings.addString(DESCRIPTOR, descriptor.shortName);
        
    }

    //~--- get methods --------------------------------------------------------


    public String getInputColumnName() {
        return descriptorA.getColumnName();
    }

    public void setInputColumnName(String inputColumnName) {
        this.descriptorA.setColumnName(inputColumnName);
    }

    //~--- set methods --------------------------------------------------------

    public void setDescriptorSettingsA(DescriptorSettings dst) {
    	this.descriptorA = dst;
    }
    
    public void setDescriptorSettingsB(DescriptorSettings dst) {
    	this.descriptorB = dst;
    }
    
    public String getCompareToColumnName() {
        return this.descriptorB.getColumnName();
    }

    public void setCompareToColumnName(String compareToColumnName) {
        this.descriptorB.setColumnName(compareToColumnName);
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public DescriptorInfo getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(DescriptorInfo descriptor) {
        this.descriptor = descriptor;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.descriptorA.loadSettings(DESCRIPTOR_COL_ID_A, settings);
        this.descriptorB.loadSettings(DESCRIPTOR_COL_ID_B, settings);
        this.columnName = settings.getString(COLUMN_NAME, "Similarity");
        String descriptorShortName = settings.getString(DESCRIPTOR, null);
        this.descriptor = DescriptorConstants.DESCRIPTOR_FFP512;
        if (descriptorShortName != null) {
            for (DescriptorInfo descriptorInfo : DescriptorConstants.DESCRIPTOR_EXTENDED_LIST) {
                if (descriptorInfo.shortName.equals(descriptorShortName)) {
                    this.descriptor = descriptorInfo;
                }
            }
        }
    }
}