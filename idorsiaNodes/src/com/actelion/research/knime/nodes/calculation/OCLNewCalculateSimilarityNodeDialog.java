package com.actelion.research.knime.nodes.calculation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.gui.JEditableStructureView;
import com.actelion.research.gui.VerticalFlowLayout;
import com.actelion.research.gui.clipboard.ClipboardHandler;
import com.actelion.research.knime.nodes.OCLNodeDialogPane;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings;

/**
 * <code>NodeDialog</code> for the "OCLNewCalculateSimilarity" node.
 * 
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewCalculateSimilarityNodeDialog extends OCLNodeDialogPane {
	
	private final OCLNewCalculateSimilarityNodeSettings m_settings = new OCLNewCalculateSimilarityNodeSettings();
    private DescriptorSettings.DescriptorSettingsGUI mDescriptorGUI;
    private JTextField mOutputColumnNameTextField;
    private JEditableStructureView mStructureView;

    /**
     * New pane for configuring the OCLNewCalculateSimilarity node.
     */
    protected OCLNewCalculateSimilarityNodeDialog() {
    	this.addTab("Similarity settings", this.createSimilarityTab());
        //init(inputColumnComboBox, descriptorComboBox);
    }
    
    @Override
    protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs, DataTableSpec[] specs_in) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        StereoMolecule fi = new StereoMolecule();
        if(this.m_settings.getFragment()!=null) {
        	if(!this.m_settings.getFragment().isEmpty()) {
        		IDCodeParser icp = new IDCodeParser();
        		icp.parse(fi,this.m_settings.getFragment());
        	}
        }
        mStructureView.structureChanged(fi);
        mOutputColumnNameTextField.setText(this.m_settings.getOutputColumnName());

        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecs = specs[0];

        // TODO: set descriptor gui to correct selection
        System.out.println("dialog::initFromSettings settings: "+settings.toString());
        this.mDescriptorGUI.initFromSettings(settings, inputSpecs);
        
        
        //populateMoleculeInputBox(inputSpecs);

        //inputColumnComboBox.setSelectedItem(selectedInputColumn);
        //descriptorComboBox.setSelectedItem(this.m_settings.getDescriptor());
    }
    
    private JPanel createSimilarityTab() {
    	JPanel jp_sim = new JPanel();
    	jp_sim.setLayout(new VerticalFlowLayout());
    	mDescriptorGUI = new DescriptorSettings.DescriptorSettingsGUI(OCLNewCalculateSimilarityNodeSettings.DESCRIPTOR_COL_ID_A);
    	
    	jp_sim.add(mDescriptorGUI);
    	
    	JPanel pa = new JPanel();
    	pa.setLayout(new FlowLayout());
    	pa.add(new JLabel("Output Column Name "));
    	mOutputColumnNameTextField = new JTextField(20);
    	pa.add(mOutputColumnNameTextField);
    	jp_sim.add(pa);
    	
    	StereoMolecule fragment = new StereoMolecule();
    	mStructureView = new JEditableStructureView(fragment);
        mStructureView.setClipboardHandler(new ClipboardHandler());
        mStructureView.setMinimumSize(new Dimension(100, 100));
        mStructureView.setPreferredSize(new Dimension(100, 100));
        mStructureView.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        mStructureView.setBackground(Color.white);
    	
    	jp_sim.add(mStructureView);
    	return jp_sim;
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.m_settings.setFragment(mStructureView.getMolecule().getIDCode());
		this.m_settings.setOutputColumnName(this.mOutputColumnNameTextField.getText()); // TODO.. change
		this.m_settings.setDescriptorSettings(this.mDescriptorGUI.getDescriptorSettings());
		this.m_settings.saveSettings(settings);
		//System.out.println("dialog::saveSettingsTo settings: "+settings());
		
		//this.mDescriptorGUI.getDescriptorSettings().saveSettings(OCLNewCalculateSimilarityNodeSettings.DESCRIPTOR_COL_ID_A, settings);
	}
}

