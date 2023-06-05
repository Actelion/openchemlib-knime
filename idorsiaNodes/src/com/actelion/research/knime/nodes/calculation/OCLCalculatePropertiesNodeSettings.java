package com.actelion.research.knime.nodes.calculation;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLCalculatePropertiesNodeSettings extends AbstractOCLNodeSettings {
    private static final String INPUT_COLUMN_NAME = "inputColumnName";
    private static final String CALCULATE_PROPERTIES = "calculateProperties";
    private static final String CALCULATE_ALL_PROPERTIES = "calculateAllProperties";
    private static final boolean[] DEFAULT_CALCULATE_PROPERTIES = new boolean[OCLCalculatePropertiesNodeModel.propertyTable.size()];

    //~--- fields -------------------------------------------------------------

    private IDCodeParser idCodeParser = new IDCodeParser();
    private DataCell defaultValue = DataType.getMissingCell();
    private DataCell mapMissingTo = DataType.getMissingCell();
    private String inputColumnName;
    private boolean calculateAllProperties;
    private boolean[] calculateProperties;


    //~--- constructors -------------------------------------------------------

    public OCLCalculatePropertiesNodeSettings() {
//        this.fragment        = new StereoMolecule();
        this.inputColumnName = "";
        this.calculateProperties = DEFAULT_CALCULATE_PROPERTIES;
        this.calculateAllProperties = false;
//        this.descriptor      = DescriptorConstants.DESCRIPTOR_FFP512;
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

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");
        this.calculateAllProperties = settings.getBoolean(CALCULATE_ALL_PROPERTIES, false);
        this.calculateProperties = settings.getBooleanArray(CALCULATE_PROPERTIES, new boolean[OCLCalculatePropertiesNodeModel.propertyTable.size()]);
    }

    @Override
    public void saveSettings(NodeSettingsWO settings) {
        settings.addString(INPUT_COLUMN_NAME, inputColumnName);
        settings.addBoolean(CALCULATE_ALL_PROPERTIES, false);
        settings.addBooleanArray(CALCULATE_PROPERTIES, calculateProperties);
    }

    //~--- get methods --------------------------------------------------------


    public String getInputColumnName() {
        return inputColumnName;
    }

    //~--- set methods --------------------------------------------------------


    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    public boolean calculateProperty(int idx) {
        return calculateProperties[idx];
    }

    public boolean calculateProperty(String propertyName) {
        int idx = OCLCalculatePropertiesNodeModel.propertyTable.indexOf(propertyName);
        return calculateProperty(idx);
    }

    public void setCalculateProperties(boolean[] calculateProperties) {
        this.calculateProperties = calculateProperties;
    }

    public boolean[] getCalculateProperties() {
        return calculateProperties;
    }


    public boolean isCalculateAllProperties() {
        return calculateAllProperties;
    }

    public void setCalculateAllProperties(boolean calculateAllProperties) {
        this.calculateAllProperties = calculateAllProperties;
    }
}
