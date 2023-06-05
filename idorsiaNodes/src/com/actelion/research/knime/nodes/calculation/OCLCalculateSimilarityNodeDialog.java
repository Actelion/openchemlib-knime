/*
 * @(#)OCLCalculateSimilarityNodeDialog.java   15/10/27
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

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.gui.JEditableStructureView;
import com.actelion.research.gui.clipboard.ClipboardHandler;
import com.actelion.research.knime.nodes.DescriptorDependentNodeDialog;
import com.actelion.research.knime.ui.DescriptorListCellRenderer;

import info.clearthought.layout.TableLayout;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import java.awt.*;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLCalculateSimilarityNodeDialog extends DescriptorDependentNodeDialog {
    private final OCLCalculateSimilarityNodeSettings m_settings = new OCLCalculateSimilarityNodeSettings();
    private JComboBox<String> inputColumnComboBox;
    private JTextField mColumnNameTextField;
    private JComboBox<DescriptorInfo> descriptorComboBox;
    private JEditableStructureView mStructureView;

    //~--- constructors -------------------------------------------------------

    public OCLCalculateSimilarityNodeDialog() {
        this.addTab("Similarity settings", this.createSimilarityTab());
        init(inputColumnComboBox, descriptorComboBox);
    }

    @Override
    protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs, DataTableSpec[] specs_in) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        mStructureView.structureChanged(this.m_settings.getFragment());
        mColumnNameTextField.setText(this.m_settings.getColumnName());

        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecs = specs[0];

        populateMoleculeInputBox(inputSpecs);

        inputColumnComboBox.setSelectedItem(selectedInputColumn);
        descriptorComboBox.setSelectedItem(this.m_settings.getDescriptor());
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setFragment(mStructureView.getMolecule());
        this.m_settings.setColumnName(mColumnNameTextField.getText());
        this.m_settings.setDescriptor((DescriptorInfo) descriptorComboBox.getSelectedItem());
        if(descriptorComboBox.getSelectedItem()==null) {
        	throw new InvalidSettingsException("No descriptor selected.");
        }
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.saveSettings(settings);
    }


    private JPanel createSimilarityTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4}, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 2, TableLayout.FILL,
                4
        }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<String>();
        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;
        mColumnNameTextField = new JTextField(10);
        p.add(new JLabel("New column name"), "1," + row);
        p.add(mColumnNameTextField, "3," + row);
        row += 2;
        descriptorComboBox = new JComboBox<DescriptorInfo>();
        descriptorComboBox.setRenderer(new DescriptorListCellRenderer());
//        populateCompoBox();
        p.add(new JLabel("Used descriptor"), "1," + row);
        p.add(descriptorComboBox, "3," + row);
        row += 2;

        StereoMolecule fragment = new StereoMolecule();

        mStructureView = new JEditableStructureView(fragment);
        mStructureView.setClipboardHandler(new ClipboardHandler());
        mStructureView.setMinimumSize(new Dimension(100, 100));
        mStructureView.setPreferredSize(new Dimension(100, 100));
        mStructureView.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        mStructureView.setBackground(Color.white);
        p.add(new JLabel("Target structure"), "1," + row + ",3," + row);
        row += 2;
        p.add(mStructureView, "1," + row + ",3," + row);

        return p;
    }

//    private void populateCompoBox() {
//        for (DescriptorInfo descriptorInfo : DescriptorHelpers.getDescriptorInfos()) {
//            descriptorComboBox.addItem(descriptorInfo);
//        }
//    }
}
