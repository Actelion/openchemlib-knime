package com.actelion.research.knime.nodes.calculation;

import java.util.List;

import javax.swing.JCheckBox;
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
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.actelion.research.knime.nodes.OCLNodeDialogPane;
import com.actelion.research.knime.utils.SpecHelper;

import info.clearthought.layout.TableLayout;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 */

public class OCLForceFieldMinimizationNodeDialog extends OCLNodeDialogPane {

		
	OCLForceFieldMinimizationNodeSettings m_Settings = new OCLForceFieldMinimizationNodeSettings();
		
	
    JComboBox<String> jComboBox_SourceColumn      = new JComboBox<>();
    
    JComboBox<String> jComboBox_ForceField        = new JComboBox<>();
    
    JCheckBox         jCheckBox_Overwrite         = new JCheckBox();
    
    JCheckBox         jCheckBox_OutputMinEnergy   = new JCheckBox();
    
    JCheckBox         jCheckBox_OutputEnergyAtStart = new JCheckBox();
          
    JTextField        jTextField_MaxIterations    = new JTextField();

//    JTextField        jTextField_MinRMS           = new JTextField();
    
    JTextField        jTextField_TolGrad          = new JTextField();
    
    JTextField        jTextField_TolFunc          = new JTextField();
    
    JCheckBox         jCheckBox_OutputOptimIter   = new JCheckBox();
    
    JCheckBox         jCheckBox_OutputOptimResult = new JCheckBox();
    
    
	
	
	
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected OCLForceFieldMinimizationNodeDialog() {
        super();
        
        this.addTab("ForceField settings", this.createForceFieldMinimizationTab_Main());
        this.addTab("Optimization settings", this.createForceFieldMinimizationTab_Optim());
                       
    }
    
    
    
    public JPanel createForceFieldMinimizationTab_Main() {
    	JPanel p = new JPanel();
    	    	    
    	double pref = TableLayout.PREFERRED;
    	double layout_a[][] = new double[][] { {4,pref,8,pref,4} , {4,pref,8,pref,8,pref,8,pref,8,pref,4} };

    	p.setLayout(new TableLayout(layout_a));
    	
    	p.add(new JLabel("Source column: "),"1,1");
    	p.add(jComboBox_SourceColumn,"3,1");
    	p.add(new JLabel("Forcefield: "),"1,3");    	
    	p.add(jComboBox_ForceField,"3,3");
    	p.add(new JLabel("Overwrite: "),"1,5");
    	p.add(jCheckBox_Overwrite,"3,5");
    	p.add(new JLabel("Output Energy: "),"1,7");
    	p.add(jCheckBox_OutputMinEnergy,"3,7");
    	p.add(new JLabel("Output Energy At Start: "),"1,9");
    	p.add(jCheckBox_OutputEnergyAtStart,"3,9");
    	    	    
    	jComboBox_ForceField.removeAllItems();
    	for(int zi=0;zi<OCLForceFieldMinimizationNodeSettings.MINIMIZE_TEXT.length;zi++) {
    		jComboBox_ForceField.addItem(OCLForceFieldMinimizationNodeSettings.MINIMIZE_TEXT[zi]);    		
    	}
    	    	    
    	return p;
    }
    public JPanel createForceFieldMinimizationTab_Optim() {
    	JPanel p = new JPanel();    	
    	
    	double pref = TableLayout.PREFERRED;
    	double layout_a[][] = new double[][] { {4,pref,8,pref,4} , {4,pref,8,pref,8,pref,8,pref,8,pref,4} };

    	p.setLayout(new TableLayout(layout_a));
    	    	
    	p.add(new JLabel("Max Iterations: "),"1,1");
    	p.add(jTextField_MaxIterations,"3,1");
    	p.add(new JLabel("Convergence_TolGrad: "),"1,3");
    	p.add(jTextField_TolGrad,"3,3");
    	p.add(new JLabel("Convergence_TolFunc: "),"1,5");    	
    	p.add(jTextField_TolFunc,"3,5");
    	
    	JLabel output_optim_result = new JLabel("Output Optim Result"); 
    	p.add(output_optim_result,"1,7");
    	p.add(jCheckBox_OutputOptimResult,"3,7");
    	JLabel output_optim_iter = new JLabel("Output Optim Iter");
    	p.add(output_optim_iter,"1,9");
    	p.add(jCheckBox_OutputOptimIter,"3,9");    	
    	
    	jTextField_TolFunc.setEnabled(false);
    	    	
    	// set tooltips..
    	output_optim_result.setToolTipText("Not yet available for all optim. methods");
    	output_optim_iter.setToolTipText("Not yet available for all optim. methods");
    	jCheckBox_OutputOptimIter.setToolTipText("Not yet available for all optim. methods");
    	jCheckBox_OutputOptimResult.setToolTipText("Not yet available for all optim. methods");
    	
    	return p;
    }    



	@Override
	protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs_out, DataTableSpec[] specs) throws NotConfigurableException {
		
		List<DataColumnSpec> in_cols = SpecHelper.getConformerListColumnSpecs(specs[0]);
					
		jComboBox_SourceColumn.removeAllItems();
		for(DataColumnSpec dci : in_cols) {
			jComboBox_SourceColumn.addItem(dci.getName());
		}
		
		this.m_Settings.loadSettingsForDialog(settings);
		

		jTextField_MaxIterations.setText( ""+this.m_Settings.getMaxIterations() );
		jTextField_TolGrad.setText( ""+this.m_Settings.getTolGrad() );
		jTextField_TolFunc.setText( ""+this.m_Settings.getTolFunc() );						
		
		jCheckBox_OutputMinEnergy.setSelected(m_Settings.isOutEnergy());
		jCheckBox_OutputEnergyAtStart.setSelected(m_Settings.isOutEnergyAtStart());
		jCheckBox_OutputOptimIter.setSelected(m_Settings.isOutOptIter());
		jCheckBox_OutputOptimResult.setSelected(m_Settings.isOutOptResult());
		
		jComboBox_SourceColumn.setSelectedItem( this.m_Settings.getInputColumnName() );
		jComboBox_ForceField.setSelectedItem( this.m_Settings.getMinimizationMethod() );
		jCheckBox_Overwrite.setSelected( this.m_Settings.isOverwriteInput());											
	}



	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
								
		settings.addString(OCLForceFieldMinimizationNodeSettings.PROPERTY_INPUT_COLUMN, (String) jComboBox_SourceColumn.getSelectedItem());
		settings.addInt(OCLForceFieldMinimizationNodeSettings.PROPERTY_MINIMIZATION_METHOD, jComboBox_ForceField.getSelectedIndex());
		settings.addBoolean(OCLForceFieldMinimizationNodeSettings.PROPERTY_OVERWRITE_INPUT, jCheckBox_Overwrite.isSelected());
		
		
		this.m_Settings.setInputColumnName((String) jComboBox_SourceColumn.getSelectedItem());
		this.m_Settings.setMinimizationMethod(jComboBox_ForceField.getSelectedIndex());
		this.m_Settings.setOverwriteInput(jCheckBox_Overwrite.isSelected());
		this.m_Settings.setOutEnergy(jCheckBox_OutputMinEnergy.isSelected());
		this.m_Settings.setOutEnergyAtStart(jCheckBox_OutputEnergyAtStart.isSelected());
			
		this.m_Settings.setOutOptResult(jCheckBox_OutputOptimResult.isSelected());
		this.m_Settings.setOutOptIter(jCheckBox_OutputOptimIter.isSelected());

		
		String tf_maxIt = jTextField_MaxIterations.getText();
		String tf_tolG  = jTextField_TolGrad.getText();
		String tf_tolF  = jTextField_TolFunc.getText();
		
		int max_it     = Integer.parseInt(tf_maxIt);
		double tol_g   = Double.parseDouble(tf_tolG);
		double tol_f   = Double.parseDouble(tf_tolF);
		
		this.m_Settings.setMaxIterations(max_it);
		this.m_Settings.setTolGrad(tol_g);
		this.m_Settings.setTolFunc(tol_f);
		
		this.m_Settings.saveSettings(settings);
		
//		try {
//			String tf_maxIt = jTextField_MaxIterations.getText();
//			String tf_tolG  = jTextField_TolGrad.getText();
//			String tf_tolF  = jTextField_TolFunc.getText();
//			
//			int max_it     = Integer.parseInt(tf_maxIt);
//			double tol_g   = Double.parseDouble(tf_tolG);
//			double tol_f   = Double.parseDouble(tf_tolF);
//			
//			settings.addInt(OCLForceFieldMinimizationNodeSettings.PROPERTY_MAX_ITS, max_it);
//			settings.addDouble(OCLForceFieldMinimizationNodeSettings.PROPERTY_TOL_GRAD,tol_g);
//			settings.addDouble(OCLForceFieldMinimizationNodeSettings.PROPERTY_TOL_FUNC,tol_f);					
//		}
//		catch(NumberFormatException ex) {
//			ex.printStackTrace();
//			throw new InvalidSettingsException("Error parsing numeric values.");
//		}					
	}
    
    
    
    
          
}

