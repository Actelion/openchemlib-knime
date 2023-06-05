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
import com.actelion.research.knime.computation.ForceFieldMinimizationTask.MinimizationResult;
import com.actelion.research.knime.data.ConformerListRow;
import com.actelion.research.knime.nodes.calculation.OCLForceFieldMinimizationNodeSettings;

public class ForceFieldMinimizationActionProvider extends AbstractStoppableChunkedActionProvider {

// Author: tl	
	
private static NodeLogger LOG = NodeLogger.getLogger(ForceFieldMinimizationTask.class);	
	
    private final List<ConformerListRow> moleculeRows;
    
    private final OCLForceFieldMinimizationNodeSettings settings;
    private final Map<String, List<StereoMolecule>> conformers_out;
    private final Map<String, List<MinimizationResult>> optim_results_out;

    
    public ForceFieldMinimizationActionProvider(List<ConformerListRow> moleculeRows, OCLForceFieldMinimizationNodeSettings settings, int chunk_size, Map<String,List<StereoMolecule>> conf_out, Map<String,List<MinimizationResult>> optim_results_out) {
        super(moleculeRows.size(),chunk_size);
        this.moleculeRows = moleculeRows;
        this.settings = settings;
        this.conformers_out = conf_out;
        this.optim_results_out = optim_results_out;        
    }

    public static synchronized void initForceField(String tableSet) {
    	ForceFieldMMFF94.loadTable(tableSet, Tables.newMMFF94(tableSet));
    }

	@Override
	protected boolean processChunk(int start, int end) throws Exception {
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
    	
    	
        for (int idx = start; idx < end; idx++) {
            if (isStopRequested()) {
                throw new CanceledExecutionException();
                //return;
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
    				ForceFieldMinimizationTask.minimize(smi_minimized, null, this.settings.getMinimizationMethod() , mMMFFOptions,result );

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
        return true;
	}
	
    
    
}
