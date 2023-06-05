package com.actelion.research.knime.nodes.manipulation;

import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.knime.base.data.append.column.AppendedCellFactory;
import org.knime.base.data.append.column.AppendedColumnTable;
import org.knime.base.data.filter.row.FilterRowGenerator;
import org.knime.base.data.filter.row.FilterRowTable;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.ThreadPool;

import com.actelion.research.chem.SSSearcher;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.knime.computation.ComputationPool;
import com.actelion.research.knime.computation.SubstructureMatcher;
import com.actelion.research.knime.computation.SubstructureMatcherActionProvider;
import com.actelion.research.knime.computation.SubstructureMatcherWithFFP;
import com.actelion.research.knime.computation.SubstructureMatcherWithFFPActionProvider;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.OCLDescriptorDataValue;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.utils.SpecHelper;


/**
 *
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewSubstructureFilterListNodeModel extends NodeModel {
    private static final int IN_PORT = 0;
    private static final int FILTER_PORT = 1;
    private static NodeLogger LOG = NodeLogger.getLogger(OCLNewSubstructureFilterListNodeModel.class);

    //~--- fields -------------------------------------------------------------
    private final OCLNewSubstructureFilterListNodeSettings m_settings = new OCLNewSubstructureFilterListNodeSettings();

    //~--- constructors -------------------------------------------------------

    public OCLNewSubstructureFilterListNodeModel() {
        super(2, 2);
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


        boolean hasSubstructureColumn = false;
        boolean filterColumnFound = false;

        DataTableSpec filterSpec = inSpecs[FILTER_PORT];
        String filterColumnName = (m_settings == null)
                ? null
                : m_settings.getFilterColumnName();

        for (int a = 0; a < filterSpec.getNumColumns(); a++) {
            DataColumnSpec columnSpec = filterSpec.getColumnSpec(a);
            DataType dataType = columnSpec.getType();

            if (dataType.isCompatible(OCLMoleculeDataValue.class) || dataType.isCompatible(SdfValue.class)) {
                hasSubstructureColumn = true;
            }

            if ((filterColumnName != null) && filterColumnName.equals(columnSpec.getName())) {
                filterColumnFound = true;
            }
        }

        if (!hasSubstructureColumn) {
            throw new InvalidSettingsException("Substructure table must contain at "
                    + "least one column containing a suitable molecule representation");
        }

        if (!filterColumnFound) {
            throw new InvalidSettingsException("Substructure table does not contain a column named " + filterColumnName + ". Please "
                    + "(re-)configure the node.");
        }
        
        // check descriptor columns:
        boolean found_descriptor_input = false;
        boolean found_descriptor_filter = false;
        
        if( (m_settings != null) && m_settings.hasFFP_Input() ) {
        	String cname = m_settings.getFFPColumn_Input();
        	if(cname!=null) { 
        		int ci = inputSpec.findColumnIndex(cname);
        		if(ci>=0) {
        			boolean correct_descriptor = SpecHelper.getDescriptorInfo(inputSpec.getColumnSpec(ci)).shortName.equals( DescriptorHelper.DESCRIPTOR_FFP512.shortName );
        			if(!correct_descriptor) {
        				LOG.warn("Found wrong descriptor type! (input) -> "+SpecHelper.getDescriptorInfo(inputSpec.getColumnSpec(ci)).shortName);
        			}
        			found_descriptor_input = correct_descriptor;
        		}
        	}
        }
        if( (m_settings != null) && m_settings.hasFFP_Filter() ) {
        	String cname = m_settings.getFFPColumn_Filter();
        	if(cname!=null) { 
        		int ci = inputSpec.findColumnIndex(cname);
        		if(ci>=0) {
        			boolean correct_descriptor = SpecHelper.getDescriptorInfo(inputSpec.getColumnSpec(ci)).shortName.equals( DescriptorHelper.DESCRIPTOR_FFP512.shortName );
        			if(!correct_descriptor) {
        				LOG.warn("Found wrong descriptor type! (filter) -> "+SpecHelper.getDescriptorInfo(inputSpec.getColumnSpec(ci)).shortName);
        			}
        			found_descriptor_input = correct_descriptor;
        		}
        	}
        }
        // report outcome?
        LOG.info("Found FFP column Input:  "+found_descriptor_input);
        LOG.info("Found FFP column Filter: "+found_descriptor_filter);
        

        DataColumnSpec[] acceptedColumnSpecs = new DataColumnSpec[inputSpec.getNumColumns() + 1];
        for (int i = 0; i < inputSpec.getNumColumns(); i++) {
            acceptedColumnSpecs[i] = inputSpec.getColumnSpec(i);
        }
        acceptedColumnSpecs[inputSpec.getNumColumns()] = createMatchingRowsColumnSpec();
        DataTableSpec acceptedTableSpecs = new DataTableSpec(acceptedColumnSpecs);
        return new DataTableSpec[]{acceptedTableSpecs, inputSpec};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputDataTable = inData[IN_PORT];
        DataTableSpec inputTableSpecs = inputDataTable.getDataTableSpec();
        BufferedDataTable filterDataTable = inData[FILTER_PORT];
        DataTableSpec filterTableSpecs = filterDataTable.getDataTableSpec();

        int structureColumnIdx = SpecHelper.getColumnIndex(inputTableSpecs, m_settings.getInputColumnName());
        int filterColumnIdx = SpecHelper.getColumnIndex(filterTableSpecs, m_settings.getFilterColumnName());

        
        int structureFFPColumnIdx = -1;
        int filterFFPColumnIdx = -1;
        if(m_settings.hasFFP_Input()) {
        	structureFFPColumnIdx = SpecHelper.getColumnIndex(filterTableSpecs, m_settings.getFFPColumn_Input());
        }
        if(m_settings.hasFFP_Filter()) {
        	filterFFPColumnIdx = SpecHelper.getColumnIndex(filterTableSpecs, m_settings.getFFPColumn_Filter());
        }
        
        
        MoleculeRow[] fragments = new MoleculeRow[filterDataTable.getRowCount()];
        Object[] fragment_descriptors = new Object[filterDataTable.getRowCount()];
        
        CloseableRowIterator filterTableIterator = filterDataTable.iterator();
        int idx = 0;
        while (filterTableIterator.hasNext()) {
            exec.checkCanceled();
            DataRow row = filterTableIterator.next();
            DataCell cell = row.getCell(filterColumnIdx);
            if(!cell.isMissing()) {
	            if (cell instanceof OCLMoleculeDataValue) {
	                StereoMolecule substructure = ((OCLMoleculeDataValue) cell).getMolecule();
	                substructure.setFragment(true);
	                fragments[idx] = new MoleculeRow(row.getKey(), substructure.getIDCode());
	                if(filterFFPColumnIdx>=0) {fragment_descriptors[idx] = ((OCLDescriptorDataValue)row.getCell(filterFFPColumnIdx)).getDescriptor();}
	                idx++;
	            }
            }
        }

//        FilterRowTable acceptedTable = new FilterRowTable(inputDataTable, new SubstructureRowFilter(structureColumnIdx, fragments, true));
//        FilterRowTable rejectedTable = new FilterRowTable(inputDataTable, new SubstructureRowFilter(structureColumnIdx, fragments, false));

        Map<String, Vector<String>> matches = filter(exec, inputDataTable, fragments, fragment_descriptors, structureColumnIdx, structureFFPColumnIdx);

        AppendedColumnTable acceptedTable = new AppendedColumnTable(new FilterRowTable(inputDataTable, new MatchingRowFilter(true, matches)), new RowKeyAppendFactory(matches), createMatchingRowsColumnSpec());
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
        (new OCLSubstructureFilterListNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private DataColumnSpec createMatchingRowsColumnSpec() {
        return SpecHelper.createStringColumnSpec("Matching substructures");
    }

    private Map<String, Vector<String>> filter(ExecutionContext exec, BufferedDataTable inputDataTable, MoleculeRow[] fragments, Object[] fragment_ffps, int structureColumnIdx, int structureFFPColumnIdx) throws CanceledExecutionException {
        Map<String, Vector<String>> matches = new ConcurrentHashMap<>();
        CloseableRowIterator iterator = inputDataTable.iterator();
        MoleculeRow[] molecules = new MoleculeRow[inputDataTable.getRowCount()];
        Object[] mol_ffps = new Object[inputDataTable.getRowCount()];
        //Object[] frag_ffp = new Object[fragments.length];
        int idx = 0;
        while (iterator.hasNext()) {
            //try {
                exec.checkCanceled();
                DataRow row = iterator.next();
                DataCell cell = row.getCell(structureColumnIdx);
                if(!cell.isMissing()) {
	                if (cell instanceof OCLMoleculeDataValue) {
	                    molecules[idx] = new MoleculeRow(row.getKey(), ((OCLMoleculeDataValue) cell).getMolecule().getIDCode());
	                    matches.put(row.getKey().toString(), new Vector<String>());
	                    if(structureFFPColumnIdx>=0) {mol_ffps[idx] = ((OCLDescriptorDataValue)row.getCell(structureFFPColumnIdx)).getDescriptor();}
	                }
                }
                idx++;
            //} catch(Exception ex) {
            	
            //}
            //finally {
            	//iterator.close();
            //}
        }
        iterator.close();
        //SubstructureMatcher fb = new SubstructureMatcher(molecules, fragments, 0, molecules.length, matches);
        //SubstructureMatcherWithFFP fb = new SubstructureMatcherWithFFP(molecules, fragments,mol_ffps,fragment_ffps, 0, molecules.length, matches);               
        //ComputationPool pool = ComputationPool.getInstance();        
//      pool.execute(fb);        
//      while (!fb.isDone() && !fb.isCancelled()) {
//          try {
//              exec.checkCanceled();
//          } catch (CanceledExecutionException e) {
//              fb.requestStop();
//              throw e;
//          }
//      }
        
        LOG.info("Starting sss");
        ThreadPool pool = org.knime.core.util.ThreadPool.currentPool().createSubPool();
        int chunk_size = (int) Math.max( 8 , Math.min(  molecules.length*0.01 ,400) );
        //System.out.println("chunk size: "+chunk_size);
        LOG.info("chunk size: "+chunk_size);
        SubstructureMatcherWithFFPActionProvider taskprovider = new SubstructureMatcherWithFFPActionProvider(molecules, mol_ffps, fragments, fragment_ffps, chunk_size, matches);
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
        private final  Map<String, Vector<String>> matches;

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
            return matches.get(dataRow.getKey().getString()).isEmpty();
        }
    }

    private class SubstructureRowFilter implements FilterRowGenerator {
        private final List<StereoMolecule> substructures;
        private final boolean accept;
        private int structureColumnIdx;

        //~--- constructors ---------------------------------------------------

        public SubstructureRowFilter(int structureColumnIdx, List<StereoMolecule> substructures, boolean accept) {
            this.substructures = substructures;
            this.structureColumnIdx = structureColumnIdx;
            this.accept = accept;
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public boolean isIn(DataRow dataRow) {
            DataCell cell = dataRow.getCell(structureColumnIdx);
            if (cell instanceof OCLMoleculeDataValue) {
                for (StereoMolecule fragment : substructures) {
                    StereoMolecule molecule = ((OCLMoleculeDataValue) cell).getMolecule();
                    SSSearcher ssSearcher = new SSSearcher();

                    ssSearcher.setFragment(fragment);
                    ssSearcher.setMolecule(molecule);


                    if (accept && ssSearcher.isFragmentInMolecule(SSSearcher.cDefaultMatchMode)) {
                        return true;
                    } else if (!accept && ssSearcher.isFragmentInMolecule(SSSearcher.cDefaultMatchMode)) {
                        return false;
                    }
                }
            } else

            {
                return false;
            }

            return !accept;
        }
    }

    private class RowKeyAppendFactory implements AppendedCellFactory {

        private final Map<String, Vector<String>> matches;

        public RowKeyAppendFactory(Map<String, Vector<String>> matches) {
            this.matches = matches;
        }

        @Override
        public DataCell[] getAppendedCell(DataRow dataRow) {
            Vector<String> matchingRowKeys = matches.get(dataRow.getKey().getString());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < matchingRowKeys.size(); i++) {
                sb.append(matchingRowKeys.get(i));
                sb.append(",");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1);
            }
            DataCell dataCell = new StringCell(sb.toString());
            return new DataCell[]{dataCell};
        }
    }
}
