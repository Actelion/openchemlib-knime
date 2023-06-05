package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

public class OCLConformerGeneratorNodeSettings extends AbstractOCLNodeSettings {

    public static final String PROPERTY_INPUT_COLUMN_NAME = "inputColumnName";    

    
    public static final String PROPERTY_ALGORITHM = "algorithm";
    public static final String PROPERTY_TORSION_SOURCE = "torsionSource";
	
    public static final String PROPERTY_POOL_CONFORMERS = "poolConformers";
    public static final String PROPERTY_MAX_CONFORMERS = "maxConformers";
    public static final String PROPERTY_LARGEST_FRAGMENT = "largestFragment";
    public static final String PROPERTY_NEUTRALIZE_FRAGMENT = "neutralize";
    public static final String PROPERTY_STEREO_ISOMER_LIMIT = "stereoIsomerLimit";

    public static final String[] TORSION_SOURCE_TEXT = { "From crystallographic database", "Use 60 degree steps" };
    public static final String[] TORSION_SOURCE_CODE = { "crystallDB", "6steps" };
    public static final int TORSION_SOURCE_CRYSTAL_DATA = 0;
    public static final int TORSION_SOURCE_6_STEPS = 1;
    public static final int DEFAULT_TORSION_SOURCE = TORSION_SOURCE_CRYSTAL_DATA;


    public static final int DEFAULT_MAX_CONFORMERS = 16;	
    public static final int MAX_CONFORMERS = 4096;
	public static final String DEFAULT_MAX_STEREO_ISOMERS = "16";

	public static final int LOW_ENERGY_RANDOM = 0;
	public static final int PURE_RANDOM = 1;
	public static final int ADAPTIVE_RANDOM = 2;
	public static final int SYSTEMATIC = 3;
	public static final int SELF_ORGANIZED = 4;
	public static final int ACTELION3D = 5;
	public static final int DEFAULT_ALGORITHM = LOW_ENERGY_RANDOM;
	public static final int FILE_TYPE_NONE = -1;

	public static final String[] ALGORITHM_TEXT = { "Random, low energy bias", "Pure random", "Adaptive collision avoidance, low energy bias", "Systematic, low energy bias", "Self-organized" };
	public static final String[] ALGORITHM_CODE = { "lowEnergyRandom", "pureRandom", "adaptiveRandom", "systematic", "selfOrganized", "actelion3d" };
	public static final boolean[] ALGORITHM_NEEDS_TORSIONS = { true, true, true, true, false, false };
	public static final String ALGORITHM_TEXT_ACTELION3D = "Actelion3D";    
    
	
    
	private String inputColumnName = null;
	
	private int algorithm = DEFAULT_ALGORITHM;
	
	private int maxConformers = DEFAULT_MAX_CONFORMERS;
	
	private int torsionSource = DEFAULT_TORSION_SOURCE;
	
	
	public void setInputColumnName(String inputColumnName) {this.inputColumnName = inputColumnName; }
	
	public String getInputColumnName() {return this.inputColumnName;}
	
	public int getAlgorithm() { return this.algorithm; }
		
	public void setAlgorithm(int algo) {this.algorithm = algo;}	
	
	public void setMaxConformers(int max_conf) {this.maxConformers = max_conf;}
	
	public int getMaxConformers() { return this.maxConformers; }	
	
	public void setTorsionSource( int torsionSrc ) {this.torsionSource = torsionSrc;}
	
	public int getTorsionSource() { return this.torsionSource; }
	
	
	
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		String input_col = settings.getString(PROPERTY_INPUT_COLUMN_NAME, "");
		if( input_col.isEmpty() ) {
			throw new InvalidSettingsException("No input column set!");
		}
	}
	
	@Override
	public void loadSettingsForDialog(NodeSettingsRO settings) {
		loadSettings(settings);
	}

	@Override
	public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
	    settings.addString(PROPERTY_INPUT_COLUMN_NAME, this.getInputColumnName());
	    settings.addInt(PROPERTY_ALGORITHM, this.getAlgorithm());
	    settings.addInt(PROPERTY_MAX_CONFORMERS, this.getMaxConformers());
	    settings.addInt(PROPERTY_TORSION_SOURCE, this.getTorsionSource());
	}
	
    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName = settings.getString(PROPERTY_INPUT_COLUMN_NAME, "");
        this.algorithm = settings.getInt(PROPERTY_ALGORITHM, DEFAULT_ALGORITHM);
        this.maxConformers = settings.getInt(PROPERTY_MAX_CONFORMERS , DEFAULT_MAX_CONFORMERS);
        this.torsionSource= settings.getInt(PROPERTY_TORSION_SOURCE, DEFAULT_TORSION_SOURCE);        
    }
	
}
