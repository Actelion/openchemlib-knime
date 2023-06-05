package com.actelion.research.knime.nodes;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

public abstract class AbstractOCLNodeSettings implements IOCLNodeSettings {
    private DataCell defaultValue;
    private DataCell missingValue;

    //~--- constructors -------------------------------------------------------

    public AbstractOCLNodeSettings() {
        this(DataType.getMissingCell(), DataType.getMissingCell());
    }

    public AbstractOCLNodeSettings(DataCell defaultValue, DataCell missingValue) {
        this.defaultValue = defaultValue;
        this.missingValue = missingValue;
    }

    //~--- get methods --------------------------------------------------------

    @Override
    public DataCell getDefaultValue() {
        return defaultValue;
    }

    @Override
    public DataCell getMissingValue() {
        return missingValue;
    }

    //~--- set methods --------------------------------------------------------

    @Override
    public void setDefaultValue(DataCell defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void setMissingValue(DataCell missingValue) {
        this.missingValue = missingValue;
    }
}
