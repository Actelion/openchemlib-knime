package com.actelion.research.knime.nodes.conversion;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.nodes.OCLNodeDialogPane;
import com.actelion.research.knime.nodes.conversion.ConformerListToMolNodeSettings.OutputType3D;
import com.actelion.research.knime.nodes.conversion.OCLToMolNodeSettings.OutputType;
import com.actelion.research.knime.utils.SpecHelper;

import info.clearthought.layout.TableLayout;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */


public class ConformerListToMolNodeDialog extends OCLNodeDialogPane {

	private ConformerListToMolNodeSettings m_Settings = new ConformerListToMolNodeSettings();
	
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected ConformerListToMolNodeDialog() {
        super();        
		
		this.addTab("Conversion settings", this.createTab());		
    }

    
    private JComboBox<String> jComboBox_InputCol = new JComboBox<>();    
    private JComboBox<String> jComboBox_OutputType = new JComboBox<>();
    
    private JPanel createTab() {
    	JPanel p = new JPanel();   
    	
    	
    	
    	double pref = TableLayout.PREFERRED;
    	double layout_a[][] = new double[][] { {4,pref,8,pref,4} , {4,pref,8,pref,4} };

    	p.setLayout(new TableLayout(layout_a));    	
    	    	    
    	p.add(new JLabel("Input Column"), "1,1" );    	
    	p.add(jComboBox_InputCol,"3,1");
    	
    	p.add(new JLabel("Output Type"), "1,3" );
    	p.add(jComboBox_OutputType,"3,3");
    	
    	return p;
    }
        
           
	@Override
	protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs_out, DataTableSpec[] specs) throws NotConfigurableException {
				
		// init input combobox, i.e. find all conformer list colum	ns:
		List<DataColumnSpec> columns = SpecHelper.getConformerListColumnSpecs(specs[0]);
		System.out.println("Input Columns: Size = "+columns.size());
		jComboBox_InputCol.removeAllItems();		
		for(DataColumnSpec ci : columns) { jComboBox_InputCol.addItem(ci.getName()); }    	
    	
		// init output combobox, i.e. find all supported 3d output types:
		jComboBox_OutputType.removeAllItems();
		for(OutputType3D oti : OutputType3D.values()) {
			jComboBox_OutputType.addItem( oti.toString() );
		}		
		
		this.m_Settings.loadSettingsForDialog(settings);			
		jComboBox_InputCol.setSelectedItem( this.m_Settings.getInputColumnName() );
		jComboBox_OutputType.setSelectedItem( this.m_Settings.getOutputType().toString() );
			
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {		
		this.m_Settings.setInputColumnName( (String) this.jComboBox_InputCol.getSelectedItem() );
		this.m_Settings.setOutputType( (String) jComboBox_OutputType.getSelectedItem() );
		this.m_Settings.saveSettings(settings);		
	}
    
    


    
}

