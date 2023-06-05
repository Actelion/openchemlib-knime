package com.actelion.research.knime.computation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.forcefield.mmff.ForceFieldMMFF94;
import com.actelion.research.chem.forcefield.mmff.Tables;
import com.actelion.research.knime.data.ConformerListRow;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.nodes.calculation.OCLConformerGeneratorNodeSettings;
import com.actelion.research.knime.nodes.calculation.OCLForceFieldMinimizationNodeSettings;
import com.actelion.research.util.DoubleFormat;

//Author: tl

public class ForceFieldMinimizationTask extends StoppableRecursiveAction {
	
    private static NodeLogger LOG = NodeLogger.getLogger(ForceFieldMinimizationTask.class);	
	
    private final List<ConformerListRow> moleculeRows;
    private final int start;
    private final int length;
    private final int maxComputations;
    private final OCLForceFieldMinimizationNodeSettings settings;
    private final Map<String, List<StereoMolecule>> conformers_out;
    private final Map<String, List<MinimizationResult>> optim_results_out;

    public ForceFieldMinimizationTask(List<ConformerListRow> conformerRows, OCLForceFieldMinimizationNodeSettings settings , int start, int length, Map<String,List<StereoMolecule>> conf_out, Map<String,List<MinimizationResult>> optim_results_out ) {
        this(null, conformerRows, settings, start, length, conf_out, optim_results_out );
    }

    private ForceFieldMinimizationTask(StoppableRecursiveAction parent, List<ConformerListRow> moleculeRows, OCLForceFieldMinimizationNodeSettings settings, int start, int length, Map<String,List<StereoMolecule>> conf_out, Map<String,List<MinimizationResult>> optim_results_out) {
        super(parent);
        this.moleculeRows = moleculeRows;
        this.settings = settings;
        this.start = start;
        this.length = length;
        this.conformers_out = conf_out;
        this.optim_results_out = optim_results_out;
        maxComputations = 4;
    }
    
    public static synchronized void initForceField(String tableSet) {
    	ForceFieldMMFF94.loadTable(tableSet, Tables.newMMFF94(tableSet));
    }

    protected void computeDirectly() {    	
    	// init forcefield
    	Map<String,Object> mMMFFOptions = new HashMap<>();
    	
		if (this.settings.getMinimizationMethod() == OCLForceFieldMinimizationNodeSettings.MINIMIZE_MMFF94sPlus ) {
			ForceFieldMMFF94.initialize(ForceFieldMMFF94.MMFF94SPLUS);
			mMMFFOptions = new HashMap<String, Object>();
		}
		else if (this.settings.getMinimizationMethod() == OCLForceFieldMinimizationNodeSettings.MINIMIZE_MMFF94s) {
			initForceField(ForceFieldMMFF94.MMFF94S);
			mMMFFOptions = new HashMap<String, Object>();
		}
    	
    	
        for (int idx = start; idx < start + length; idx++) {
            if (isStopRequested()) {
                completeExceptionally(new CanceledExecutionException());
                return;
            }
            ConformerListRow moleculeRow = moleculeRows.get(idx);
            List<StereoMolecule> conformers = moleculeRow.getConformers();
            
            String idcode = moleculeRow.getIDCode();

        	LOG.info("Minimize conformers for" + idcode +  " --> n="+conformers.size());
        	if(conformers.size()==0) {
        		LOG.info("zero conformers supplied -> continue..");        		
        	}
        	
        	List<StereoMolecule> minimized_conformers = new ArrayList<>();
        	List<MinimizationResult> optim_results    = new ArrayList<>();
        	
            for(int zi=0;zi<conformers.size();zi++) {
            	// compute minimization..
            	StereoMolecule smi = conformers.get(zi);
            	try {
            		StereoMolecule smi_minimized = new StereoMolecule(smi);
            		
            		// TODO: minimize!
    				MinimizationResult result = new MinimizationResult();
   					minimize(smi_minimized, null, this.settings.getMinimizationMethod() , mMMFFOptions,result );

    			    minimized_conformers.add(smi_minimized);
    			    LOG.info("OPTIM SUCCESS! E0="+result.energyBeforeOptim+" E1="+result.energy);
    			    System.out.println("OPTIM SUCCESS! E0="+result.energyBeforeOptim+" E1="+result.energy);    			    
    			    optim_results.add(result);
            	}
            	catch(Exception ex) {
            		LOG.warn("Forcefield Minimization failed for conformer " + smi.getIDCode()+ " : "+smi.getIDCoordinates() );
            		LOG.warn(ex.getMessage());
            		StereoMolecule smi_not_minimized = new StereoMolecule(smi);
            		minimized_conformers.add(smi_not_minimized);
            		//System.out.println("Forcefield Minimization failed for conformer " + smi.getIDCode()+ " : "+smi.getIDCoordinates() );
            		//System.out.println(ex.getMessage());            		
            	}            	
            }            
            this.conformers_out.put(idcode, minimized_conformers);
            this.optim_results_out.put(idcode, optim_results);           
        }
    	    	        	    	    	 
        LOG.info("Done with job");
        LOG.info("Still queued: " + getPool().getQueuedTaskCount());
        LOG.info("Still running: " + getPool().getRunningThreadCount());
    }	
	
    @Override
    protected void compute() {
        if (isStopRequested()) {
            completeExceptionally(new CanceledExecutionException());
            return;
        }

        if (length == 1 || (length) <= maxComputations) {
            computeDirectly();
            return;
        }
        int split = length / 2;
        invokeAll(new ForceFieldMinimizationTask(this, moleculeRows, settings, start, split, conformers_out, optim_results_out),
                new ForceFieldMinimizationTask(this, moleculeRows, settings, start + split, length - split, conformers_out, optim_results_out));
        LOG.info("Forking...");
    }
    
    
    
	/**
	 * Minimizes the molecule with the method defined in mMinimization.
	 * The starting conformer is taken from ffmol, if it is not null.
	 * When this method finishes, then the minimized atom coodinates are in mol,
	 * even if mMinimization == MINIMIZE_NONE.
	 * @param mol receives minimized coodinates; taken as start conformer if ffmol == null
	 * @param ffmol if not null this is taken as start conformer
	 * @param result receives energy and possibly error message
	 */
    public static void minimize(StereoMolecule mol, StereoMolecule ffmol, int mMinimization, Map<String,Object> mMMFFOptions, MinimizationResult result) throws Exception {
    	try {
    		if (mMinimization == OCLForceFieldMinimizationNodeSettings.MINIMIZE_MMFF94sPlus || mMinimization == OCLForceFieldMinimizationNodeSettings.MINIMIZE_MMFF94s) {
    			String tableSet = mMinimization == OCLForceFieldMinimizationNodeSettings.MINIMIZE_MMFF94sPlus ? ForceFieldMMFF94.MMFF94SPLUS : ForceFieldMMFF94.MMFF94S;
    			int[] fragmentNo = new int[mol.getAllAtoms()];
    			int fragmentCount = mol.getFragmentNumbers(fragmentNo, false, true);
    			if (fragmentCount == 1) {
    				ForceFieldMMFF94 ff = new ForceFieldMMFF94(mol, tableSet, mMMFFOptions);
    				int error = ff.minimise(10000, 0.0001, 1.0e-6);
    				if (error != 0)
    					throw new Exception("MMFF94 error code "+error);
    				result.energy = (float)ff.getTotalEnergy();
    			}
    			else {
    				int maxAtoms = 0;

    				StereoMolecule[] fragment = mol.getFragments(fragmentNo, fragmentCount);
    				for (StereoMolecule f:fragment) {
    					if (f.getAllAtoms() > 2) {
    						ForceFieldMMFF94 ff = new ForceFieldMMFF94(f, tableSet, mMMFFOptions);
    						int error = ff.minimise(10000, 0.0001, 1.0e-6);
    						if (error != 0)
    							throw new Exception("MMFF94 error code "+error);

    						if (maxAtoms < f.getAllAtoms()) {	// we take the energy value from the largest fragment
    							maxAtoms = f.getAllAtoms();
    							result.energy = (float)ff.getTotalEnergy();
    						}
    					}
    				}
    				int[] atom = new int[fragmentCount];
    				for (int i=0; i<fragmentNo.length; i++) {
    					int f = fragmentNo[i];
    					mol.setAtomX(i, fragment[f].getAtomX(atom[f]));
    					mol.setAtomY(i, fragment[f].getAtomY(atom[f]));
    					mol.setAtomZ(i, fragment[f].getAtomZ(atom[f]));
    					atom[f]++;
    				}
    			}
    		}
    	}
    	catch (Exception e) {
    		result.energy = Double.NaN;
    		result.errorMessage = e.getLocalizedMessage();

    		//if (mMinimizationErrors == 0)
    		e.printStackTrace();
    		//mMinimizationErrors++;
    	}
    }
    
	public static void copyFFMolCoordsToMol(StereoMolecule mol, StereoMolecule ffmol) {
		for (int atom=0; atom<mol.getAllAtoms(); atom++) {
			mol.setAtomX(atom, ffmol.getAtomX(atom));
			mol.setAtomY(atom, ffmol.getAtomY(atom));
			mol.setAtomZ(atom, ffmol.getAtomZ(atom));
		}
	}
    
    
	public static class MinimizationResult {
		public double energy;
		public double energyBeforeOptim;
		public String errorMessage = null;

		public int iterations = 0;
		public String result = "none";
		
		
		public MinimizationResult() {
			energy            = Double.NaN;
			energyBeforeOptim = Double.NaN;
			errorMessage = null;
		}
						
		public String energy() {
			return Double.isNaN(energy) ? "" : DoubleFormat.toString(energy);
		}

		public String energyBeforeOptim() {
			return Double.isNaN(energyBeforeOptim) ? "" : DoubleFormat.toString(energyBeforeOptim);
		}

		
		public String error() {
			return errorMessage == null ? "" : errorMessage;
		}
	}
	
}
