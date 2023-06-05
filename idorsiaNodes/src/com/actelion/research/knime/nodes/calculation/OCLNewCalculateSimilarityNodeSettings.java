package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings;

public class OCLNewCalculateSimilarityNodeSettings extends AbstractOCLNodeSettings {
	
	private static final String FRAGMENT = "fragment";
	private static final String OUTPUT_COLUMN_NAME  = "columnNameOutput";
	public static final String DESCRIPTOR_COL_ID_A = "DescriptorA";
	//public static final String DESCRIPTOR_COL_ID_B = "DescriptorB";
	
	
	private String fragment = null;
	private DescriptorSettings descriptorSettings = new DescriptorSettings();
	private String columnNameOutput = "Calculated Similarity";
	
	public String getInputColumnName() {
		return descriptorSettings.getColumnName();
	}
	
	public String getOutputColumnName() {
		return columnNameOutput;
	}
	
	public String getFragment() {
		return fragment;
	}
	
	public void setFragment(String f) {
		this.fragment = f;
	}
	
	public void setOutputColumnName(String colname) {
		this.columnNameOutput = colname;
	}
	
	public void setDescriptorSettings(DescriptorSettings dsc_settings) {
		this.descriptorSettings = dsc_settings;
	}
	
	public DescriptorInfo getDescriptorInfo() {
		return descriptorSettings.getDescriptorInfo();
	}
	
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
		settings.addString(FRAGMENT,this.fragment);
		settings.addString(OUTPUT_COLUMN_NAME, this.columnNameOutput);
		this.descriptorSettings.saveSettings(DESCRIPTOR_COL_ID_A, settings);
	}
	
	private void loadSettings(NodeSettingsRO settings) {
		this.fragment = settings.getString(FRAGMENT,"");
		this.columnNameOutput = settings.getString(OUTPUT_COLUMN_NAME,"Computed Similarity");
		this.descriptorSettings = new DescriptorSettings();
		this.descriptorSettings.loadSettings(DESCRIPTOR_COL_ID_A, settings);
	}
	
	
	

}
