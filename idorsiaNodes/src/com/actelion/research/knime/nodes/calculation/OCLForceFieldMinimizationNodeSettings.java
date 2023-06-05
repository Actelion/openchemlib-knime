package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 */


public class OCLForceFieldMinimizationNodeSettings extends AbstractOCLNodeSettings {

	public static final String PROPERTY_MINIMIZATION_METHOD = "minimizationMethod";
	
	public static final String PROPERTY_INPUT_COLUMN = "inputColumn";
	
	public static final String PROPERTY_OVERWRITE_INPUT = "overwriteInput";
	
	public static final String PROPERTY_MAX_ITS = "optMaxIterations";

//	public static final String PROPERTY_MIN_RMS = "optMinRMS";
	
	public static final String PROPERTY_TOL_GRAD = "tolGrad";
	
	public static final String PROPERTY_TOL_FUNC    = "tolFunc";
	
	public static final String PROPERTY_OUT_ENERGY    = "outEnergyFinal";
	
	public static final String PROPERTY_OUT_ENERGY_AT_START  = "outEnergyAtStart";
	
	public static final String PROPERTY_OUT_OPT_RESULT = "outOptResult";
	
	public static final String PROPERTY_OUT_OPT_ITER   = "outOptIter";
		
	public static final String[] MINIMIZE_TEXT = { "MMFF94s+ forcefield", "MMFF94s forcefield", "Idorsia forcefield", "Don't minimize" };
	public static final String[] MINIMIZE_CODE = { "mmff94+", "mmff94", "actelion", "none" };
	public static final String[] MINIMIZE_TITLE = { "mmff94s+", "mmff94s", "Idorsia-FF", "not minimized" };
	
	public static final int MINIMIZE_MMFF94sPlus = 0;
	public static final int MINIMIZE_MMFF94s = 1;
	public static final int MINIMIZE_IDORSIA_FORCEFIELD = 2;
	public static final int MINIMIZE_NONE = 3;
	public static final int DEFAULT_MINIMIZATION = MINIMIZE_MMFF94sPlus;
		
	
	
	private int minimizationMethod = DEFAULT_MINIMIZATION;
	
	private String inputColumn;
	
	private boolean overwriteInput;
	
	
	
	private int maxIterations = 10000;

	private double minRMS     = 0.0001;
	private double tolGrad    = 0.0001;
	private double tolFunc    = 1.0e-6;
	
	private boolean outEnergy = true;
	
	private boolean outEnergyAtStart = false;
	
	private boolean outOptResult  = false;
	
	private boolean outOptIter    = false;
	
	
	
	public void setMinimizationMethod(int minimization) { this.minimizationMethod = minimization; }
	
	public int getMinimizationMethod() { return this.minimizationMethod; }
	
	public void setInputColumnName(String input) { this.inputColumn = input; }
	
	public String getInputColumnName() {return this.inputColumn;}
	
	public void setOverwriteInput(boolean overwrite) { this.overwriteInput = overwrite;	}
	
	public boolean isOverwriteInput() {return this.overwriteInput;}
	
	
	public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
	
	public int getMaxIterations() {return this.maxIterations;}
	
	public void setTolGrad(double tolGrad) { this.tolGrad = tolGrad; }
	
	public double getTolGrad() {return this.tolGrad;}
	
	public void setTolFunc(double tolFunc) { this.tolFunc = tolFunc;}
	
	public double getTolFunc() { return this.tolFunc; }	
	
	
	public void setOutEnergy(boolean out) { this.outEnergy = out; }
	
	public boolean isOutEnergy() { return this.outEnergy; }

	public void setOutEnergyAtStart(boolean out) { this.outEnergyAtStart = out; }
	
	public boolean isOutEnergyAtStart() { return this.outEnergyAtStart; }
	
	
	public void setOutOptResult(boolean out) { this.outOptResult = out; }
	
	public boolean isOutOptResult() { return this.outOptResult; }
	
	public void setOutOptIter(boolean out) { this.outOptIter = out; }
	
	public boolean isOutOptIter() { return this.outOptIter; }

	
	
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		
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
		settings.addInt(PROPERTY_MINIMIZATION_METHOD, this.minimizationMethod);
		settings.addString(PROPERTY_INPUT_COLUMN, this.inputColumn);
		settings.addBoolean(PROPERTY_OVERWRITE_INPUT, this.overwriteInput);
		
		settings.addInt(PROPERTY_MAX_ITS, this.maxIterations);
		settings.addDouble(PROPERTY_TOL_GRAD, this.tolGrad);
		settings.addDouble(PROPERTY_TOL_FUNC, this.tolFunc);
			
		settings.addBoolean(PROPERTY_OUT_ENERGY, this.outEnergy);
		settings.addBoolean(PROPERTY_OUT_ENERGY_AT_START, this.outEnergyAtStart);
		settings.addBoolean(PROPERTY_OUT_OPT_RESULT, this.outOptResult);
		settings.addBoolean(PROPERTY_OUT_OPT_ITER, this.outOptIter);		
	}
		
	private void loadSettings(NodeSettingsRO settings) {
		this.minimizationMethod = settings.getInt(PROPERTY_MINIMIZATION_METHOD,DEFAULT_MINIMIZATION);
		this.inputColumn = settings.getString(PROPERTY_INPUT_COLUMN,"");
		this.overwriteInput = settings.getBoolean(PROPERTY_OVERWRITE_INPUT,false);
		
		
		this.maxIterations = settings.getInt(PROPERTY_MAX_ITS, 10000);
		this.tolGrad = settings.getDouble(PROPERTY_TOL_GRAD, 0.0001);
		this.tolFunc = settings.getDouble(PROPERTY_TOL_FUNC, 1.0e-6);
		
		this.outEnergy        = settings.getBoolean(PROPERTY_OUT_ENERGY,true);
		this.outEnergyAtStart = settings.getBoolean(PROPERTY_OUT_ENERGY_AT_START,false); 
		this.outOptResult = settings.getBoolean(PROPERTY_OUT_OPT_RESULT,true);
		this.outOptIter   = settings.getBoolean(PROPERTY_OUT_OPT_ITER,false);
	}
	
}
