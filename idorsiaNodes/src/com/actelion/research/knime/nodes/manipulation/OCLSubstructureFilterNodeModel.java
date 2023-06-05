package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.SSSearcher;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.computation.SubstructureMatcher;
import com.actelion.research.knime.computation.SubstructureMatcherActionProvider;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.utils.SpecHelper;

import org.knime.base.data.filter.row.FilterRowGenerator;
import org.knime.base.data.filter.row.FilterRowTable;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

//~--- JDK imports ------------------------------------------------------------

public class OCLSubstructureFilterNodeModel extends NodeModel {
    private static final int IN_PORT = 0;
    private static NodeLogger LOG = NodeLogger.getLogger(OCLMoleculeDataCell.class);

    //~--- fields -------------------------------------------------------------
    private final OCLSubstructureFilterNodeSettings m_settings = new OCLSubstructureFilterNodeSettings();


    //~--- constructors -------------------------------------------------------

    public OCLSubstructureFilterNodeModel() {
        super(1, 2);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean hasMoleculeColumn = false;
        boolean inputColumnNameFound = false;
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        for (int a = 0; a < inputSpec.getNumColumns(); a++) {
            DataColumnSpec columnSpec = inputSpec.getColumnSpec(a);
            DataType dataType = columnSpec.getType();

            if (dataType.isCompatible(OCLMoleculeDataValue.class) || dataType.isCompatible(SdfValue.class)) {
                hasMoleculeColumn = true;
            }

            if ((inputColumnName != null) && inputColumnName.equals(columnSpec.getName())) {
                inputColumnNameFound = true;
            }
        }

        if (!hasMoleculeColumn) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing a suitable molecule representation");
        }

        if (!inputColumnNameFound) {
            throw new InvalidSettingsException("Input table does not contain a column named " + inputColumnName + ". Please "
                    + "(re-)configure the node.");
        }

        return new DataTableSpec[]{inputSpec, inputSpec};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputDataTable = inData[IN_PORT];
        DataTableSpec inputTableSpecs = inputDataTable.getDataTableSpec();
        int structureColumnIdx = SpecHelper.getColumnIndex(inputTableSpecs, m_settings.getInputColumnName());
        StereoMolecule fragment = m_settings.getFragment();
        Map<String, Vector<String>> matches = filter(exec, inputDataTable, fragment, structureColumnIdx);

        FilterRowTable acceptedTable = new FilterRowTable(inputDataTable, new MatchingRowFilter(true, matches));
        FilterRowTable rejectedTable = new FilterRowTable(inputDataTable, new MatchingRowFilter(false, matches));

        BufferedDataTable accepted = exec.createBufferedDataTable(acceptedTable, exec);
        BufferedDataTable rejected = exec.createBufferedDataTable(rejectedTable, exec);

        return new BufferedDataTable[]{accepted, rejected};
    }

    @Override
    protected void loadInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        m_settings.loadSettingsForModel(nodeSettingsRO);
    }

    @Override
    protected void reset() {
    }

    @Override
    protected void saveInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO nodeSettingsWO) {
        m_settings.saveSettings(nodeSettingsWO);
    }

    @Override
    protected void validateSettings(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        (new OCLSubstructureFilterNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private Map<String, Vector<String>> filter(ExecutionContext exec, BufferedDataTable inputDataTable, StereoMolecule fragment, int structureColumnIdx) {
        Map<String, Vector<String>> matches = new ConcurrentHashMap<>();
        CloseableRowIterator iterator = inputDataTable.iterator();
        MoleculeRow[] fragments = new MoleculeRow[]{new MoleculeRow(RowKey.createRowKey(0), fragment.getIDCode())};
        MoleculeRow[] molecules = new MoleculeRow[inputDataTable.getRowCount()];
        int idx = 0;
        while (iterator.hasNext()) {
            try {
                exec.checkCanceled();
                DataRow row = iterator.next();
                DataCell cell = row.getCell(structureColumnIdx);
                if(!cell.isMissing()) {                	                
	                if (cell instanceof OCLMoleculeDataValue) {
	                    molecules[idx] = new MoleculeRow(row.getKey(), ((OCLMoleculeDataValue) cell).getMolecule().getIDCode());
	                    matches.put(row.getKey().toString(), new Vector());
	                }
                }
                idx++;
                if (idx % 1000 == 0)
                    LOG.info("Process molecule " + idx);
            } catch (CanceledExecutionException e) {
                return null;
            }

        }
        LOG.info("Starting sss");
        //SubstructureMatcher fb = new SubstructureMatcher(molecules, fragments, 0, molecules.length, matches);
        //ForkJoinPool pool = new ForkJoinPool();
        ThreadPool pool = org.knime.core.util.ThreadPool.currentPool().createSubPool();
        int chunk_size = (int) Math.max( 8 , Math.min(  molecules.length*0.01 ,400) );
        //System.out.println("chunk size: "+chunk_size);
        LOG.info("chunk size: "+chunk_size);
        SubstructureMatcherActionProvider taskprovider = new SubstructureMatcherActionProvider(molecules, chunk_size, fragments, matches);
        //pool.invoke(fb);
        taskprovider.getTasks().forEach( ti -> pool.enqueue(ti) );
        
        try {
			pool.waitForTermination();			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pool.shutdown();
        
                
        LOG.info("All done. Returning matching map");
        return matches;
    }

    //~--- inner classes ------------------------------------------------------

    private class MatchingRowFilter implements FilterRowGenerator {

        private final boolean accepting;
        private final Map<String, Vector<String>> matches;

        public MatchingRowFilter(boolean accepting, Map<String, Vector<String>> matches) {
            this.accepting = accepting;
            this.matches = matches;
        }

        @Override
        public boolean isIn(DataRow dataRow) {
        	// this is to handle "missing value" entries in the input.
        	// this should be the only case in which we do not find a key.
        	if(!matches.containsKey(dataRow.getKey().getString())) {
        		return false;
        	}
            if (accepting) {            	
                return !matches.get(dataRow.getKey().getString()).isEmpty();
            }
            else {            
            	return matches.get(dataRow.getKey().getString()).isEmpty();
            }
        }
    }

    private class SubstructureRowFilter implements FilterRowGenerator {
        private final StereoMolecule fragment;
        private boolean accept;
        private int structureColumnIdx;

        //~--- constructors ---------------------------------------------------

        public SubstructureRowFilter(int structureColumnIdx, StereoMolecule fragment, boolean accept) {
            this.structureColumnIdx = structureColumnIdx;
            this.fragment = fragment;
            this.accept = accept;
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public boolean isIn(DataRow dataRow) {
            DataCell cell = dataRow.getCell(structureColumnIdx);

            if (cell instanceof OCLMoleculeDataValue) {
                StereoMolecule molecule = ((OCLMoleculeDataValue) cell).getMolecule();
                SSSearcher ssSearcher = new SSSearcher();

                ssSearcher.setFragment(fragment);
                ssSearcher.setMolecule(molecule);

                if (accept) {
                    return ssSearcher.isFragmentInMolecule(SSSearcher.cDefaultMatchMode);
                } else {
                    return !ssSearcher.isFragmentInMolecule(SSSearcher.cDefaultMatchMode);
                }
            }

            return false;
        }
    }


}
