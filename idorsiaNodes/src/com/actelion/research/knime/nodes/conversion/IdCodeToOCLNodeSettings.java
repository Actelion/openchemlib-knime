package com.actelion.research.knime.nodes.conversion;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class IdCodeToOCLNodeSettings extends AbstractOCLNodeSettings {
    private static final String  DEFAULT_NEW_COLUMN_NAME     = "Molecule";
    private static final String  DEFAULT_INPUT_COLUMN_NAME   = "";
    private static final String  NEW_COLUMN_NAME             = "newColumnName";
    private static final String  IDCODE_COLUMN_NAME          = "idCodeColumn";
    private static final String  REMOVE_INPUT_COLUMN         = "removeInputColumn";
    private static final boolean DEFAULT_REMOVE_INPUT_COLUMN = false;

    //~--- fields -------------------------------------------------------------

    private String  newColumnName;
    private String  inputColumnName;
    private boolean removeInputColumn;

    //~--- constructors -------------------------------------------------------

    public IdCodeToOCLNodeSettings() {
        this.inputColumnName = DEFAULT_INPUT_COLUMN_NAME;
        this.newColumnName   = DEFAULT_NEW_COLUMN_NAME;
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
        settings.addString(IDCODE_COLUMN_NAME, inputColumnName);
        settings.addString(NEW_COLUMN_NAME, newColumnName);
        settings.addBoolean(REMOVE_INPUT_COLUMN, removeInputColumn);
    }

    //~--- get methods --------------------------------------------------------

    public String getInputColumnName() {
        return inputColumnName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public boolean isRemoveInputColumn() {
        return removeInputColumn;
    }

    //~--- set methods --------------------------------------------------------

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public void setRemoveInputColumn(boolean removeInputColumn) {
        this.removeInputColumn = removeInputColumn;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName   = settings.getString(IDCODE_COLUMN_NAME, DEFAULT_INPUT_COLUMN_NAME);
        this.newColumnName     = settings.getString(NEW_COLUMN_NAME, DEFAULT_NEW_COLUMN_NAME);
        this.removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN, DEFAULT_REMOVE_INPUT_COLUMN);
    }
}
