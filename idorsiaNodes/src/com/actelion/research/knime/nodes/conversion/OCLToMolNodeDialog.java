package com.actelion.research.knime.nodes.conversion;

import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.OCLNodeDialogPane;
import com.actelion.research.knime.ui.DataColumnSpecListCellRenderer;
import info.clearthought.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLToMolNodeDialog extends OCLNodeDialogPane {
    private final OCLToMolNodeSettings m_settings = new OCLToMolNodeSettings();
    private JTextField                           newColumnNameTextField;
    private JComboBox<DataColumnSpec>            inputColumnComboBox;
    private JCheckBox                            removeInputColumn;
    private JComboBox<OCLToMolNodeSettings.OutputType> outputTypeComboBox;

    //~--- constructors -------------------------------------------------------

    public OCLToMolNodeDialog() {
        this.addTab("Conversion settings", this.createTab());
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs, DataTableSpec[] specs_in) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        newColumnNameTextField.setText(this.m_settings.getNewColumnName());

        // Input column
        String         selectedInputColumn     = this.m_settings.getInputColumnName();
        DataColumnSpec selectedInputColumnSpec = null;
        DataTableSpec  inputSpecs              = specs[0];

        inputColumnComboBox.removeAllItems();

        for (String columnName : inputSpecs.getColumnNames()) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(columnName);

            if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)
                    || columnSpec.getType().isAdaptable(OCLMoleculeDataValue.class)) {
                inputColumnComboBox.addItem(columnSpec);

                if (columnSpec.getName().equals(selectedInputColumn)) {
                    selectedInputColumnSpec = columnSpec;
                }
            }
        }


        inputColumnComboBox.setSelectedItem(selectedInputColumnSpec);

        removeInputColumn.setSelected(m_settings.isRemoveInputColumn());
        OCLToMolNodeSettings.OutputType outputType = m_settings.getOutputType();
        outputTypeComboBox.setSelectedItem(outputType);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setNewColumnName(newColumnNameTextField.getText().trim());
        this.m_settings.setRemoveInputColumn(removeInputColumn.isSelected());

        DataColumnSpec columnSpec = (DataColumnSpec) inputColumnComboBox.getSelectedItem();

        this.m_settings.setInputColumnName((columnSpec == null)
                                           ? null
                                           : columnSpec.getName());
        this.m_settings.setOutputType((OCLToMolNodeSettings.OutputType) outputTypeComboBox.getSelectedItem());
        this.m_settings.saveSettings(settings);

    }

    private JPanel createTab() {
        JPanel      p      = new JPanel();
        double[][]  sizes  = {
            { 4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4 }, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4
            }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<DataColumnSpec>();
        inputColumnComboBox.setRenderer(new DataColumnSpecListCellRenderer());
        newColumnNameTextField = new JTextField(10);
        outputTypeComboBox = new JComboBox<OCLToMolNodeSettings.OutputType>();
        removeInputColumn      = new JCheckBox("Remove input column?");
        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;
        p.add(new JLabel("New column name"), "1," + row);
        p.add(newColumnNameTextField, "3," + row);
        row += 2;
        p.add(new JLabel("Output type"), "1," + row);
        p.add(outputTypeComboBox, "3," + row);
        row += 2;

        p.add(removeInputColumn, "1," + row + ",3," + row);

        populateComboBox();
        return p;
    }

    private void populateComboBox() {
        for (OCLToMolNodeSettings.OutputType outputType : OCLToMolNodeSettings.OutputType.values()) {
            outputTypeComboBox.addItem(outputType);
        }
    }
}
