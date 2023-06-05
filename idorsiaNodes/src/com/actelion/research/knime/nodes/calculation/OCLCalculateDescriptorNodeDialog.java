/*
 * @(#)OCLCalculateDescriptorNodeDialog.java   15/10/27
 * 
 * Copyright (c) 2010-2011 Actelion Pharmaceuticals Ltd.
 *
 *  Gewerbestrasse 16, CH-4123 Allschwil, Switzerland
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information
 *  of Actelion Pharmaceuticals Ltd. ("Confidential Information").  You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Actelion Pharmaceuticals Ltd.
 *
 *  Author: finkt	
 */


package com.actelion.research.knime.nodes.calculation;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.ui.MultiSelectionPane;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;

import info.clearthought.layout.TableLayout;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLCalculateDescriptorNodeDialog extends NodeDialogPane {
    private final OCLCalculateDescriptorNodeSettings m_settings = new OCLCalculateDescriptorNodeSettings();
    private JComboBox<String> inputColumnComboBox;
    private MultiSelectionPane descriptorSelectionPane;
    private Map<String, List<String>> moleculeToDescriptorShortNameMap = new HashMap<>();

    //~--- constructors -------------------------------------------------------

    public OCLCalculateDescriptorNodeDialog() {
        this.addTab("Node settings", this.createDescriptorTab());
        inputColumnComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateDescriptors();
            }
        });
    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);

        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecs = specs[0];

        inputColumnComboBox.removeAllItems();
        descriptorSelectionPane.setSelection(this.m_settings.getSelectedDescriptors(), getAllDescriptors());
        moleculeToDescriptorShortNameMap.clear();
        List<DataColumnSpec> moleculeColumnSpecs = SpecHelper.getMoleculeColumnSpecs(inputSpecs);
        for (DataColumnSpec moleculeColumnSpec : moleculeColumnSpecs) {
            List<String> descriptorNames = new ArrayList<>();
            List<DataColumnSpec> descriptorColumnSpecs = SpecHelper.getDescriptorColumnSpecs(inputSpecs, moleculeColumnSpec);
            for (DataColumnSpec descriptorColumnSpec : descriptorColumnSpecs) {
                descriptorNames.add(SpecHelper.getDescriptorInfo(descriptorColumnSpec).shortName);
            }
            moleculeToDescriptorShortNameMap.put(moleculeColumnSpec.getName(), descriptorNames);
        }
        for (DataColumnSpec moleculeColumnSpec : moleculeColumnSpecs) {
            inputColumnComboBox.addItem(moleculeColumnSpec.getName());
        }
        inputColumnComboBox.setSelectedItem(selectedInputColumn);

    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.setSelectedDescriptors(descriptorSelectionPane.getSelectedItems());
        ;
        this.m_settings.saveSettings(settings);
    }

    private void updateDescriptors() {
        String moleculeColumnName = (String) inputColumnComboBox.getSelectedItem();
        List<String> newSelection = new ArrayList<>(Arrays.asList(descriptorSelectionPane.getSelectedItems()));
        List<String> availableDescriptors = getAllDescriptors();
        if (moleculeColumnName == null) {
            availableDescriptors.clear();
            newSelection.clear();
        } else {
            List<String> existingDescriptors = moleculeToDescriptorShortNameMap.get(moleculeColumnName);
            if (existingDescriptors != null) {
                availableDescriptors.removeAll(existingDescriptors);
                newSelection.removeAll(existingDescriptors);
            }
        }
        descriptorSelectionPane.setSelection(newSelection, availableDescriptors);
    }

    private List<String> getAllDescriptors() {
        List<String> result = new ArrayList<>();
        for (DescriptorInfo descriptorInfo : DescriptorHelpers.getDescriptorInfos()) {
            result.add(descriptorInfo.shortName);
        }
        return result;

    }

    private MultiSelectionPane createDescriptorSelectionPane() {
        return new MultiSelectionPane();
    }

    private JPanel createDescriptorTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4},
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4}
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<String>();
        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;

        descriptorSelectionPane = createDescriptorSelectionPane();
        p.add(new JLabel("Descriptor"), "1," + row);
        p.add(descriptorSelectionPane.getContentPane(), "3," + row);
        return p;
    }
}
