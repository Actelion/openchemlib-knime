package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.knime.data.ScaffoldType;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLScaffoldAnalysisNodeSettings extends AbstractOCLNodeSettings {
    private static final String INPUT_COLUMN_NAME = "inputColumnName";
    private static final String SCAFFOLD_TYPE     = "scaffoldType";

    //~--- fields -------------------------------------------------------------

    private String       inputColumnName;
    private ScaffoldType scaffoldType = ScaffoldType.RING_SYSTEMS;

    //~--- constructors -------------------------------------------------------

    public OCLScaffoldAnalysisNodeSettings() {
        this.inputColumnName = "";
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
        settings.addString(SCAFFOLD_TYPE, scaffoldType.toString());
    }

    //~--- get methods --------------------------------------------------------

    public String getInputColumnName() {
        return inputColumnName;
    }

    public ScaffoldType getScaffoldType() {
        return scaffoldType;
    }

    //~--- set methods --------------------------------------------------------

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    public void setScaffoldType(ScaffoldType scaffoldType) {
        this.scaffoldType = scaffoldType;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");
        this.scaffoldType    = ScaffoldType.fromString(settings.getString(SCAFFOLD_TYPE, ScaffoldType.RING_SYSTEMS.toString()));
    }
}
