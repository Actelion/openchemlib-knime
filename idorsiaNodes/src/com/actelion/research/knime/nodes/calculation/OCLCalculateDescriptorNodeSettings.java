/*
 * @(#)OCLCalculateDescriptorNodeSettings.java   15/10/29
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
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.utils.DescriptorHelpers;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.util.ArrayList;
import java.util.List;

public class OCLCalculateDescriptorNodeSettings extends AbstractOCLNodeSettings {
    private static final String DESCRIPTORS = "descriptor";
    private static final String INPUT_COLUMN_NAME = "inputColumnName";

    //~--- fields -------------------------------------------------------------

    private DescriptorInfo[] selectedDescriptors;
    private String inputColumnName;

    //~--- constructors -------------------------------------------------------

    public OCLCalculateDescriptorNodeSettings() {
        this.inputColumnName = "";
        this.selectedDescriptors = new DescriptorInfo[0];  
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
        String[] descriptorNames = new String[selectedDescriptors.length];
        for (int i = 0; i < selectedDescriptors.length; i++) {
            descriptorNames[i] = selectedDescriptors[i].shortName;
        }
        settings.addStringArray(DESCRIPTORS, descriptorNames);
    }

    //~--- get methods --------------------------------------------------------


    public List<String> getSelectedDescriptors() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < selectedDescriptors.length; i++) {
            result.add(selectedDescriptors[i].shortName);
        }
        return result;
    }


    public void setSelectedDescriptors(String[] descriptorNames) {
        this.selectedDescriptors = new DescriptorInfo[descriptorNames.length];
        for (int i = 0; i < descriptorNames.length; i++) {
            selectedDescriptors[i] = DescriptorHelpers.getDescriptorInfoByShortName(descriptorNames[i]);
        }
    }


    //~--- set methods --------------------------------------------------------


    public String getInputColumnName() {
        return inputColumnName;
    }

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");
        String[] descriptorNames = settings.getStringArray(DESCRIPTORS, new String[0]);
        this.selectedDescriptors = new DescriptorInfo[descriptorNames.length];
        for (int i = 0; i < descriptorNames.length; i++) {
            selectedDescriptors[i] = DescriptorHelpers.getDescriptorInfoByShortName(descriptorNames[i]);
        }
    }
}
