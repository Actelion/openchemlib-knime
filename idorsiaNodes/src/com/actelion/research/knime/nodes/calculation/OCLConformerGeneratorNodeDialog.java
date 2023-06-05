package com.actelion.research.knime.nodes.calculation;

import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.eclipse.core.internal.runtime.PrintStackUtil;
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

import com.actelion.research.knime.nodes.OCLNodeDialogPane;
import com.actelion.research.knime.utils.SpecHelper;

import info.clearthought.layout.TableLayout;

/**
 * 
 * @author Idorsia Pharmaceuticals Ltd
 */
public class OCLConformerGeneratorNodeDialog extends OCLNodeDialogPane {

	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected OCLConformerGeneratorNodeDialog() {                
        this.addTab("Conformer Generationg settings", this.createConformerGenerationTab());

    }

    JComboBox<String> jComboBox_SourceColumn      = new JComboBox<>();
    JComboBox<String> jComboBox_Algorithm         = new JComboBox<>(OCLConformerGeneratorNodeSettings.ALGORITHM_TEXT);
    JComboBox<String> jComboBox_InitialTorsions  = new JComboBox<>(OCLConformerGeneratorNodeSettings.TORSION_SOURCE_TEXT);
    
    JTextField        jTextField_MaxConformers    = new JTextField(""+OCLConformerGeneratorNodeSettings.DEFAULT_MAX_CONFORMERS);
    
    
    
    
	private Component createConformerGenerationTab() {		
		JPanel p = new JPanel();		
		
		double pref       = TableLayout.PREFERRED;
		double layout_a[][] = new double[][] { {4,pref,8,pref,4} , {4,pref,8,pref,8,pref,8,pref,4} } ;
		
		TableLayout layout = new TableLayout(layout_a);
		
		p.setLayout(layout);
		p.add(new JLabel("Source column: "),"1,1");
		p.add(jComboBox_SourceColumn,"3,1");
		p.add(new JLabel("Algorithm: "),"1,3");		
		p.add(jComboBox_Algorithm,"3,3");
		p.add(new JLabel("Initial torsions: "),"1,5");
		p.add(jComboBox_InitialTorsions,"3,5");
		p.add(new JLabel("Max Conformers: "),"1,7");
		p.add(jTextField_MaxConformers,"3,7");
					
		return p;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
				
		settings.addString(OCLConformerGeneratorNodeSettings.PROPERTY_INPUT_COLUMN_NAME, (String) jComboBox_SourceColumn.getSelectedItem());
		settings.addInt(OCLConformerGeneratorNodeSettings.PROPERTY_ALGORITHM, jComboBox_Algorithm.getSelectedIndex());
		settings.addInt(OCLConformerGeneratorNodeSettings.PROPERTY_TORSION_SOURCE, jComboBox_InitialTorsions.getSelectedIndex());
		
		int max_conf = 16;
		try {
			max_conf = Integer.parseInt(jTextField_MaxConformers.getText());
		}
		catch(Exception ex) {
			throw new InvalidSettingsException("Cannot parse max number of conformers");
		}
		
		settings.addInt(OCLConformerGeneratorNodeSettings.PROPERTY_MAX_CONFORMERS, max_conf);				
	}

	@Override
	protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs_out, DataTableSpec[] specs) throws NotConfigurableException {
		
		// get the algorithm options:
		int maxConformers = settings.getInt(OCLConformerGeneratorNodeSettings.PROPERTY_MAX_CONFORMERS, OCLConformerGeneratorNodeSettings.DEFAULT_MAX_CONFORMERS);		
		int algo              = settings.getInt(OCLConformerGeneratorNodeSettings.PROPERTY_ALGORITHM,OCLConformerGeneratorNodeSettings.DEFAULT_ALGORITHM);
		int initial_torstions = settings.getInt(OCLConformerGeneratorNodeSettings.PROPERTY_TORSION_SOURCE,OCLConformerGeneratorNodeSettings.DEFAULT_TORSION_SOURCE);
		
		jComboBox_Algorithm.setSelectedIndex(algo);
		jComboBox_InitialTorsions.setSelectedIndex(initial_torstions);
		jTextField_MaxConformers.setText(""+maxConformers);
		
		// init molecule columns and try to set the correct one:
		List<DataColumnSpec> mol_columns = SpecHelper.getMoleculeColumnSpecs(specs[0]);
		
		// init source column options:
		jComboBox_SourceColumn.removeAllItems();
		for(DataColumnSpec spi : mol_columns) { jComboBox_SourceColumn.addItem(spi.getName()); }
		
		// try to set the correct value:
		String sourceColumn = settings.getString(OCLConformerGeneratorNodeSettings.PROPERTY_INPUT_COLUMN_NAME,"");
		System.out.println("::initFromSettings(..) -> try to set input column to: "+sourceColumn);
			
		jComboBox_SourceColumn.setSelectedItem(sourceColumn);
		
	}
    
    
    
}

