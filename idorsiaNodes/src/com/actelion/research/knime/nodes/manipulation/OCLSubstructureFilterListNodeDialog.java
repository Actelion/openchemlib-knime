package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.knime.data.OCLMoleculeDataValue;
import info.clearthought.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.*;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLSubstructureFilterListNodeDialog extends NodeDialogPane {
    private final OCLSubstructureFilterListNodeSettings m_settings = new OCLSubstructureFilterListNodeSettings();
    private JComboBox<String> inputColumnComboBox;
    private JComboBox<String> filterColumnComboBox;


    //~--- constructors -------------------------------------------------------

    public OCLSubstructureFilterListNodeDialog() {
        this.addTab("Scaffold analysis settings", this.createSimilarityTab());
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);


        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecs = specs[0];
        inputColumnComboBox.removeAllItems();

        for (String columnName : inputSpecs.getColumnNames()) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(columnName);

            if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
                inputColumnComboBox.addItem(columnName);
            }
        }
        inputColumnComboBox.setSelectedItem(selectedInputColumn);

        // Filter column
        String selectedFilterColumn = this.m_settings.getFilterColumnName();
        DataTableSpec filterSpecs = specs[1];
        filterColumnComboBox.removeAllItems();

        for (String columnName : filterSpecs.getColumnNames()) {
            DataColumnSpec columnSpec = filterSpecs.getColumnSpec(columnName);

            if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
                filterColumnComboBox.addItem(columnName);
            }
        }
        filterColumnComboBox.setSelectedItem(selectedFilterColumn);
        
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.setFilterColumnName((String) filterColumnComboBox.getSelectedItem());
        this.m_settings.saveSettings(settings);
    }

    private JPanel createSimilarityTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4}, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4
        }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<String>();
        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;
        filterColumnComboBox = new JComboBox<String>();
        p.add(new JLabel("Substructure column"), "1," + row);
        p.add(filterColumnComboBox, "3," + row);


        return p;
    }
}
