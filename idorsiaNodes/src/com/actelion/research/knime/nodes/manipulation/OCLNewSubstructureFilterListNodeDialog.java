package com.actelion.research.knime.nodes.manipulation;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings.DescriptorSettingsGUI;

import info.clearthought.layout.TableLayout;

/**
 * 
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewSubstructureFilterListNodeDialog extends NodeDialogPane {
    private final OCLNewSubstructureFilterListNodeSettings m_settings = new OCLNewSubstructureFilterListNodeSettings();
    private JComboBox<String> inputColumnComboBox;
    private JComboBox<String> filterColumnComboBox;
    private JCheckBox jcb_inputUseFFP;
    private JCheckBox jcb_filterUseFFP;
    private DescriptorSettings.DescriptorSettingsGUI descriptorGUI_input;
    private DescriptorSettings.DescriptorSettingsGUI descriptorGUI_filter;
    


    //~--- constructors -------------------------------------------------------

    public OCLNewSubstructureFilterListNodeDialog() {
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
        
        jcb_inputUseFFP.setSelected(this.m_settings.hasFFP_Input());
        jcb_filterUseFFP.setSelected(this.m_settings.hasFFP_Filter());
        
        descriptorGUI_input.initFromSettings(settings, inputSpecs);
        descriptorGUI_filter.initFromSettings(settings, filterSpecs);
        
        descriptorGUI_input.setEnabled(this.m_settings.hasFFP_Input());
        descriptorGUI_input.setEnabled(this.m_settings.hasFFP_Filter());
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.setFilterColumnName((String) filterColumnComboBox.getSelectedItem());
        
        if(this.jcb_inputUseFFP.isSelected()) {
        	this.m_settings.setDescriptorInput(this.descriptorGUI_input.getDescriptorSettings());
        }
        else {
        	this.m_settings.setDescriptorInput(null);
        }
        if(this.jcb_filterUseFFP.isSelected()) {
        	this.m_settings.setDescriptorFilter(this.descriptorGUI_filter.getDescriptorSettings());
        }
        else {
        	this.m_settings.setDescriptorFilter(null);
        }
        
        this.m_settings.saveSettings(settings);
    }

    private JPanel createSimilarityTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4}, {
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
        
        jcb_inputUseFFP = new JCheckBox("Use FFP");
        jcb_filterUseFFP = new JCheckBox("Use FFP");
        descriptorGUI_input  = new DescriptorSettingsGUI(OCLNewSubstructureFilterListNodeSettings.DESCRIPTOR_INPUT_ID);
        descriptorGUI_filter = new DescriptorSettingsGUI(OCLNewSubstructureFilterListNodeSettings.DESCRIPTOR_FILTER_ID);
        
        descriptorGUI_input.setShowOnlySpecific(DescriptorHelper.DESCRIPTOR_FFP512.shortName);
        descriptorGUI_filter.setShowOnlySpecific(DescriptorHelper.DESCRIPTOR_FFP512.shortName);
        
        
        p.add(jcb_inputUseFFP,"5,1");
        p.add(jcb_filterUseFFP,"5,3");
        
        p.add(descriptorGUI_input,"7,1");
        p.add(descriptorGUI_filter,"7,3");
        
        jcb_inputUseFFP.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				descriptorGUI_input.setEnabled(jcb_inputUseFFP.isSelected());
			}
		});
        jcb_filterUseFFP.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				descriptorGUI_filter.setEnabled(jcb_filterUseFFP.isSelected());
			}
		});


        return p;
    }
}

