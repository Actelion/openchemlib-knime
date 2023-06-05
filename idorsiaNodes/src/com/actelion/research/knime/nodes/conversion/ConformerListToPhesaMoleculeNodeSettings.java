package com.actelion.research.knime.nodes.conversion;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class ConformerListToPhesaMoleculeNodeSettings extends AbstractOCLNodeSettings {
	
	private static final String PROP_INPUT_COLUMN = "inputColumn";
	
	private String inputColumn = "";
	
	
	public ConformerListToPhesaMoleculeNodeSettings() {
		super();
	}


	public String getInputColumnName() { return this.inputColumn; }
	
	public void setInputColumnName(String col) { this.inputColumn = col; }
	
	@Override
	public void loadSettingsForDialog(NodeSettingsRO settings) {
		// TODO Auto-generated method stub
		this.inputColumn = settings.getString(PROP_INPUT_COLUMN,"");
	}

	@Override
	public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		this.inputColumn = settings.getString(PROP_INPUT_COLUMN,"");
	}


	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(PROP_INPUT_COLUMN, this.inputColumn);
	}
	
	

}
