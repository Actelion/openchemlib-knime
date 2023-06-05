package com.actelion.research.knime.nodes;

import com.actelion.research.knime.data.OCLDataHelper;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;

public abstract class OCLNodeDialogPane extends NodeDialogPane {

    @Override
    protected final void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        DataTableSpec[] convertedSpecs = OCLDataHelper.convertInputTables(specs);
        initFromSettings(settings, convertedSpecs, specs);
    }

    protected abstract void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs_output, DataTableSpec[] specs_input) throws NotConfigurableException;
}
