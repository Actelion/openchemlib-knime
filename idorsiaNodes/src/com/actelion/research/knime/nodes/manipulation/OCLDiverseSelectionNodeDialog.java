package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.DescriptorDependentNodeDialog;
import com.actelion.research.knime.ui.DescriptorListCellRenderer;

import info.clearthought.layout.TableLayout;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLDiverseSelectionNodeDialog extends DescriptorDependentNodeDialog {
    private final OCLDiverseSelectionNodeSettings m_settings = new OCLDiverseSelectionNodeSettings();
    private JComboBox<DescriptorInfo> descriptorComboBox;
    private JTextField diversitySelectionRankColumnName;
    private JComboBox<String> exclusionColumnComboBox;
    private JComboBox<String> inputColumnComboBox;
    private JSpinner numberOfCompoundsSpinner;

    //~--- constructors -------------------------------------------------------

    public OCLDiverseSelectionNodeDialog() {
        this.addTab("Diverse selection settings", this.createClusterTab());
        init(inputColumnComboBox, descriptorComboBox);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs, DataTableSpec[] specs_in) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        numberOfCompoundsSpinner.setValue(this.m_settings.getNoOfCompounds());
        diversitySelectionRankColumnName.setText(this.m_settings.getDiversityRankColumnName());

        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();

        DataTableSpec inputSpecs = specs[0];

        DescriptorInfo[] allowedDescriptors = {DescriptorConstants.DESCRIPTOR_FFP512, DescriptorConstants.DESCRIPTOR_PFP512, DescriptorConstants.DESCRIPTOR_HashedCFp};
        populateMoleculeInputBox(inputSpecs, allowedDescriptors);

        String exclusionColumnName = this.m_settings.getExclusionColumnName();
        DataTableSpec exclusionSpecs = specs[1];

        exclusionColumnComboBox.removeAllItems();
        exclusionColumnComboBox.setEnabled(exclusionSpecs != null);

        if (exclusionSpecs != null) {
            for (String columnName : exclusionSpecs.getColumnNames()) {
                DataColumnSpec columnSpec = exclusionSpecs.getColumnSpec(columnName);

                if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
                    exclusionColumnComboBox.addItem(columnName);
                }
            }

            exclusionColumnComboBox.setSelectedItem(exclusionColumnName);
        } else {
            exclusionColumnComboBox.setSelectedItem(null);
        }

        inputColumnComboBox.setSelectedItem(selectedInputColumn);
        descriptorComboBox.setSelectedItem(this.m_settings.getDescriptor());

    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setDiversityRankColumnName(diversitySelectionRankColumnName.getText().trim());
        this.m_settings.setNoOfCompounds(getNoCompounds());
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.setExclusionColumnName((String) exclusionColumnComboBox.getSelectedItem());
        this.m_settings.setDescriptor((DescriptorInfo) descriptorComboBox.getSelectedItem());
        //this.m_settings.setDescriptorSettings(this.desc);
        this.m_settings.saveSettings(settings);
    }

    private JPanel createClusterTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4}, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4,
                TableLayout.PREFERRED, 4
        }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<String>();
//        inputColumnComboBox.setRenderer(new DataColumnSpecListCellRenderer());
        exclusionColumnComboBox = new JComboBox<String>();
        numberOfCompoundsSpinner = new JSpinner(new SpinnerNumberModel(1000, 0, 100000, 1));
        diversitySelectionRankColumnName = new JTextField(10);
        descriptorComboBox = new JComboBox<DescriptorInfo>();
        descriptorComboBox.setRenderer(new DescriptorListCellRenderer());
        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;
        p.add(new JLabel("Descriptor"), "1," + row);
        p.add(descriptorComboBox, "3," + row);
        row += 2;
        p.add(new JLabel("No of compounds"), "1," + row);
        p.add(numberOfCompoundsSpinner, "3," + row);
        row += 2;
        p.add(new JLabel("Selection rank column name"), "1," + row);
        p.add(diversitySelectionRankColumnName, "3," + row);
        row += 2;
        p.add(new JLabel("Avoid compounds in column"), "1," + row);
        p.add(exclusionColumnComboBox, "3," + row);

        return p;
    }


    //~--- get methods --------------------------------------------------------

    private int getNoCompounds() {
        SpinnerNumberModel spinnerNumberModel = (SpinnerNumberModel) numberOfCompoundsSpinner.getModel();

        return spinnerNumberModel.getNumber().intValue();
    }
}
