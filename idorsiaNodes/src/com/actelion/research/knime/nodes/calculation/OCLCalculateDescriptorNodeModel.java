/*
 * @(#)OCLCalculateDescriptorNodeModel.java   15/12/29
 *
 * Copyright (c) 2010-2011 Actelion Pharmaceuticals Ltd.
 *
 *  Gewerbestrasse 16, CH-4123 Allschwil, Switzerland
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information
 *  of Actelion Pharmaceuticals Ltd. ("Confidential Information").  You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Actelion Pharmaceuticals Ltd.
 *
 *  Author: finkt
 */


package com.actelion.research.knime.nodes.calculation;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.computation.ComputationPool;
import com.actelion.research.knime.computation.DescriptorCalculator;
import com.actelion.research.knime.computation.DescriptorCalculatorActionProvider;
import com.actelion.research.knime.computation.SubstructureMatcherActionProvider;
import com.actelion.research.knime.data.MoleculeRow;
import com.actelion.research.knime.data.OCLDescriptorDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;

import org.knime.chem.types.SdfValue;
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
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//~--- JDK imports ------------------------------------------------------------

public class OCLCalculateDescriptorNodeModel extends NodeModel {
    private static final int IN_PORT = 0;

    //~--- fields -------------------------------------------------------------

    private final OCLCalculateDescriptorNodeSettings m_settings;

    //~--- constructors -------------------------------------------------------

    public OCLCalculateDescriptorNodeModel() {
        super(1, 1);
        m_settings = new OCLCalculateDescriptorNodeSettings();
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

        ColumnRearranger columnRearranger = createColumnRearranger(inputSpec, null);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputTable = inData[0];
        Map<String, Map<DescriptorInfo, byte[]>> descriptors = new ConcurrentHashMap<>();
        final int moleculeColumnIdx = SpecHelper.getColumnIndex(inputTable.getDataTableSpec(), m_settings.getInputColumnName());
        List<String> descriptorInfo = m_settings.getSelectedDescriptors();

        runDescriptorCalculation(inputTable, moleculeColumnIdx, descriptorInfo, exec, descriptors);
        ColumnRearranger columnRearranger = createColumnRearranger(inData[IN_PORT].getDataTableSpec(), descriptors);
        BufferedDataTable outputTable = exec.createColumnRearrangeTable(inData[IN_PORT], columnRearranger, exec);
        return new BufferedDataTable[]{outputTable};

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
        (new OCLCalculateDescriptorNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private void runDescriptorCalculation(BufferedDataTable dataTable, int moleculeColumnIdx, List<String> descriptorNames, ExecutionContext exec, Map<String, Map<DescriptorInfo, byte[]>> descriptors) throws CanceledExecutionException {
        CloseableRowIterator rowIterator = null;
        List<MoleculeRow> moleculeRows = new ArrayList<>();

        try {
            rowIterator = dataTable.iterator();

            while (rowIterator.hasNext()) {
                exec.checkCanceled();
                DataRow row = rowIterator.next();                
                DataCell cell = row.getCell(moleculeColumnIdx);
                if( cell.isMissing() ) {
                	
                }
                else if (cell.getType().isCompatible(OCLMoleculeDataValue.class)) {
                    moleculeRows.add(new MoleculeRow(row.getKey(), ((OCLMoleculeDataCell) cell).getIdCode()));
                }
            }
        } finally {
            if (rowIterator != null) {
                rowIterator.close();
            }
        }

        List<DescriptorInfo> descriptorInfos = new ArrayList<>();
        for (String descriptorName : descriptorNames) {
            descriptorInfos.add(DescriptorHelpers.getDescriptorInfoByShortName(descriptorName));
        }
        
        ThreadPool pool = org.knime.core.util.ThreadPool.currentPool().createSubPool();
        //int min_elements_per_job = ()
        int chunk_size = (int) Math.max( 1 , Math.min(  moleculeRows.size()*0.01 ,100) );
        //System.out.println("chunk size: "+chunk_size);
        //LOG.info("chunk size: "+chunk_size);
        DescriptorCalculatorActionProvider taskprovider = new DescriptorCalculatorActionProvider(moleculeRows, descriptorInfos, chunk_size, descriptors);
        //pool.invoke(fb);
        taskprovider.getTasks().forEach( ti -> pool.enqueue(ti) );
        
        try {
			pool.waitForTermination();			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        pool.shutdown();

//        DescriptorCalculator fb = new DescriptorCalculator(moleculeRows, descriptorInfos, 0, moleculeRows.size(), descriptors);
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
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec in, Map<String, Map<DescriptorInfo, byte[]>> descriptors) {
        ColumnRearranger c = new ColumnRearranger(in);
        String inputMoleculeColumnName = m_settings.getInputColumnName();
        final int inputMoleculeColumnIdx = SpecHelper.getColumnIndex(in, inputMoleculeColumnName);
        List<String> selectedDescriptors = m_settings.getSelectedDescriptors();
        for (String selectedDescriptor : selectedDescriptors) {
            DescriptorInfo descriptorInfo = DescriptorHelpers.getDescriptorInfoByShortName(selectedDescriptor);
            DataColumnSpec newSpec = createDescriptorColumnSpec(in, descriptorInfo, inputMoleculeColumnIdx);
            CellFactory factory = new DescriptorCellFactory(newSpec, inputMoleculeColumnName, inputMoleculeColumnIdx, descriptorInfo, descriptors);
            c.append(factory);
        }
        return c;
    }


    private DataColumnSpec createDescriptorColumnSpec(DataTableSpec in, DescriptorInfo descriptorInfo, int inputMoleculeColumnIdx) {
        return SpecHelper.createDescriptorColumnSpec(descriptorInfo, in.getColumnSpec(inputMoleculeColumnIdx));
    }

    //~--- inner classes ------------------------------------------------------

    public static class DescriptorCellFactory extends SingleCellFactory {
        private final String moleculeColumnName;
        private final Map<String, Map<DescriptorInfo, byte[]>> descriptors;
        private DescriptorInfo descriptorInfo;
        private int moleculeColumnIdx;

        //~--- constructors ---------------------------------------------------

        public DescriptorCellFactory(DataColumnSpec columnSpec, String moleculeColumnName, int moleculeColumnIdx, DescriptorInfo descriptorInfo, Map<String, Map<DescriptorInfo, byte[]>> descriptors) {
            super(columnSpec);
            this.moleculeColumnName = moleculeColumnName;
            this.moleculeColumnIdx = moleculeColumnIdx;
            this.descriptorInfo = descriptorInfo;
            this.descriptors = descriptors;
        }

        //~--- get methods ----------------------------------------------------

        @Override
        public DataCell getCell(DataRow dataRow) {
            DataCell orgCell = dataRow.getCell(moleculeColumnIdx);


            if (orgCell.isMissing()) {
                return DataType.getMissingCell();
            }

            if (descriptorInfo == null) {
                return DataType.getMissingCell();
            }

            Map<DescriptorInfo, byte[]> descriptorInfoMap = descriptors.get(dataRow.getKey().getString());
            if (descriptorInfoMap == null) {
                return DataType.getMissingCell();
            }
            byte[] descriptorBytes = descriptorInfoMap.get(descriptorInfo);
            if (descriptorBytes == null) {
                return DataType.getMissingCell();
            }
            OCLDescriptorDataCell descriptorDataCell = ValueHelper.createDescriptorDataCell(descriptorInfo, descriptorBytes);
            return descriptorDataCell;
        }
    }
}
