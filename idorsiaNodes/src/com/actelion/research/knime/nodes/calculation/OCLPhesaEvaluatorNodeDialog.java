package com.actelion.research.knime.nodes.calculation;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.nodes.OCLNodeDialogPane;
import com.actelion.research.knime.utils.SpecHelper;

import info.clearthought.layout.TableLayout;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 */


public class OCLPhesaEvaluatorNodeDialog extends OCLNodeDialogPane {

	OCLPhesaEvaluatorNodeSettings m_Settings = new OCLPhesaEvaluatorNodeSettings();
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected OCLPhesaEvaluatorNodeDialog() {
        super();
        this.addTab("ForceField Minimization settings", this.createForceFieldMinimizationTab());
        
    }
        
    JComboBox<String> jComboBox_ColRef   = new JComboBox<>();
    JComboBox<String> jComboBox_ColQuery = new JComboBox<>();    
    
    JCheckBox         jCheckBox_OutputRef    = new JCheckBox();
    JCheckBox         jCheckBox_OutputQuery  = new JCheckBox();
        
    public JPanel createForceFieldMinimizationTab() {
    	JPanel p = new JPanel();
    	    	    
    	double pref = TableLayout.PREFERRED;
    	double layout_a[][] = new double[][] { {4,pref,8,pref,4} , {4,pref,8,pref,8,pref,8,pref,4} };

    	p.setLayout(new TableLayout(layout_a));
    	
    	p.add(new JLabel("Source column (Reference): "),"1,1");    	    
    	p.add(jComboBox_ColRef,"3,1");
    	p.add(new JLabel("Source column (Library): "),"1,3");
    	p.add(jComboBox_ColQuery,"3,3");
    	
    	JLabel l_outp_a = new JLabel("Output Ref Molecule: ");
    	JLabel l_outp_b = new JLabel("Output Library Molecule: ");
    	p.add(l_outp_a,"1,5");    	    
    	p.add(jCheckBox_OutputRef,"3,5");
    	p.add(l_outp_b,"1,7");
    	p.add(jCheckBox_OutputQuery,"3,7");    	
    	    	        
    	// add warning popups:
    	jCheckBox_OutputRef.setToolTipText("Only for first supplied Ref Molecule");
    	jCheckBox_OutputQuery.setToolTipText("Only for first supplied Ref Molecule");
    	l_outp_a.setToolTipText("Only for first supplied Ref Molecule");
    	l_outp_b.setToolTipText("Only for first supplied Ref Molecule");
    	    	
    	jComboBox_ColRef.removeAllItems();
    	jComboBox_ColQuery.removeAllItems();
    	    	    
    	return p;
    }

	@Override
	protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs_output, DataTableSpec[] specs_input)
			throws NotConfigurableException {
		
		List<DataColumnSpec> in_cols_r = SpecHelper.getPhesaMoleculeColumnSpecs(specs_input[0]);
		List<DataColumnSpec> in_cols_q = SpecHelper.getPhesaMoleculeColumnSpecs(specs_input[1]);
		
		if(in_cols_r.size()==0 || in_cols_q.size()==0) {
			throw new NotConfigurableException("Phesa Evaluator Node requires PhesaMolecule columns as input.");
		}
		
		jComboBox_ColRef.removeAllItems();
		jComboBox_ColQuery.removeAllItems();
		for(DataColumnSpec dci : in_cols_r) {
			jComboBox_ColRef.addItem(dci.getName());			
		}
		for(DataColumnSpec dci : in_cols_q) {
			jComboBox_ColQuery.addItem(dci.getName());			
		}
		
        this.m_Settings.loadSettingsForDialog(settings);
        
        jComboBox_ColRef.setSelectedItem(this.m_Settings.getInputColNameRef());
        jComboBox_ColQuery.setSelectedItem(this.m_Settings.getInputColNameQuery());
        
        jCheckBox_OutputRef.setSelected(this.m_Settings.isOutputMolRef());
        jCheckBox_OutputQuery.setSelected(this.m_Settings.isOutputMolQuery());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		m_Settings.setInputColNameRef( (String) jComboBox_ColRef.getSelectedItem());
		m_Settings.setInputColNameQuery( (String) jComboBox_ColQuery.getSelectedItem() );
		m_Settings.setOutputMolRef( jCheckBox_OutputRef.isSelected() );
		m_Settings.setOutputMolQuery( jCheckBox_OutputQuery.isSelected() );
		m_Settings.saveSettings(settings);
	}
        
}

