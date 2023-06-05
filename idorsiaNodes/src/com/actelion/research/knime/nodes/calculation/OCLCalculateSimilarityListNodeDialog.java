package com.actelion.research.knime.nodes.calculation;

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

public class OCLCalculateSimilarityListNodeDialog extends DescriptorDependentNodeDialog {
    private final OCLCalculateSimilarityListNodeSettings m_settings = new OCLCalculateSimilarityListNodeSettings();
    private JComboBox<String> inputColumnComboBox;
    private JComboBox<String> compareToComboBox;
    private JTextField mColumnNameTextField;
    private JComboBox<DescriptorInfo> descriptorComboBox;


    //~--- constructors -------------------------------------------------------

    public OCLCalculateSimilarityListNodeDialog() {
        this.addTab("Similarity settings", this.createSimilarityTab());
        init(inputColumnComboBox, descriptorComboBox);
    }

    @Override
    protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs, DataTableSpec[] specs_in) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        mColumnNameTextField.setText(this.m_settings.getColumnName());

        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecsOne = specs[0];
        DataTableSpec inputSpecsTwo = specs[1];

        populateMoleculeInputBox(inputSpecsOne);

        inputColumnComboBox.setSelectedItem(selectedInputColumn);
        descriptorComboBox.setSelectedItem(this.m_settings.getDescriptor());

        compareToComboBox.removeAllItems();

        for (String columnName : inputSpecsTwo.getColumnNames()) {
            DataColumnSpec columnSpec = inputSpecsTwo.getColumnSpec(columnName);

            if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
                compareToComboBox.addItem(columnName);
            }
        }

        compareToComboBox.setSelectedItem(selectedInputColumn);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.setColumnName(mColumnNameTextField.getText());
        this.m_settings.setDescriptor((DescriptorInfo) descriptorComboBox.getSelectedItem());
        this.m_settings.setCompareToColumnName((String) compareToComboBox.getSelectedItem());
        this.m_settings.saveSettings(settings);
    }


    private JPanel createSimilarityTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4}, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4
        }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<String>();
        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;
        compareToComboBox = new JComboBox<String>();
        p.add(new JLabel("Compare to"), "1," + row);
        p.add(compareToComboBox, "3," + row);
        row += 2;
        mColumnNameTextField = new JTextField(10);
        p.add(new JLabel("New column name"), "1," + row);
        p.add(mColumnNameTextField, "3," + row);
        row += 2;
        descriptorComboBox = new JComboBox<DescriptorInfo>();
        descriptorComboBox.setRenderer(new DescriptorListCellRenderer());
        p.add(new JLabel("Used descriptor"), "1," + row);
        p.add(descriptorComboBox, "3," + row);

        return p;
    }


}
