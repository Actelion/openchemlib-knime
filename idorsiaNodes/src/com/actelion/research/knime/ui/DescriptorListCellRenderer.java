package com.actelion.research.knime.ui;

import com.actelion.research.chem.descriptor.DescriptorInfo;

import java.awt.*;

import javax.swing.*;

public class DescriptorListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if ((value != null) && (value instanceof DescriptorInfo)) {
            this.setText(((DescriptorInfo) value).shortName);
        } else {
            setText("-");
        }

        return this;
    }
}
