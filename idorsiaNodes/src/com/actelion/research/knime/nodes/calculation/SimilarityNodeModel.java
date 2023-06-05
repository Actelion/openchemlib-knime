package com.actelion.research.knime.nodes.calculation;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.computation.ComputationPool;
import com.actelion.research.knime.computation.DescriptorCalculatorActionProvider;
import com.actelion.research.knime.computation.SimilarityCalculator;
import com.actelion.research.knime.computation.SimilarityCalculatorActionProvider;
import com.actelion.research.knime.data.DescriptorRow;
import com.actelion.research.knime.data.OCLDescriptorDataCell;
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
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeModel;
import org.knime.core.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SimilarityNodeModel extends NodeModel {

    protected SimilarityNodeModel(int nrInDataPorts, int nrOutDataPorts) {
        super(nrInDataPorts, nrOutDataPorts);
    }

    protected ColumnRearranger createColumnRearranger(DataTableSpec in, String columnName, boolean multiSim) {
        ColumnRearranger c = new ColumnRearranger(in);
        DataColumnSpec newSpec = createColumnSpec(in, columnName, multiSim);
        CellFactory factory = new SimilarityCellFactory(newSpec, null, multiSim);
        c.append(factory);
        return c;
    }

    //
    protected ColumnRearranger createColumnRearranger(DataTableSpec in, String columnName, boolean multiSim, Map<String, Double[]> similarities) {
        ColumnRearranger c = new ColumnRearranger(in);
        DataColumnSpec newSpec = createColumnSpec(in, columnName, multiSim);
        CellFactory factory = new SimilarityCellFactory(newSpec, similarities, multiSim);
        c.append(factory);
        return c;
    }

    protected DataColumnSpec createColumnSpec(DataTableSpec in, String columnTitle, boolean multiSim) {
        String columnName = DataTableSpec.getUniqueColumnName(in, columnTitle);
        if (multiSim) {
            return SpecHelper.createDoubleVectorColumnSpec(columnName);
        } else {
            return SpecHelper.createDoubleColumnSpec(columnName);
        }
    }

    Map<String, Double[]> runSimilarityAnalysis(BufferedDataTable dataTable, int moleculeColumnIdx, DescriptorInfo descriptorInfo, List<Object> queryDescriptors, ExecutionContext exec) throws CanceledExecutionException {
        CloseableRowIterator rowIterator = null;
        Map<String, Double[]> similarites = new ConcurrentHashMap<String, Double[]>();
        List<DescriptorRow> descriptorRows = new ArrayList<>();

        int descriptorColumnIdx = SpecHelper.getDescriptorColumnIndex(dataTable.getDataTableSpec(), moleculeColumnIdx, descriptorInfo);

        try {
            rowIterator = dataTable.iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();

                DataCell cell = row.getCell(descriptorColumnIdx);
                if(!cell.isMissing()) {
                	descriptorRows.add(new DescriptorRow(row.getKey(), descriptorInfo, ((OCLDescriptorDataCell) cell).getEncodedDescriptor().getBytes()));
                }
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }

        SimilarityCalculator fb = new SimilarityCalculator(descriptorRows, descriptorInfo, queryDescriptors, 0, descriptorRows.size(), similarites);        
        ComputationPool pool = ComputationPool.getInstance();
        pool.execute(fb);
        while (!fb.isDone() && !fb.isCancelled()) {
            try {
                exec.checkCanceled();
            } catch (CanceledExecutionException e) {
                fb.requestStop();
                throw e;
            }
        }
        return similarites;
    }
    
    Map<String, Double[]> runSimilarityAnalysis2(BufferedDataTable dataTable, int descriptorColumnIdx, DescriptorInfo descriptorInfo, List<Object> queryDescriptors, ExecutionContext exec) throws CanceledExecutionException {
        CloseableRowIterator rowIterator = null;
        Map<String, Double[]> similarites = new ConcurrentHashMap<String, Double[]>();
        List<DescriptorRow> descriptorRows = new ArrayList<>();

        //int descriptorColumnIdx = SpecHelper.getDescriptorColumnIndex(dataTable.getDataTableSpec(), moleculeColumnIdx, descriptorInfo);

        try {
            rowIterator = dataTable.iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();

                DataCell cell = row.getCell(descriptorColumnIdx);
                if(!cell.isMissing()) {
                	descriptorRows.add(new DescriptorRow(row.getKey(), descriptorInfo, ((OCLDescriptorDataCell) cell).getEncodedDescriptor().getBytes()));
                }
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }

//        SimilarityCalculator fb = new SimilarityCalculator(descriptorRows, descriptorInfo, queryDescriptors, 0, descriptorRows.size(), similarites);        
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
        int chunk_size = (int) Math.max( 16 , Math.min(  descriptorRows.size()*0.01 ,1000) );
        //System.out.println("chunk size: "+chunk_size);
        //LOG.info("chunk size: "+chunk_size);
        SimilarityCalculatorActionProvider taskprovider = new SimilarityCalculatorActionProvider(descriptorRows, descriptorInfo, queryDescriptors, chunk_size, similarites);
        //pool.invoke(fb);
        taskprovider.getTasks().forEach( ti -> pool.enqueue(ti) );
        
        try {
			pool.waitForTermination();			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pool.shutdown();
        
        
        return similarites;
    }

    private class SimilarityCellFactory extends SingleCellFactory {
        private final Map<String, Double[]> similarities;
        private final boolean multiSim;

        //~--- constructors ---------------------------------------------------

        public SimilarityCellFactory(DataColumnSpec columnSpec, Map<String, Double[]> similarities, boolean multiSim) {
            super(columnSpec);
            this.similarities = similarities;
            this.multiSim = multiSim;
        }


        //~--- get methods ----------------------------------------------------

        @Override
        public DataCell getCell(DataRow dataRow) {
            Double[] sims = similarities.get(dataRow.getKey().toString());        
            if(sims==null) {
        		return DataType.getMissingCell();
        	}
            if (!multiSim) {            	
            	boolean allNaN = true;            
                for (Double sim : sims) {
                    allNaN = Double.isNaN(sim);
                }
                if (allNaN) {
                    return DataType.getMissingCell();
                }            	
                return ValueHelper.createDataCell(sims[0]);
            } else {            
                double[] s = new double[sims.length];
                for (int i = 0; i < sims.length; i++) {
                    s[i] = sims[i];
                }
                return ValueHelper.createDoubleVectorDataCell(s);
            }

        }


    }
}
