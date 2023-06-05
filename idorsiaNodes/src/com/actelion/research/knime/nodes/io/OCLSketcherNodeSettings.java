package com.actelion.research.knime.nodes.io;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

public class OCLSketcherNodeSettings extends AbstractOCLNodeSettings {

	private static final String STRUCTURE_IDCODE = "structureIDCode";
   
	private String structureIDCode = "";
	
	@Override
	public void loadSettingsForDialog(NodeSettingsRO settings) {
		// TODO Auto-generated method stub
		loadSettings(settings);
	}

	@Override
	public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		settings.addString(STRUCTURE_IDCODE,this.structureIDCode);
	}
	
	private void loadSettings(NodeSettingsRO settings) {
        this.structureIDCode = settings.getString(STRUCTURE_IDCODE,"");
    }

	public String getStructureIDCode() {
		return structureIDCode;
	}

	public void setIDCode(String idCode) {
		this.structureIDCode = idCode;
	}
	
	

}
