package com.actelion.research.knime.nodes.conversion;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
 *
 *
 * @author liphath1
 * 
 * Idorsia Pharmaceuticals Ltd - 2020
 * 
 */
public class MolToConformerListNodeDialog extends OCLNodeDialogPane {
	
	private MolToConformerListNodeSettings m_Settings = new MolToConformerListNodeSettings();
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected MolToConformerListNodeDialog() {
        super();
        
        this.addTab("Conversion settings", this.createTab());	
    }
    
    
    private JComboBox<String> jComboBox_InputCol = new JComboBox<>();    
    
    private JPanel createTab() {
    	JPanel p = new JPanel();       	
    	
    	double pref = TableLayout.PREFERRED;
    	double layout_a[][] = new double[][] { {4,pref,8,pref,4} , {4,pref,4} };

    	p.setLayout(new TableLayout(layout_a));
    	
    	p.add(new JLabel("Source column: "),"1,1");        	   
    	p.add(jComboBox_InputCol,"3,1");   	
    	
    	return p;
    }
    
         
    
	@Override
	protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs_output, DataTableSpec[] specs_input)
			throws NotConfigurableException {
		
		// init input combobox, i.e. find all conformer list columns:
		List<DataColumnSpec> columns = SpecHelper.getMoleculeColumnSpecs(specs_input[0]);
		System.out.println("Input Columns: Size = "+columns.size());
		jComboBox_InputCol.removeAllItems();		
		for(DataColumnSpec ci : columns) { jComboBox_InputCol.addItem(ci.getName()); }    	
    			
		this.m_Settings.loadSettingsForDialog(settings);			
		jComboBox_InputCol.setSelectedItem( this.m_Settings.getInputColumnName() );
		
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {		
		this.m_Settings.setInputColumnName( (String) this.jComboBox_InputCol.getSelectedItem() );
		this.m_Settings.saveSettings(settings);			
    }    
    
}

