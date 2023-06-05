package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.knime.data.ScaffoldType;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import info.clearthought.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLScaffoldAnalysisNodeDialog extends NodeDialogPane {
    private final OCLScaffoldAnalysisNodeSettings m_settings = new OCLScaffoldAnalysisNodeSettings();
    private JComboBox<String>                       inputColumnComboBox;
    private JComboBox<ScaffoldType> scaffoldTypeComboBox;

    //~--- constructors -------------------------------------------------------

    public OCLScaffoldAnalysisNodeDialog() {
        this.addTab("Scaffold analysis settings", this.createScaffoldAnalysisTab());
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);

        // Input column
        String        selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecs          = specs[0];

        inputColumnComboBox.removeAllItems();

        for (String columnName : inputSpecs.getColumnNames()) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(columnName);

            if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
                inputColumnComboBox.addItem(columnName);
            }
        }

        inputColumnComboBox.setSelectedItem(selectedInputColumn);

        // Scaffold type
        scaffoldTypeComboBox.setSelectedItem(m_settings.getScaffoldType());
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.setScaffoldType((ScaffoldType)scaffoldTypeComboBox.getSelectedItem());
        this.m_settings.saveSettings(settings);
    }

    private JPanel createScaffoldAnalysisTab() {
        JPanel      p      = new JPanel();
        double[][]  sizes  = {
            { 4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4 }, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4
            }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<String>();
        scaffoldTypeComboBox = new JComboBox<ScaffoldType>(ScaffoldType.values());


        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;

        p.add(new JLabel("Scaffold type"), "1," + row);
        p.add(scaffoldTypeComboBox, "3," + row);

        return p;
    }
}
