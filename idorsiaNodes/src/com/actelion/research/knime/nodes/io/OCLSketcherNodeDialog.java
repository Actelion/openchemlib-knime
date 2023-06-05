package com.actelion.research.knime.nodes.io;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.gui.DrawAreaEvent;
import com.actelion.research.gui.DrawAreaListener;
import com.actelion.research.gui.editor.EditorEvent;
import com.actelion.research.gui.generic.GenericEventListener;
import com.actelion.research.knime.nodes.view.JExtendedDrawPanel;

/**
 * 
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLSketcherNodeDialog extends NodeDialogPane {

	OCLSketcherNodeSettings m_Settings = new OCLSketcherNodeSettings();
	JExtendedDrawPanel edp;
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected OCLSketcherNodeDialog() {
        super();
        
        /*
		 * The DefaultNodeSettingsPane provides methods to add simple standard
		 * components to the dialog pane via the addDialogComponent(...) method. This
		 * method expects a new DialogComponet object that should be added to the dialog
		 * pane. There are many already predefined components for the most commonly used
		 * configuration needs like a text box (DialogComponentString) to enter some
		 * String or a number spinner (DialogComponentNumber) to enter some number in a
		 * specific range and step size.
		 * 
		 * The dialog components are connected to the node model via settings model
		 * objects that can easily load and save their settings to the node settings.
		 * Depending on the type of input the dialog component should receive, the
		 * constructor of the component requires a suitable settings model object. E.g.
		 * the DialogComponentString requires a SettingsModelString. Additionally,
		 * dialog components sometimes allow to further configure the behavior of the
		 * component in the constructor. E.g. to disallow empty inputs (like below).
		 * Here, the loading/saving in the dialog is already taken care of by the
		 * DefaultNodeSettingsPane. It is important to use the same key for the settings
		 * model here as used in the node model implementation (it does not need to be
		 * the same object). One best practice is to use package private static methods
		 * to create the settings model as we did in the node model implementation (see
		 * createNumberFormatSettingsModel() in the NodeModel class).
		 * 
		 * Here we create a simple String DialogComponent that will display a label
		 * String besides a text box in which the use can enter a value. The
		 * DialogComponentString has additional options to disallow empty inputs, hence
		 * we do not need to worry about that in the model implementation anymore.
		 * 
		 */
		
		
		
		JPanel jp_main   = new JPanel();
		JPanel jp_top    = new JPanel();
		JPanel jp_center = new JPanel();
		
		edp = new JExtendedDrawPanel();
		
		jp_main.setLayout(new BorderLayout());
		jp_main.add(jp_top,BorderLayout.NORTH);
		jp_main.add(jp_center,BorderLayout.CENTER);
		jp_center.setLayout(new BorderLayout());
		
		edp.getDrawPanel().getDrawArea().addDrawAreaListener(new GenericEventListener<EditorEvent>() {
			public void eventHappened(EditorEvent ev) {}
		});
		
		jp_center.add(edp,BorderLayout.CENTER);
		
		this.addTab("Sketcher", jp_main);
    }
    
    public JExtendedDrawPanel getExtendedDrawPanel() {
    	return this.getExtendedDrawPanel();
    }
    
    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
    	this.m_Settings.loadSettingsForDialog(settings);
    	
    	StereoMolecule mi = new StereoMolecule();
    	IDCodeParser icp = new IDCodeParser();
    	try {
    		icp.parse(mi,this.m_Settings.getStructureIDCode());
    	}
    	catch(Exception ex) {
    		System.out.println("[warn] parsing idcode failed..");
    	}
    	
    	mi.ensureHelperArrays(StereoMolecule.cHelperCIP);
    	this.edp.getDrawPanel().getDrawArea().setMolecule(mi);
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.m_Settings.setIDCode( this.edp.getDrawPanel().getDrawArea().getMolecule().getIDCode() );
		this.m_Settings.saveSettings(settings);
	}
    
    
    
}

