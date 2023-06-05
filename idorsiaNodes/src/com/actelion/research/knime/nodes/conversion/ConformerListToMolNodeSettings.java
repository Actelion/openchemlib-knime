package com.actelion.research.knime.nodes.conversion;


import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataValue;
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


public class ConformerListToMolNodeSettings extends AbstractOCLNodeSettings {
	
    public static final String  PROP_INPUT_COLUMN_NAME     = "inputColumnName";
    public static final String  PROP_OUTPUT_TYPE           = "outputType";
    
    private String inputColumnName = "";      
    private String outputType = "Sdf";
        
        
    public void setInputColumnName(String name) { this.inputColumnName = name; }
    
    public String getInputColumnName() { return this.inputColumnName; }

    public OutputType3D getOutputType() { return OutputType3D.fromString( outputType ); }
    
    public void setOutputType( String type) { this.outputType = type; }
    
	@Override
	public void loadSettingsForDialog(NodeSettingsRO settings) {
		this.inputColumnName = settings.getString(PROP_INPUT_COLUMN_NAME,"");
		this.outputType = settings.getString(PROP_OUTPUT_TYPE,"Sdf");
	}

	@Override
	public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		this.inputColumnName = settings.getString(PROP_INPUT_COLUMN_NAME,"");	
		this.outputType      = settings.getString(PROP_OUTPUT_TYPE,"Sdf"); 
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		settings.addString(PROP_INPUT_COLUMN_NAME, this.inputColumnName);
		settings.addString(PROP_OUTPUT_TYPE, outputType.toString());
	}
    
	
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		
	}
	
    public enum OutputType3D {
        MOL("Mol", MolValue.class), SDF("Sdf", SdfValue.class)
        ;

        private String                     description;
        private Class<? extends DataValue> type;

        //~--- constructors ---------------------------------------------------

        OutputType3D(String description, Class<? extends DataValue> type) {
            this.description = description;
            this.type        = type;
        }

        //~--- methods --------------------------------------------------------

        public static OutputType3D fromString(String s) {
            if (s == null) {
                return null;
            }

            for (OutputType3D outputType : OutputType3D.values()) {
                if (outputType.description.equals(s)) {
                    return outputType;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return description;
        }
    }	
    
    
    
}
