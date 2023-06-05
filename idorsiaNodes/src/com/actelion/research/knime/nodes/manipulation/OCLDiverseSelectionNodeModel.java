package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.DiversitySelector;
import com.actelion.research.chem.SortedStringList;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.OCLDescriptorDataValue;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.OCLNodeModel;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;

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
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

//~--- JDK imports ------------------------------------------------------------

public class OCLDiverseSelectionNodeModel extends OCLNodeModel {
    private static final int CPD_PORT = 0;
    private static final int EXCLUSION_PORT = 1;

    //~--- fields -------------------------------------------------------------

    private final OCLDiverseSelectionNodeSettings m_settings = new OCLDiverseSelectionNodeSettings();

    //~--- constructors -------------------------------------------------------

    protected OCLDiverseSelectionNodeModel() {
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


        List<DataColumnSpec> moleculeColumnSpecs = SpecHelper.getMoleculeColumnSpecs(cpdTableSpec);
        if (moleculeColumnSpecs.isEmpty()) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing a suitable molecule representation");
        }
        for (DataColumnSpec moleculeColumnSpec : moleculeColumnSpecs) {
            if (Objects.equals(inputColumnName, moleculeColumnSpec.getName())) {
                inputColumnNameFound = true;
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

        DataColumnSpec moleculeColumnSpec = cpdTableSpec.getColumnSpec(inputColumnName);
        List<DataColumnSpec> descriptorColumnSpecs = SpecHelper.getDescriptorColumnSpecs(cpdTableSpec, moleculeColumnSpec);
        for (DataColumnSpec descriptorColumnSpec : descriptorColumnSpecs) {
            if (SpecHelper.getDescriptorInfo(descriptorColumnSpec) == descriptor) {
                descriptorColumnNameFound = true;
            }
        }

        if (!descriptorColumnNameFound) {
            throw new InvalidSettingsException("The " + descriptor.shortName + " descriptor has not been calculated for column " + inputColumnName + ". Please calculate the descriptor before or select a different descriptor.");
        }

        ColumnRearranger columnRearranger = createColumnRearranger(cpdTableSpec, null);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable cpdTable = inData[CPD_PORT];
        BufferedDataTable exclusionTable = inData[EXCLUSION_PORT];
        DescriptorInfo descriptorType = m_settings.getDescriptor();
        int cpdTableMoleculeIdx = SpecHelper.getColumnIndex(cpdTable.getDataTableSpec(), m_settings.getInputColumnName()
        );

        int descriptorColumnIndex = SpecHelper.getDescriptorColumnIndex(cpdTable.getDataTableSpec(), cpdTableMoleculeIdx, descriptorType);
        Map<Integer, Integer> selectionMap = runSelection(cpdTable, exclusionTable, cpdTableMoleculeIdx,
                descriptorColumnIndex, descriptorType);

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

    private Map<Integer, Integer> runSelection(BufferedDataTable cpdTable, BufferedDataTable exclusionTable, int moleculeColumnIdx,
                                               int descriptorColumnIdx, DescriptorInfo descriptorType) {
        int selectionCount = m_settings.getNoOfCompounds();
        DiversitySelector selector = new DiversitySelector();
        TreeSet<String> uniqueCompoundList = new TreeSet<String>();

        if (exclusionTable != null) {
            selector.initializeExistingSet(512);

            CloseableRowIterator iterator = null;

            try {
                iterator = exclusionTable.iterator();

                int i = 0;

                while (iterator.hasNext()) {
                    DataRow dataRow = iterator.next();
                    OCLMoleculeDataValue cell = ValueHelper.getOCLCell(dataRow.getCell(moleculeColumnIdx));
                    OCLDescriptorDataValue descriptorCell = (OCLDescriptorDataValue) dataRow.getCell(descriptorColumnIdx);
                    String idcode = cell.getIdCode();

                    if ((idcode != null) && !uniqueCompoundList.contains(idcode)) {
                        uniqueCompoundList.add(idcode);
                        //selector.addToExistingSet((int[]) descriptorCell.getDescriptor());
                        selector.addToExistingSet((long[]) descriptorCell.getDescriptor());
                    }
                }
            } finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }

        int[] originalIndex = null;
        int[] selected = null;
        ArrayList<long[]> indexList = new ArrayList<long[]>();
        int uniqueStructureCount = getUniqueStructureCount(cpdTable, moleculeColumnIdx, descriptorColumnIdx, descriptorType);

        originalIndex = new int[uniqueStructureCount];

        CloseableRowIterator iterator = null;

        try {
            iterator = cpdTable.iterator();

            int i = 0;

            while (iterator.hasNext()) {
                DataRow dataRow = iterator.next();
                OCLMoleculeDataValue cell = ValueHelper.getOCLCell(dataRow.getCell(moleculeColumnIdx));
                OCLDescriptorDataValue descriptorCell = (OCLDescriptorDataValue) dataRow.getCell(descriptorColumnIdx);
                String idcode = cell.getIdCode();

                if ((idcode != null) && !uniqueCompoundList.contains(idcode)) {
                    long[] features = (long[]) descriptorCell.getDescriptor();

                    if (features != null) {
                        uniqueCompoundList.add(idcode);
                        originalIndex[indexList.size()] = i;
                        indexList.add(features);
                    }
                }

                i++;
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

        int compoundsToSelect = (selectionCount == -1)
                ? uniqueStructureCount
                : selectionCount;

        //selected = selector.select(indexList.toArray(new int[0][]), compoundsToSelect);
        selected = selector.select(indexList.toArray(new long[0][]), compoundsToSelect);

        Map<Integer, Integer> selectionMap = new HashMap<Integer, Integer>();

        if (selected != null) {
            for (int i = 0; i < selected.length; i++) {
                selectionMap.put(originalIndex[selected[i]], i + 1);
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
                OCLMoleculeDataValue cell = ValueHelper.getOCLCell(dataRow.getCell(moleculeColumnIdx));
                OCLDescriptorDataValue descriptorCell = (OCLDescriptorDataValue) dataRow.getCell(descriptorColumnIdx);

                String idCode = cell.getIdCode();
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
