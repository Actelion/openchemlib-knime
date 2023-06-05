package com.actelion.research.knime.nodes.calculation;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.gui.VerticalFlowLayout;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.DescriptorDependentNodeDialog;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings.AvailableDescriptors;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings.DescriptorSettingsGUI;
import com.actelion.research.knime.ui.DescriptorListCellRenderer;

import info.clearthought.layout.TableLayout;

/**
 * <code>NodeDialog</code> for the "OCLNewCalculateSimilarityList" node.
 * 
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewCalculateSimilarityListNodeDialog extends DescriptorDependentNodeDialog {
    private final OCLNewCalculateSimilarityListNodeSettings m_settings = new OCLNewCalculateSimilarityListNodeSettings();
    
    JPanel p = new JPanel();
    private DescriptorSettings.DescriptorSettingsGUI j_descriptorGUI_A;
    private DescriptorSettings.DescriptorSettingsGUI j_descriptorGUI_B;
    //private JComboBox<String> inputColumnComboBox;
    //private JComboBox<String> compareToComboBox;
    private JTextField jt_ColumnNameTextField;
    //rivate JComboBox<String> descriptorComboBox;
    private JComboBox<String> jc_descriptors;
    
    
    List<String> all_descriptors = new ArrayList<>();


    //~--- constructors -------------------------------------------------------

    public OCLNewCalculateSimilarityListNodeDialog() {
        this.addTab("Similarity settings", this.createSimilarityTab());
        //init(inputColumnComboBox, descriptorComboBox);
    }
    
    @Override
    protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs, DataTableSpec[] specs_in) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        jt_ColumnNameTextField.setText(this.m_settings.getColumnName());
        
        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecsA = specs[0];
        DataTableSpec inputSpecsB = specs[1];
                
        //populateMoleculeInputBox(inputSpecsOne);
        
        // now fill the descriptor box with all descriptors where two are available:
        AvailableDescriptors avd_a = DescriptorSettings.findDescriptorColumns(inputSpecsA);
        AvailableDescriptors avd_b = DescriptorSettings.findDescriptorColumns(inputSpecsB);
        
        this.all_descriptors = avd_a.getAllDescriptorShortNames();
        this.all_descriptors.retainAll(avd_b.getAllDescriptorShortNames());
        
        
        String[] a_all_descr = new String[all_descriptors.size()];
        all_descriptors.toArray(a_all_descr);
        //jc_descriptors = new JComboBox<>(a_all_descr);
        jc_descriptors.setModel(new DefaultComboBoxModel<String>(a_all_descr));
        
        jc_descriptors.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				j_descriptorGUI_A.setShowOnlySpecific( (String) jc_descriptors.getSelectedItem());
				j_descriptorGUI_B.setShowOnlySpecific( (String) jc_descriptors.getSelectedItem());
				j_descriptorGUI_A.revalidate();
				j_descriptorGUI_B.revalidate();
				j_descriptorGUI_A.repaint();
				j_descriptorGUI_B.repaint();
				p.revalidate();
			}
		});
        
        this.j_descriptorGUI_A.initFromSettings(settings, inputSpecsA);
        this.j_descriptorGUI_B.initFromSettings(settings, inputSpecsB);
        
        // try to set combo box to correct value:
        try {
        	String descr_a = j_descriptorGUI_A.getDescriptorSettings().getDescriptorInfo().shortName;
        	jc_descriptors.setSelectedItem(descr_a);
        }
        catch(Exception ex) {
        	System.out.println("[ERR] Could not set descriptor combobox to stored value?..");
        }
        

        //compareToComboBox.removeAllItems();

//        for (String columnName : inputSpecsTwo.getColumnNames()) {
//            DataColumnSpec columnSpec = inputSpecsTwo.getColumnSpec(columnName);
//
//            if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
//                compareToComboBox.addItem(columnName);
//            }
//        }
//
//        compareToComboBox.setSelectedItem(selectedInputColumn);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        //this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        //this.m_settings.setColumnName(mColumnNameTextField.getText());
        //this.m_settings.setDescriptor((DescriptorInfo) descriptorComboBox.getSelectedItem());
        //this.m_settings.setCompareToColumnName((String) compareToComboBox.getSelectedItem());
    
    	
		this.m_settings.setColumnName(jt_ColumnNameTextField.getText());
		this.m_settings.setDescriptorSettingsA(this.j_descriptorGUI_A.getDescriptorSettings());
		this.m_settings.setDescriptorSettingsB(this.j_descriptorGUI_B.getDescriptorSettings());
		this.m_settings.setDescriptor(this.j_descriptorGUI_A.getDescriptorSettings().getDescriptorInfo());
		
        this.m_settings.saveSettings(settings);       	
    }


    private JPanel createSimilarityTab() {
        //JPanel p = new JPanel();
        //double[][] sizes = {
        //        {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4}, {
        //        4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4
        //}
        //};
        //TableLayout layout = new TableLayout(sizes);
        //p.setLayout(layout);
    	this.p = new JPanel();
    	
    	j_descriptorGUI_A = new DescriptorSettings.DescriptorSettingsGUI(OCLNewCalculateSimilarityListNodeSettings.DESCRIPTOR_COL_ID_A);
    	j_descriptorGUI_B = new DescriptorSettings.DescriptorSettingsGUI(OCLNewCalculateSimilarityListNodeSettings.DESCRIPTOR_COL_ID_B);

        p.setLayout(new VerticalFlowLayout());
        
        jc_descriptors = new JComboBox<>();
        p.add(jc_descriptors);
        
        JPanel pi_a = new JPanel();
        pi_a.setLayout(new FlowLayout());
        JPanel pi_b = new JPanel();
        pi_b.setLayout(new FlowLayout());
        
        pi_a.add(new JLabel("Input A"));
        pi_a.add(j_descriptorGUI_A);
        pi_b.add(new JLabel("Input B"));
        pi_b.add(j_descriptorGUI_B);
        
        //j_descriptorGUI_A = new DescriptorSettings.DescriptorSettingsGUI();
        //j_descriptorGUI_B = new DescriptorSettings.DescriptorSettingsGUI();
        
        JPanel pi_c = new JPanel();
        pi_c.setLayout(new FlowLayout());
        pi_c.add(new JLabel("Column Name "));
        jt_ColumnNameTextField = new JTextField(20);
        pi_c.add(jt_ColumnNameTextField);
        
        p.add(pi_a);
        p.add(pi_b);
        p.add(pi_c);
        
        return p;
    }


}

