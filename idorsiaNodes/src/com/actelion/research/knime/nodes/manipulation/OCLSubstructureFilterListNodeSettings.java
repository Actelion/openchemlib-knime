package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLSubstructureFilterListNodeSettings extends AbstractOCLNodeSettings {
    private static final String INPUT_COLUMN_NAME = "inputColumnName";
    private static final String FILTER_COLUMN_NAME = "filterColumnName";

    //~--- fields -------------------------------------------------------------

    private DataCell defaultValue = DataType.getMissingCell();
    private DataCell mapMissingTo = DataType.getMissingCell();

    private String inputColumnName;
    private String filterColumnName;

    //~--- constructors -------------------------------------------------------

    public OCLSubstructureFilterListNodeSettings() {
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

    @Override
    public void saveSettings(NodeSettingsWO settings) {
        settings.addString(INPUT_COLUMN_NAME, inputColumnName);
        settings.addString(FILTER_COLUMN_NAME, filterColumnName);
    }

    //~--- get methods --------------------------------------------------------


    public String getInputColumnName() {
        return inputColumnName;
    }

    public String getFilterColumnName() {
        return filterColumnName;
    }

    //~--- set methods --------------------------------------------------------


    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    public void setFilterColumnName(String filterColumnName) {
        this.filterColumnName = filterColumnName;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");
        this.filterColumnName = settings.getString(FILTER_COLUMN_NAME, "");
    }
}
