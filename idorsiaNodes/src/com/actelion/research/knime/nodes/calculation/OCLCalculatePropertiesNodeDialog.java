package com.actelion.research.knime.nodes.calculation;

import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.ui.DataColumnSpecListCellRenderer;
import info.clearthought.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OCLCalculatePropertiesNodeDialog extends NodeDialogPane {
    private final OCLCalculatePropertiesNodeSettings m_settings = new OCLCalculatePropertiesNodeSettings();
    private JCheckBox[] propertyCheckboxes = new JCheckBox[OCLCalculatePropertiesNodeModel.propertyTable.size()];
    private Map<Integer, List<OCLCalculatePropertiesNodeModel.OCLProperty>> tabToPropertyMap;
    private JComboBox<DataColumnSpec> inputColumnComboBox;

//    private List<SettingsModelBoolean> settingsModels = new ArrayList<SettingsModelBoolean>();
//    private SettingsModelColumnName inputColumnModel;


    public OCLCalculatePropertiesNodeDialog() {
        createTabToPropertyMap();
        this.addTab("Calculate properties settings", this.createTab());

    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        DataColumnSpec columnSpec = (DataColumnSpec) inputColumnComboBox.getSelectedItem();
        this.m_settings.setInputColumnName((columnSpec == null)
                ? null
                : columnSpec.getName());


        int properties = OCLCalculatePropertiesNodeModel.propertyTable.size();
        boolean[] calculateProperty = new boolean[properties];
        for (int propertyTableIdx = 0; propertyTableIdx < properties; propertyTableIdx++) {
            calculateProperty[propertyTableIdx] = propertyCheckboxes[propertyTableIdx].isSelected();
        }
        this.m_settings.setCalculateProperties(calculateProperty);
        this.m_settings.saveSettings(settings);

    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);

        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataColumnSpec selectedInputColumnSpec = null;
        DataTableSpec inputSpecs = specs[0];

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

        boolean[] calculateProperties = this.m_settings.getCalculateProperties();
        for (int propertyIdx = 0; propertyIdx < calculateProperties.length; propertyIdx++) {
            propertyCheckboxes[propertyIdx].setSelected(calculateProperties[propertyIdx]);
        }

    }

    private void createTabToPropertyMap() {

        tabToPropertyMap = new HashMap<Integer, List<OCLCalculatePropertiesNodeModel.OCLProperty>>();
        for (int propertyTableIdx = 0; propertyTableIdx < OCLCalculatePropertiesNodeModel.propertyTable.size(); propertyTableIdx++) {
            OCLCalculatePropertiesNodeModel.OCLProperty property = OCLCalculatePropertiesNodeModel.propertyTable.get(propertyTableIdx);
            int tabIdx = property.tab;
            List<OCLCalculatePropertiesNodeModel.OCLProperty> propertyList = tabToPropertyMap.get(tabIdx);
            if (propertyList == null) {
                propertyList = new ArrayList<OCLCalculatePropertiesNodeModel.OCLProperty>();
                tabToPropertyMap.put(tabIdx, propertyList);
            }
            propertyList.add(property);
        }
    }

    private Component createTab() {
        JPanel p = new JPanel();
        int noTabs = OCLCalculatePropertiesNodeModel.TAB_GROUP.length;

        double[][] sizes = new double[2][];
        sizes[0] = new double[]{4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4};
        sizes[1] = new double[2 + 2 * noTabs];
        sizes[1][0] = 4;
        sizes[1][1] = TableLayout.PREFERRED;

        for (int tabGroupIdx = 0; tabGroupIdx < noTabs; tabGroupIdx++) {
            sizes[1][2 + tabGroupIdx * 2] = 4;
            sizes[1][3 + tabGroupIdx * 2] = TableLayout.PREFERRED;
        }
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);


        inputColumnComboBox = new JComboBox<DataColumnSpec>();
        inputColumnComboBox.setRenderer(new DataColumnSpecListCellRenderer());

        p.add(new JLabel("Input column"), "1,1");
        p.add(inputColumnComboBox, "3,1");


        for (int tabGroupIdx = 0; tabGroupIdx < noTabs; tabGroupIdx++) {
            JPanel propertyGroupPanel = createPropertyGroupPanel(tabGroupIdx);
            int row = (3 + tabGroupIdx * 2);
            p.add(propertyGroupPanel, "1," + row + ",3," + row);
        }


        return p;
    }

    private JPanel createPropertyGroupPanel(int tabIdx) {
        JPanel p = new JPanel();
        String groupName = OCLCalculatePropertiesNodeModel.TAB_GROUP[tabIdx];
        p.setBorder(new TitledBorder(groupName));
        List<OCLCalculatePropertiesNodeModel.OCLProperty> propertyList = tabToPropertyMap.get(tabIdx);
        List<JCheckBox> checkBoxesToAdd = new ArrayList<JCheckBox>();
        for (int propertyIdx = 0; propertyIdx < propertyList.size(); propertyIdx++) {
            OCLCalculatePropertiesNodeModel.OCLProperty property = propertyList.get(propertyIdx);
            JCheckBox checkBox = new JCheckBox(property.description);
            int i = OCLCalculatePropertiesNodeModel.propertyTable.indexOf(property);
            propertyCheckboxes[i] = checkBox;
            checkBoxesToAdd.add(propertyCheckboxes[i]);
        }
        double[][] sizes = new double[2][];
        sizes[0] = new double[]{TableLayout.PREFERRED};
        sizes[1] = new double[(checkBoxesToAdd.size() * 2) - 1];
        for (int a = 0; a < checkBoxesToAdd.size(); a++) {
            sizes[1][2 * a] = TableLayout.PREFERRED;
            if (a < (checkBoxesToAdd.size() - 1)) {
                sizes[1][2 * a + 1] = 4;
            }
        }
        TableLayout layout = new TableLayout(sizes);
        p.setLayout(layout);
        for (int a = 0; a < checkBoxesToAdd.size(); a++) {
            p.add(checkBoxesToAdd.get(a), "0, " + 2 * a);
        }

        return p;
    }

//    private void initSettingsModels() {
//        inputColumnModel = new SettingsModelColumnName(OCLCalculatePropertiesNodeModel.CFGKEY_INPUT_COLUMN, OCLCalculatePropertiesNodeModel.DEFAULT_INPUT_COLUMN);
//
//        for (int a = 0; a < OCLCalculatePropertiesNodeModel.propertyTable.size(); a++) {
//            settingsModels.add(new SettingsModelBoolean(OCLCalculatePropertiesNodeModel.PROPERTY_CODE[a], false));
//        }
//    }

//    private void initUI() {
//        setDefaultTabTitle("Input column");
//        addDialogComponent(new DialogComponentColumnNameSelection(inputColumnModel, "Input column", 0, true, false, OCLMoleculeDataValue.class));
//        int currentTab = -1;
//        for (int a = 0; a < OCLCalculatePropertiesNodeModel.propertyTable.size(); a++) {
//            OCLCalculatePropertiesNodeModel.OCLProperty oclProperty = OCLCalculatePropertiesNodeModel.propertyTable.get(a);
//            if (oclProperty.tab > currentTab) {
//                currentTab = oclProperty.tab;
//                createNewTab(OCLCalculatePropertiesNodeModel.TAB_GROUP[currentTab]);
//            }
//            addDialogComponent(new DialogComponentBoolean(settingsModels.get(a), OCLCalculatePropertiesNodeModel.propertyTable.get(a).description));
//        }
//    }

}
