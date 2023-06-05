package com.actelion.research.knime.nodes.manipulation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.actelion.research.chem.DiversitySelector;
import com.actelion.research.chem.SortedStringList;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.OCLDescriptorDataValue;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.OCLNodeModel;
import com.actelion.research.knime.nodes.manipulation.OCLDiverseSelectionNodeModel.ClusterColumnCellFactory;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;

/**
 * <code>NodeModel</code> for the "OCLNewDiverseSelection" node.
 *
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewDiverseSelectionNodeModel extends OCLNodeModel {
    
	private static final int CPD_PORT = 0;
    private static final int EXCLUSION_PORT = 1;

    //~--- fields -------------------------------------------------------------

    private final OCLNewDiverseSelectionNodeSettings m_settings = new OCLNewDiverseSelectionNodeSettings();

    //~--- constructors -------------------------------------------------------

    protected OCLNewDiverseSelectionNodeModel() {
        super(createOptionalPorts(2, 2), createOptionalPorts(1));
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean inputColumnNameFound = false;
        boolean descriptorColumnNameFound = false;
        DataTableSpec cpdTableSpec = inSpecs[CPD_PORT];
        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();


        //List<DataColumnSpec> moleculeColumnSpecs = SpecHelper.getMoleculeColumnSpecs(cpdTableSpec);
        List<DataColumnSpec> moleculeDescriptorColumnSpecs = SpecHelper.getDescriptorColumnSpecs(cpdTableSpec);
        if (moleculeDescriptorColumnSpecs.isEmpty()) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing a suitable molecule representation");
        }
        for (DataColumnSpec descriptorColumnSpec : moleculeDescriptorColumnSpecs) {
            if (Objects.equals(inputColumnName, descriptorColumnSpec.getName())) {
                inputColumnNameFound = true;
                descriptorColumnNameFound = true;
            }
        }

        if (!inputColumnNameFound) {
            throw new InvalidSettingsException("Input table does not contain a column named " + inputColumnName + ". Please "
                    + "(re-)configure the node.");
        }

        DescriptorInfo descriptor = m_settings.getDescriptor();
        if (descriptor == null) {
            throw new InvalidSettingsException("No descriptor for similarity calculation selected");
        }

        //DataColumnSpec moleculeColumnSpec = cpdTableSpec.getColumnSpec(inputColumnName);
        //List<DataColumnSpec> descriptorColumnSpecs = SpecHelper.getDescriptorColumnSpecs(cpdTableSpec);//SpecHelper.getDescriptorColumnSpecs(cpdTableSpec, moleculeColumnSpec);
        //for (DataColumnSpec descriptorColumnSpec : descriptorColumnSpecs) {
        //    if (SpecHelper.getDescriptorInfo(descriptorColumnSpec) == descriptor) {
        //        descriptorColumnNameFound = true;
        //    }
        //}
        

//        if (!descriptorColumnNameFound) {
//            throw new InvalidSettingsException("The " + descriptor.shortName + " descriptor has not been calculated for column " + inputColumnName + ". Please calculate the descriptor before or select a different descriptor.");
//        }

        ColumnRearranger columnRearranger = createColumnRearranger(cpdTableSpec, null);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable cpdTable = inData[CPD_PORT];
        BufferedDataTable exclusionTable = inData[EXCLUSION_PORT];
        DescriptorInfo descriptorType = m_settings.getDescriptor();
        //int cpdTableMoleculeIdx = SpecHelper.getColumnIndex(cpdTable.getDataTableSpec(), m_settings.getInputColumnName()
        //);

        //int descriptorColumnIndex = SpecHelper.getDescriptorColumnIndex(cpdTable.getDataTableSpec(), cpdTableMoleculeIdx, descriptorType);
        int descriptorColumnIndex = SpecHelper.getColumnIndex(cpdTable.getDataTableSpec(), m_settings.getInputColumnName());
        int descriptorExclusionColumnIndex = SpecHelper.getColumnIndex(cpdTable.getDataTableSpec(), m_settings.getExclusionColumnName());
        Map<Integer, Integer> selectionMap = runSelection(cpdTable, exclusionTable, descriptorColumnIndex,
                descriptorExclusionColumnIndex, descriptorType);

//      DataTableSpec inputDataTableSpec = inputDataTable.getDataTableSpec();
//      DescriptorInfo descriptor = m_settings.getDescriptor();
//      int structureCellIdx = SpecHelper.getColumnIndex(m_settings.getInputColumnName(), inputDataTableSpec);
//      int clusterCutoff = m_settings.getClusterCutoff();
//      float similarityCutoff = (m_settings.getSimilarityCutoff() < 0)
//              ? 0.0f
//              : m_settings.getSimilarityCutoff();
//      int rowCount = inputDataTable.getRowCount();
//      Object[] descriptorList = new Object[rowCount];
//      CloseableRowIterator iterator = inputDataTable.iterator();
//      int idx = 0;
//
//      while (iterator.hasNext()) {
//          DataRow next = iterator.next();
//          OCLMoleculeDataCell cell = (OCLMoleculeDataCell) next.getCell(structureCellIdx);
//          Object descriptorValue = cell.getDescriptor(descriptor);
//
//          descriptorList[idx++] = descriptorValue;
//      }
//
//      Clusterer clusterer = new Clusterer(DescriptorHelpers.getDescriptorHander(descriptor), descriptorList);
//
//      clusterer.cluster(similarityCutoff, clusterCutoff);
//      clusterer.regenerateClusterNos();
//      iterator.close();
        ColumnRearranger c = createColumnRearranger(cpdTable.getDataTableSpec(), selectionMap);
        BufferedDataTable out = exec.createColumnRearrangeTable(cpdTable, c, exec);

        return new BufferedDataTable[]{out};
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
        (new OCLDiverseSelectionNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private DataColumnSpec createBooleanTableSpec(DataTableSpec in, String columnTitle) {
        String columnName = DataTableSpec.getUniqueColumnName(in, columnTitle);

        return SpecHelper.createBooleanColumnSpec(columnName);
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec in, Map<Integer, Integer> selectionMap) {
        ColumnRearranger c = new ColumnRearranger(in);
        DataColumnSpec diverseSelectionColumnSpec = createIntTableSpec(in, m_settings.getDiversityRankColumnName());
        CellFactory clusterColumnCellFactory = new ClusterColumnCellFactory(diverseSelectionColumnSpec, selectionMap);

        c.append(clusterColumnCellFactory);

        return c;
    }

    private DataColumnSpec createIntTableSpec(DataTableSpec in, String columnTitle) {
        String columnName = DataTableSpec.getUniqueColumnName(in, columnTitle);

        return SpecHelper.createIntColumnSpec(columnName);
    }

    private Map<Integer, Integer> runSelection(BufferedDataTable cpdTable, BufferedDataTable exclusionTable, int descriptorColIdx,
                                               int exclusionDescriptorColumnIdx, DescriptorInfo descriptorType) {
        int selectionCount = m_settings.getNoOfCompounds();
        DiversitySelector selector = new DiversitySelector();
        //TreeSet<String> uniqueCompoundList = new TreeSet<String>();

        
        
        if (exclusionTable != null) {
            selector.initializeExistingSet(512);

            CloseableRowIterator iterator = null;

            try {
                iterator = exclusionTable.iterator();

                int i = 0;

                while (iterator.hasNext()) {
                    DataRow dataRow = iterator.next();
                    //OCLMoleculeDataValue cell = ValueHelper.getOCLCell(dataRow.getCell(moleculeColumnIdx));
                    OCLDescriptorDataValue descriptorCell = (OCLDescriptorDataValue) dataRow.getCell(exclusionDescriptorColumnIdx);
                    long[] descriptor = (long[]) descriptorCell.getDescriptor();
                    //BitSet bs_descriptor = BitSet.valueOf(descriptor);
                    //if(!allDescriptorsToAddedPosition.containsKey(bs_descriptor)) {
                    	//descriptorToRows.put(bs_descriptor, new ArrayList<>());
                    selector.addToExistingSet(descriptor);
                    	//allDescriptors.put(bs_descriptor,);
                    //}                    
                    //                    String idcode = cell.getIdCode();
//                    if ((idcode != null) && !uniqueCompoundList.contains(idcode)) {
//                        uniqueCompoundList.add(idcode);
//                        //selector.addToExistingSet((int[]) descriptorCell.getDescriptor());
//                        selector.addToExistingSet((long[]) descriptorCell.getDescriptor());
//                    }
                }
            } finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }

        //int[] originalIndex = null;
        int[] selected = null;
        ArrayList<long[]> indexList = new ArrayList<long[]>();
        
        
        //int uniqueStructureCount = ;//getUniqueStructureCount(cpdTable, moleculeColumnIdx, descriptorColumnIdx, descriptorType);
        //originalIndex = new int[uniqueStructureCount];

        Set<BitSet> allDescriptors = new HashSet<>();
        Map<BitSet,List<Integer>> descriptorToRows = new HashMap<>();
        List<BitSet> consideredBitSets = new ArrayList();
        		
        
        if(true) {
	        CloseableRowIterator iterator = null;
	        try {
	            iterator = cpdTable.iterator();
	
	            int row = 0;
	            int i = 0;
	
	            while (iterator.hasNext()) {
	                DataRow dataRow = iterator.next();
	                //OCLMoleculeDataValue cell = ValueHelper.getOCLCell(dataRow.getCell(moleculeColumnIdx));
	                OCLDescriptorDataValue descriptorCell = (OCLDescriptorDataValue) dataRow.getCell(descriptorColIdx);
	                
	                long[] features = (long[]) descriptorCell.getDescriptor();
	                
	                BitSet descriptor_bs = BitSet.valueOf(features);
	                if(!allDescriptors.contains(descriptor_bs)) {
	                	descriptorToRows.put(descriptor_bs,new ArrayList<>());
	                	allDescriptors.add(descriptor_bs);
	                	consideredBitSets.add(descriptor_bs);
	                	indexList.add(features);
	                	i++;
	                }
	                descriptorToRows.get(descriptor_bs).add(row);
	                row++;
	                	               
	          //      indexList.add(features);
	//                String idcode = ""+i;//cell.getIdCode();
	//                if ((idcode != null) && !uniqueCompoundList.contains(idcode)) {
	//                    long[] features = (long[]) descriptorCell.getDescriptor();
	//
	//                    if (features != null) {
	//                        uniqueCompoundList.add(idcode);
	//                        originalIndex[indexList.size()] = i;
	//                        indexList.add(features);
	//                    }
	//                }
	                //i++;
	            }
	        } finally {
	            if (iterator != null) {
	                iterator.close();
	            }
	        }
	
	        long[][] featureList = new long[indexList.size()][];
	
	        for (int i = 0; i < indexList.size(); i++) {
	            featureList[i] = indexList.get(i);
	        }
        }

        int compoundsToSelect = Math.min( allDescriptors.size() , selectionCount);
//        int compoundsToSelect = (selectionCount == -1)
//                ? uniqueStructureCount
//                : selectionCount;

        //selected = selector.select(indexList.toArray(new int[0][]), compoundsToSelect);
        selected = selector.select(indexList.toArray(new long[0][]), compoundsToSelect);

        Map<Integer, Integer> selectionMap = new HashMap<Integer, Integer>();

        Random ri = new Random(123);
        if (selected != null) {
            for (int i = 0; i < selected.length; i++) {
            	// row idx -> rank
                //selectionMap.put(originalIndex[selected[i]], i + 1);
            	BitSet descr_i = BitSet.valueOf(indexList.get(i));
            	List<Integer> rows_i = descriptorToRows.get(descr_i);
            	int selected_row = rows_i.get(ri.nextInt(rows_i.size()));
            	selectionMap.put( selected_row , i + 1);
            }
        }

        return selectionMap;
    }

    //~--- get methods --------------------------------------------------------

    private int getUniqueStructureCount(BufferedDataTable cpdTable, int moleculeColumnIdx, int descriptorColumnIdx, DescriptorInfo descriptorType) {
        CloseableRowIterator iterator = null;
        int count = 0;
        SortedStringList uniqueCompoundList = new SortedStringList();

        try {
            iterator = cpdTable.iterator();

            while (iterator.hasNext()) {
                DataRow dataRow = iterator.next();
                //OCLMoleculeDataValue cell = ValueHelper.getOCLCell(dataRow.getCell(moleculeColumnIdx));
                OCLDescriptorDataValue descriptorCell = (OCLDescriptorDataValue) dataRow.getCell(descriptorColumnIdx);

                //String idCode = cell.getIdCode();
                String idCode = ""+count;
                Object descriptorValue = descriptorCell.getDescriptor();

                if ((idCode != null) && (descriptorValue != null) && (uniqueCompoundList.addString(idCode) != -1)) {
                    count++;
                }
            }
        } finally {
            if (iterator != null) {
                iterator.close();
            }
        }

        return count;
    }

    //~--- inner classes ------------------------------------------------------

    class ClusterColumnCellFactory extends SingleCellFactory {
        private int currentRowIdx;
        private Map<Integer, Integer> selectionMap;

        //~--- constructors ---------------------------------------------------

        public ClusterColumnCellFactory(DataColumnSpec columnSpec, Map<Integer, Integer> selectionMap) {
            super(columnSpec);
            this.selectionMap = selectionMap;
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public DataCell getCell(DataRow dataRow) {
            Integer rank = selectionMap.get(currentRowIdx++);

            if (rank == null) {
                return DataType.getMissingCell();
            }

            return new IntCell(rank);
        }
    }
    
}

