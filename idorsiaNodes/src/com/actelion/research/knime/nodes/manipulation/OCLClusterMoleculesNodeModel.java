package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.Clusterer;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.OCLDescriptorDataCell;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

//~--- JDK imports ------------------------------------------------------------

public class OCLClusterMoleculesNodeModel extends NodeModel {
    private static final int IN_PORT = 0;

    //~--- fields -------------------------------------------------------------

    private final OCLClusterMoleculesNodeSettings m_settings = new OCLClusterMoleculesNodeSettings();

    //~--- constructors -------------------------------------------------------

    protected OCLClusterMoleculesNodeModel() {
        super(1, 1);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean inputColumnNameFound = false;
        boolean descriptorColumnNameFound = false;
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        List<DataColumnSpec> moleculeColumnSpecs = SpecHelper.getMoleculeColumnSpecs(inputSpec);
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

        DataColumnSpec moleculeColumnSpec = inputSpec.getColumnSpec(inputColumnName);
        List<DataColumnSpec> descriptorColumnSpecs = SpecHelper.getDescriptorColumnSpecs(inputSpec, moleculeColumnSpec);
        for (DataColumnSpec descriptorColumnSpec : descriptorColumnSpecs) {
            if (SpecHelper.getDescriptorInfo(descriptorColumnSpec) == descriptor) {
                descriptorColumnNameFound = true;
            }
        }

        if (!descriptorColumnNameFound) {
            throw new InvalidSettingsException("The " + descriptor.shortName + " descriptor has not been calculated for column " + inputColumnName + ". Please calculate the descriptor before or select a different descriptor.");
        }

        ColumnRearranger columnRearranger = createColumnRearranger(inputSpec, null);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputDataTable = inData[IN_PORT];
        DataTableSpec inputDataTableSpec = inputDataTable.getDataTableSpec();
        DescriptorInfo descriptor = m_settings.getDescriptor();
        int structureCellIdx = SpecHelper.getColumnIndex(inputDataTableSpec, m_settings.getInputColumnName());
        int descriptorColumnIndex = SpecHelper.getDescriptorColumnIndex(inputDataTableSpec, structureCellIdx, descriptor);
        int clusterCutoff = m_settings.getClusterCutoff();
        float similarityCutoff = (m_settings.getSimilarityCutoff() < 0)
                ? 0.0f
                : m_settings.getSimilarityCutoff();
        int rowCount = inputDataTable.getRowCount();
        Object[] descriptorList = new Object[rowCount];
        CloseableRowIterator iterator = inputDataTable.iterator();
        int idx = 0;

        while (iterator.hasNext()) {
            DataRow next = iterator.next();
            OCLDescriptorDataCell descriptorDataCell = (OCLDescriptorDataCell) next.getCell(descriptorColumnIndex);
            descriptorList[idx++] = descriptorDataCell.getDescriptor();
        }

        System.out.println("init clusterer..");                
        Clusterer clusterer = new Clusterer(DescriptorHelpers.getDescriptorHandler(descriptor), descriptorList);

        clusterer.cluster(similarityCutoff, clusterCutoff);
        clusterer.regenerateClusterNos();
        iterator.close();

        ColumnRearranger c = createColumnRearranger(inputDataTableSpec, clusterer);
        BufferedDataTable out = exec.createColumnRearrangeTable(inputDataTable, c, exec);

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
        OCLClusterMoleculesNodeSettings tempSettings = new OCLClusterMoleculesNodeSettings();
        tempSettings.loadSettingsForModel(nodeSettingsRO);
        if (tempSettings.getClusterColumnName().isEmpty()) {
            throw new InvalidSettingsException("The cluster column name can not be empty");
        }
        if (tempSettings.getRepresentativeColumnName().isEmpty()) {
            throw new InvalidSettingsException("The representative column name can not be empty");
        }
    }

    private DataColumnSpec createBooleanTableSpec(DataTableSpec in, String columnTitle) {
        String columnName = DataTableSpec.getUniqueColumnName(in, columnTitle);

        return SpecHelper.createBooleanColumnSpec(columnName);
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec in, Clusterer clusterer) {
        ColumnRearranger c = new ColumnRearranger(in);
        DataColumnSpec clusterColumnSpec = createIntTableSpec(in, m_settings.getClusterColumnName());
        DataColumnSpec representaticColumnSpec = createBooleanTableSpec(in, m_settings.getRepresentativeColumnName());
        CellFactory clusterColumnCellFactory = new ClusterColumnCellFactory(clusterColumnSpec, clusterer);
        CellFactory representativeCellFactory = new RepresentativeColumnCellFactory(representaticColumnSpec, clusterer);

        c.append(clusterColumnCellFactory);
        c.append(representativeCellFactory);

        return c;
    }

    private DataColumnSpec createIntTableSpec(DataTableSpec in, String columnTitle) {
        String columnName = DataTableSpec.getUniqueColumnName(in, columnTitle);

        return SpecHelper.createIntColumnSpec(columnName);
    }

    //~--- inner classes ------------------------------------------------------

    public static class ClusterColumnCellFactory extends SingleCellFactory {
        private Clusterer clusterer;
        private int currentRowIdx;

        //~--- constructors ---------------------------------------------------

        public ClusterColumnCellFactory(DataColumnSpec columnSpec, Clusterer clusterer) {
            super(columnSpec);
            this.clusterer = clusterer;
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public IntCell getCell(DataRow dataRow) {
            return new IntCell(clusterer.getClusterNo(currentRowIdx++));
        }
    }


    public static class RepresentativeColumnCellFactory extends SingleCellFactory {
        private Clusterer clusterer;
        private int currentRowIdx;

        //~--- constructors ---------------------------------------------------

        public RepresentativeColumnCellFactory(DataColumnSpec columnSpec, Clusterer clusterer) {
            super(columnSpec);
            this.clusterer = clusterer;
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public BooleanCell getCell(DataRow dataRow) {
            return clusterer.isRepresentative(currentRowIdx++)
                    ? BooleanCell.TRUE
                    : BooleanCell.FALSE;
        }
    }
}
