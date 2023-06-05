package com.actelion.research.knime.nodes.calculation;

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


public class OCLPhesaEvaluatorNodeSettings extends AbstractOCLNodeSettings {

    public static final String PROP_COLUMN_NAME_REF   = "inputColumnNameRef";
    public static final String PROP_COLUMN_NAME_QUERY = "inputColumnNameQuery";
    
    public static final String PROP_OUTPUT_MOL_REF     = "outputMolRef";
    public static final String PROP_OUTPUT_MOL_QUERY   = "outputMolQuery";
    
    private String inputColNameRef    = "";
    private String inputColNameQuery  = "";

    private boolean outputMolRef   = false;
    private boolean outputMolQuery = false;
	    
    public String getInputColNameRef() {return inputColNameRef;}
    public String getInputColNameQuery() {return inputColNameQuery;}
    
    public boolean isOutputMolRef() {return this.outputMolRef;}
    public boolean isOutputMolQuery() {return this.outputMolQuery;}
    
    public void setInputColNameRef(String name) {this.inputColNameRef = name;}
    public void setInputColNameQuery(String name) {this.inputColNameQuery = name;}
    
    public void setOutputMolRef(boolean out) { this.outputMolRef = out; }
    public void setOutputMolQuery(boolean out) { this.outputMolQuery = out; }
	
       
	@Override
	public void loadSettingsForDialog(NodeSettingsRO settings) {
		this.inputColNameRef     = settings.getString(PROP_COLUMN_NAME_REF,"");
		this.inputColNameQuery   = settings.getString(PROP_COLUMN_NAME_QUERY,"");
		this.outputMolRef        = settings.getBoolean(PROP_OUTPUT_MOL_REF,false);
		this.outputMolQuery      = settings.getBoolean(PROP_OUTPUT_MOL_QUERY,false);
	}

	@Override
	public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		this.inputColNameRef     = settings.getString(PROP_COLUMN_NAME_REF,"");
		this.inputColNameQuery   = settings.getString(PROP_COLUMN_NAME_QUERY,"");
		this.outputMolRef        = settings.getBoolean(PROP_OUTPUT_MOL_REF,false);
		this.outputMolQuery      = settings.getBoolean(PROP_OUTPUT_MOL_QUERY,false);		
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addString(PROP_COLUMN_NAME_REF    , this.inputColNameRef);
		settings.addString(PROP_COLUMN_NAME_QUERY  , this.inputColNameQuery);
		settings.addBoolean(PROP_OUTPUT_MOL_REF     , this.outputMolRef);
		settings.addBoolean(PROP_OUTPUT_MOL_QUERY   , this.outputMolQuery);		
	}
		
}
