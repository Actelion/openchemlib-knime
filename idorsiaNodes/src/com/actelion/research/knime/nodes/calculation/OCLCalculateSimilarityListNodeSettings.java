package com.actelion.research.knime.nodes.calculation;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLCalculateSimilarityListNodeSettings extends AbstractOCLNodeSettings {
    private static final String INPUT_COLUMN_NAME = "inputColumnName";
    private static final String COMPARE_TO_COLUMN_NAME = "compareToColumnName";
    private static final String COLUMN_NAME = "columnName";
    private static final String DESCRIPTOR = "descriptor";
    private IDCodeParser idCodeParser = new IDCodeParser();

    //~--- fields -------------------------------------------------------------

    private DataCell defaultValue = DataType.getMissingCell();
    private DataCell mapMissingTo = DataType.getMissingCell();

    private String inputColumnName;
    private String compareToColumnName;
    private String columnName;
    private DescriptorInfo descriptor;

    //~--- constructors -------------------------------------------------------

    public OCLCalculateSimilarityListNodeSettings() {
        this.inputColumnName = "";
        this.compareToColumnName = "";
        this.columnName = "Similarity";
        this.descriptor = DescriptorConstants.DESCRIPTOR_FFP512;
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
        settings.addString(COMPARE_TO_COLUMN_NAME, compareToColumnName);
        settings.addString(COLUMN_NAME, columnName);
        settings.addString(DESCRIPTOR, descriptor.shortName);
    }

    //~--- get methods --------------------------------------------------------


    public String getInputColumnName() {
        return inputColumnName;
    }

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    //~--- set methods --------------------------------------------------------

    public String getCompareToColumnName() {
        return compareToColumnName;
    }

    public void setCompareToColumnName(String compareToColumnName) {
        this.compareToColumnName = compareToColumnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public DescriptorInfo getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(DescriptorInfo descriptor) {
        this.descriptor = descriptor;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");
        this.compareToColumnName = settings.getString(COMPARE_TO_COLUMN_NAME, "");
        this.columnName = settings.getString(COLUMN_NAME, "Similarity");
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
}
