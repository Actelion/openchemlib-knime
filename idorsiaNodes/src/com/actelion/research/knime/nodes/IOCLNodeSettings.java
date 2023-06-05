package com.actelion.research.knime.nodes;

import org.knime.core.data.DataCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public interface IOCLNodeSettings {
    void loadSettingsForDialog(NodeSettingsRO settings);

    void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException;

    void saveSettings(NodeSettingsWO settings);

    DataCell getDefaultValue();

    DataCell getMissingValue();

    void setDefaultValue(DataCell dataCell);

    void setMissingValue(DataCell mapMissingTo);
}
