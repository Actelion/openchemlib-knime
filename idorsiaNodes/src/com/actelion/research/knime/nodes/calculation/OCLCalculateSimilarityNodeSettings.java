/*
 * @(#)OCLCalculateSimilarityNodeSettings.java   15/10/29
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

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLCalculateSimilarityNodeSettings extends AbstractOCLNodeSettings {
    private static final String COLUMN_NAME       = "columnName";
    private static final String DESCRIPTOR        = "descriptor";
    private static final String FRAGMENT          = "fragment";
    private static final String INPUT_COLUMN_NAME = "inputColumnName";

    //~--- fields -------------------------------------------------------------

    private IDCodeParser   idCodeParser = new IDCodeParser();
    private DataCell       defaultValue = DataType.getMissingCell();
    private DataCell       mapMissingTo = DataType.getMissingCell();
    private String         columnName;
    private DescriptorInfo descriptor;

//  private static final String SIMILARITY = "similarity";
    private StereoMolecule fragment;
    private String         inputColumnName;

    //~--- constructors -------------------------------------------------------

    public OCLCalculateSimilarityNodeSettings() {
        this.fragment        = new StereoMolecule();
        this.columnName      = "Similarity";
        this.inputColumnName = "";
        this.descriptor      = DescriptorConstants.DESCRIPTOR_FFP512;
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

    private void loadSettings(NodeSettingsRO settings) {
        String[] fragmentIdCodeCoords = settings.getStringArray(FRAGMENT, (String[]) null);

        this.fragment = new StereoMolecule();

        if (fragmentIdCodeCoords != null) {
            idCodeParser.parse(this.fragment, fragmentIdCodeCoords[0], fragmentIdCodeCoords[1]);
        }

        this.columnName      = settings.getString(COLUMN_NAME, "Similarity");
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");

        String descriptorShortName = settings.getString(DESCRIPTOR, null);

        this.descriptor = DescriptorConstants.DESCRIPTOR_FFP512;

        if (descriptorShortName != null) {
            for (DescriptorInfo descriptorInfo : DescriptorConstants.DESCRIPTOR_EXTENDED_LIST) {
                if (descriptorInfo.shortName.equals(descriptorShortName)) {
                    this.descriptor = descriptorInfo;
                }
            }
        }
    }

    @Override
    public void saveSettings(NodeSettingsWO settings) {
        settings.addStringArray(FRAGMENT, fragment.getIDCode(), fragment.getIDCoordinates());
        settings.addString(INPUT_COLUMN_NAME, inputColumnName);
        settings.addString(COLUMN_NAME, columnName);
        settings.addString(DESCRIPTOR, descriptor.shortName);
    }

    //~--- get methods --------------------------------------------------------

    public String getColumnName() {
        return columnName;
    }

    public DescriptorInfo getDescriptor() {
        return descriptor;
    }

    public StereoMolecule getFragment() {
        return fragment;
    }

    public String getInputColumnName() {
        return inputColumnName;
    }

    //~--- set methods --------------------------------------------------------

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setDescriptor(DescriptorInfo descriptor) {
        this.descriptor = descriptor;
    }

    public void setFragment(StereoMolecule fragment) {
        this.fragment = fragment;
    }

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }
}
