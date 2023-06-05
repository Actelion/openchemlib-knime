package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.knime.computation.ComputationPool;
import com.actelion.research.knime.computation.PhesaDefaultEvaluatorActionProvider;
import com.actelion.research.knime.computation.ScaffoldAnalyzer;
import com.actelion.research.knime.computation.ScaffoldAnalyzerActionProvider;
import com.actelion.research.knime.data.FrequencyItem;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.data.ScaffoldType;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;

import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.util.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

//~--- JDK imports ------------------------------------------------------------

public class OCLScaffoldAnalysisNodeModel extends NodeModel {
    private static final int IN_PORT = 0;
    private static NodeLogger LOG = NodeLogger.getLogger(OCLScaffoldAnalysisNodeModel.class);

    //~--- fields -------------------------------------------------------------
    private final OCLScaffoldAnalysisNodeSettings m_settings = new OCLScaffoldAnalysisNodeSettings();

    //~--- constructors -------------------------------------------------------

    public OCLScaffoldAnalysisNodeModel() {
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

        ColumnRearranger columnRearranger = createAnalysisColumnRearranger(inputSpec);

        return new DataTableSpec[]{columnRearranger.createSpec(), createFrequencyTableDataSpecs()};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputDataTable = inData[IN_PORT];
        DataTableSpec inputTableSpecs = inputDataTable.getDataTableSpec();
        final int moleculeColumnIdx = SpecHelper.getColumnIndex(inputTableSpecs, m_settings.getInputColumnName());
        ScaffoldType scaffoldType = m_settings.getScaffoldType();
        AnalysisResult analysisResult = runScaffoldAnalysis(exec, inputDataTable, moleculeColumnIdx, scaffoldType);
        ColumnRearranger analysisColumnRearranger = createAnalysisColumnRearranger(inputTableSpecs, analysisResult.fragmentList);
        BufferedDataTable analysisDataTable = exec.createColumnRearrangeTable(inData[IN_PORT], analysisColumnRearranger, exec);
        DataTableSpec frequencyTableDataSpecs = createFrequencyTableDataSpecs();

        int counter = 1;
        BufferedDataContainer frequencyDataContainer = exec.createDataContainer(frequencyTableDataSpecs);

        for (FrequencyItem frequencyItem : analysisResult.frequencyItems) {
            exec.checkCanceled();
            DataCell[] cells = new DataCell[2];

            //cells[0] = ValueHelper.createMoleculeDataCell(frequencyItem.getIdCode());
            cells[0] = ValueHelper.createFragmentMoleculeDataCell(frequencyItem.getIdCode());
            cells[1] = ValueHelper.createIntDataCell(frequencyItem.getCount());

            RowKey key = new RowKey("Row " + counter++);
            DataRow row = new DefaultRow(key, cells);

            frequencyDataContainer.addRowToTable(row);
        }

        frequencyDataContainer.close();

        BufferedDataTable frequencyDataTable = frequencyDataContainer.getTable();

        return new BufferedDataTable[]{analysisDataTable, frequencyDataTable};
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
        (new OCLScaffoldAnalysisNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private ColumnRearranger createAnalysisColumnRearranger(DataTableSpec in) {
        return createAnalysisColumnRearranger(in, Collections.<String, String>emptyMap());
    }

    private ColumnRearranger createAnalysisColumnRearranger(DataTableSpec in, Map<String, String> fragmentList) {
        ColumnRearranger c = new ColumnRearranger(in);
        ScaffoldType scaffoldType = m_settings.getScaffoldType();
        String ringSystemColumnName = scaffoldType.toString();

        ringSystemColumnName = SpecHelper.getUniqueColumnName(in, ringSystemColumnName);

        DataColumnSpec ringSystemSpec = SpecHelper.createMoleculeColumnSpec(ringSystemColumnName);
        CellFactory factory = new RingSystemCellFactory(ringSystemSpec, fragmentList);

        c.append(factory);

        return c;
    }

    private DataTableSpec createFrequencyTableDataSpecs() {
        DataColumnSpec[] columnSpecs = new DataColumnSpec[2];
        int columnSpecIdx = 0;

        columnSpecs[columnSpecIdx++] = SpecHelper.createMoleculeColumnSpec("Structure");
        columnSpecs[columnSpecIdx] = SpecHelper.createIntColumnSpec("Frequency");

        DataTableSpec outputSpec = new DataTableSpec(columnSpecs);

        return outputSpec;
    }

    private AnalysisResult runScaffoldAnalysis(ExecutionContext exec, BufferedDataTable dataTable, int moleculeColumnIdx, ScaffoldType scaffoldType) throws CanceledExecutionException {
        CloseableRowIterator rowIterator = null;
        Vector<FrequencyItem> frequencyItems = new Vector<FrequencyItem>();
        Map<String, String> fragmentList = new ConcurrentHashMap<String, String>();
        List<MoleculeRow> moleculeRows = new ArrayList<>();

        try {
            rowIterator = dataTable.iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();

                DataCell cell = row.getCell(moleculeColumnIdx);
                if(!cell.isMissing()) {
	                if (cell.getType().isCompatible(OCLMoleculeDataValue.class)) {
	                    moleculeRows.add(new MoleculeRow(row.getKey(), ((OCLMoleculeDataCell) cell).getIdCode()));
	                }
                }
                else {
                	moleculeRows.add(null);
                }
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }

//        ScaffoldAnalyzer fb = new ScaffoldAnalyzer(moleculeRows, scaffoldType, 0, moleculeRows.size(), frequencyItems, fragmentList);
//        ComputationPool pool = ComputationPool.getInstance();
//        pool.execute(fb);
//        while (!fb.isDone() && !fb.isCancelled()) {
//            try {
//                exec.checkCanceled();
//            } catch (CanceledExecutionException e) {
//                fb.requestStop();
//                throw e;
//            }
//        }
        ThreadPool pool = org.knime.core.util.ThreadPool.currentPool().createSubPool();
        //int min_elements_per_job = ()
        int chunk_size = (int) Math.max( 16 , Math.min(  moleculeRows.size()*0.01 ,256) );
        //System.out.println("chunk size: "+chunk_size);
        //LOG.info("chunk size: "+chunk_size);
        ScaffoldAnalyzerActionProvider taskprovider = new ScaffoldAnalyzerActionProvider(moleculeRows,scaffoldType, chunk_size, frequencyItems, fragmentList);
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
        Collections.sort(frequencyItems);
        Collections.reverse(frequencyItems);

        return new AnalysisResult(fragmentList, frequencyItems);
    }

    //~--- inner classes ------------------------------------------------------

    private class AnalysisResult {
        private Map<String, String> fragmentList;
        private List<FrequencyItem> frequencyItems;

        //~--- constructors ---------------------------------------------------

        public AnalysisResult(Map<String, String> fragmentList, List<FrequencyItem> frequencyItems) {
            this.fragmentList = fragmentList;
            this.frequencyItems = frequencyItems;
        }
    }

    private class RingSystemCellFactory extends SingleCellFactory {
        private int currentRowIdx;
        private Map<String, String> fragmentList;

        //~--- constructors ---------------------------------------------------

        public RingSystemCellFactory(DataColumnSpec columnSpec, Map<String, String> fragmentList) {
            super(columnSpec);
            this.fragmentList = fragmentList;
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public DataCell getCell(DataRow dataRow) {
        	if(dataRow==null) {
        		return DataType.getMissingCell();
        	}
            String fragmentIdCode = fragmentList.get(dataRow.getKey().toString());
            if ((fragmentIdCode == null) || fragmentIdCode.equals("")) {
                return DataType.getMissingCell();
            }

            return ValueHelper.createFragmentMoleculeDataCell(fragmentIdCode);
        }
    }
}
