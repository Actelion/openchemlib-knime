package com.actelion.research.knime.ui;

import org.knime.core.data.DataColumnSpec;

import javax.swing.*;
import java.awt.*;

public class DataColumnSpecListCellRenderer extends DefaultListCellRenderer {

    private final String emptyLabel;

    public DataColumnSpecListCellRenderer() {
        this("");
    }

    public DataColumnSpecListCellRenderer(String emptyLabel) {
        this.emptyLabel = emptyLabel;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value == null){
            this.setText(emptyLabel);
        }
        if (value instanceof DataColumnSpec) {
            DataColumnSpec columnSpec = (DataColumnSpec) value;
            this.setText(columnSpec.getName());
            this.setIcon(columnSpec.getType().getIcon());
        }

        return this;
    }
}
