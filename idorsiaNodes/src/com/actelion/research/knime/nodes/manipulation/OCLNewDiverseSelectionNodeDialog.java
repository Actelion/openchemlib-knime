package com.actelion.research.knime.nodes.manipulation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.nodes.DescriptorDependentNodeDialog;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings.DescriptorSettingsGUI;
import com.actelion.research.knime.ui.DescriptorListCellRenderer;

import info.clearthought.layout.TableLayout;

/**
 * 
 * <code>NodeDialog</code> for the "OCLNewDiverseSelection" node.
 * 
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewDiverseSelectionNodeDialog extends DescriptorDependentNodeDialog  {
	private OCLNewDiverseSelectionNodeSettings m_settings = new OCLNewDiverseSelectionNodeSettings();
    //private JComboBox<DescriptorInfo> descriptorComboBox;
	private DescriptorSettings.DescriptorSettingsGUI descriptorGUI;
    private JTextField diversitySelectionRankColumnName;
    
    private JComboBox<String> descriptorTypeComboBox;
    //private JComboBox<String> exclusionColumnComboBox;
    //private JComboBox<String> inputColumnComboBox;
    private DescriptorSettings.DescriptorSettingsGUI descriptorGUI_Exclusion;
    private JSpinner numberOfCompoundsSpinner;
    
    private DescriptorInfo[] allowedDescriptors = {DescriptorConstants.DESCRIPTOR_FFP512, DescriptorConstants.DESCRIPTOR_PFP512, DescriptorConstants.DESCRIPTOR_HashedCFp};


    //~--- constructors -------------------------------------------------------

    public OCLNewDiverseSelectionNodeDialog() {
        this.addTab("Diverse selection settings", this.createClusterTab());
        //init(inputColumnComboBox, descriptorComboBox);
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

        //populateMoleculeInputBox(inputSpecs, allowedDescriptors);
        //this.descriptorGUI.set

        
        // we can use false because we anyway enforce the restriction later in this
        // function by setting the descriptor type combo box
        this.descriptorGUI.initFromSettings(settings, specs_in[0], false);
        
        if(specs_in[1]==null) {
        	this.descriptorGUI_Exclusion.setEnabled(false);
        }
        else {
        	this.descriptorGUI_Exclusion.setEnabled(true);
        	this.descriptorGUI_Exclusion.initFromSettings(settings, specs_in[1], false);
        }
        
        if(this.descriptorGUI.getDescriptorSettings().getDescriptorInfo() != null) {
        	this.descriptorTypeComboBox.setSelectedItem(this.descriptorGUI.getDescriptorSettings().getDescriptorInfo().shortName);
        }
        //String exclusionColumnName = this.m_settings.getExclusionColumnName();
        //DataTableSpec exclusionSpecs = specs[1];

        //exclusionColumnComboBox.removeAllItems();
        //exclusionColumnComboBox.setEnabled(exclusionSpecs != null);

//        if (exclusionSpecs != null) {
//            for (String columnName : exclusionSpecs.getColumnNames()) {
//                DataColumnSpec columnSpec = exclusionSpecs.getColumnSpec(columnName);
//
//                if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
//                    exclusionColumnComboBox.addItem(columnName);
//                }
//            }
//
//            exclusionColumnComboBox.setSelectedItem(exclusionColumnName);
//        } else {
//            exclusionColumnComboBox.setSelectedItem(null);
//        }

        //inputColumnComboBox.setSelectedItem(selectedInputColumn);
        //descriptorComboBox.setSelectedItem(this.m_settings.getDescriptor());
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    	if(this.descriptorGUI.getDescriptorSettings().getDescriptorInfo()!=null) {
    		this.m_settings.setDescriptorType(this.descriptorGUI.getDescriptorSettings().getDescriptorInfo().shortName);	
    	}
    	else {
    		this.m_settings.setDescriptorType("");
    	}
    	
    	
        this.m_settings.setDiversityRankColumnName(diversitySelectionRankColumnName.getText().trim());
        this.m_settings.setNoOfCompounds(getNoCompounds());
        
        //this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        //this.m_settings.setExclusionColumnName((String) exclusionColumnComboBox.getSelectedItem());
        //this.m_settings.setDescriptor((DescriptorInfo) descriptorComboBox.getSelectedItem());
        //this.m_settings.setDescriptorSettings(this.descriptorGUI.getDescriptorSettings());
        //this.descriptorGUI.getDescriptorSettings().saveSettings(OCLNewDiverseSelectionNodeSettings.DESCRIPTOR_ID_A, settings);
        this.m_settings.setDescriptorA(this.descriptorGUI.getDescriptorSettings());
        if(this.descriptorGUI_Exclusion.isEnabled()) {
        	this.m_settings.setDescriptorB_Active(true);
        	this.m_settings.setDescriptorB(this.descriptorGUI_Exclusion.getDescriptorSettings());
        }
        else {
        	this.m_settings.setDescriptorB_Active(false);
        	this.m_settings.setDescriptorB(null);
        }
        this.m_settings.saveSettings(settings);
    }

    private JPanel createClusterTab() {
    	
        List<String> allowedDscs = Arrays.stream(allowedDescriptors).map(di -> di.shortName).collect(Collectors.toList());
    	
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
        
        
        this.descriptorTypeComboBox = new JComboBox<>(new Vector<>(allowedDscs));
       

        //inputColumnComboBox = new JComboBox<String>();
//        inputColumnComboBox.setRenderer(new DataColumnSpecListCellRenderer());
        //exclusionColumnComboBox = new JComboBox<String>();
        numberOfCompoundsSpinner = new JSpinner(new SpinnerNumberModel(1000, 0, 100000, 1));
        diversitySelectionRankColumnName = new JTextField(10);
        //descriptorComboBox = new JComboBox<DescriptorInfo>();
        //descriptorComboBox.setRenderer(new DescriptorListCellRenderer());
        
        descriptorGUI = new DescriptorSettingsGUI(OCLNewDiverseSelectionNodeSettings.DESCRIPTOR_ID_A);
        descriptorGUI_Exclusion = new DescriptorSettingsGUI(OCLNewDiverseSelectionNodeSettings.DESCRIPTOR_ID_B);
        
        p.add(new JLabel("Descriptor"), "1," + row);
        p.add(descriptorTypeComboBox, "3," + row);
        row += 2;
        
        p.add(new JLabel("Input column"), "1," + row);
        p.add(descriptorGUI, "3," + row);
        
        //p.add(descriptorComboBox, "3," + row);
        //p.add(descriptorGUI);
        row += 2;
        p.add(new JLabel("No of compounds"), "1," + row);
        p.add(numberOfCompoundsSpinner, "3," + row);
        row += 2;
        p.add(new JLabel("Selection rank column name"), "1," + row);
        p.add(diversitySelectionRankColumnName, "3," + row);
        row += 2;
        p.add(new JLabel("Avoid compounds in column"), "1," + row);
        p.add(descriptorGUI_Exclusion, "3," + row);
        
        this.descriptorTypeComboBox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		descriptorGUI.setShowOnlySpecific( (String) descriptorTypeComboBox.getSelectedItem());
        	}
        });
        
        this.descriptorTypeComboBox.setSelectedIndex(0);
        
        return p;
        
    }


    //~--- get methods --------------------------------------------------------

    private int getNoCompounds() {
        SpinnerNumberModel spinnerNumberModel = (SpinnerNumberModel) numberOfCompoundsSpinner.getModel();

        return spinnerNumberModel.getNumber().intValue();
    }
}

