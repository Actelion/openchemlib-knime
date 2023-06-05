/*
 * @(#)OCLCalculateSimilarityNodeModel.java   15/12/29
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

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

//~--- JDK imports ------------------------------------------------------------

public class OCLCalculateSimilarityNodeModel extends SimilarityNodeModel {
    private static final int IN_PORT = 0;

    //~--- fields -------------------------------------------------------------

    private final OCLCalculateSimilarityNodeSettings m_settings = new OCLCalculateSimilarityNodeSettings();

    //~--- constructors -------------------------------------------------------

    public OCLCalculateSimilarityNodeModel() {
        super(1, 1);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean hasMoleculeColumn = false;
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

        ColumnRearranger columnRearranger = createColumnRearranger(inputSpec, m_settings.getColumnName(), false);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputTable = inData[0];
        final int moleculeColumnIdx = SpecHelper.getColumnIndex(inputTable.getDataTableSpec(), m_settings.getInputColumnName());
        StereoMolecule queryMolecule = m_settings.getFragment();
        DescriptorInfo descriptorInfo = m_settings.getDescriptor();

        List<Object> queryDescriptors = new ArrayList<>();
        Object queryDescriptor = DescriptorHelpers.calculateDescriptor(queryMolecule, descriptorInfo);
        queryDescriptors.add(queryDescriptor);

        Map<String, Double[]> similarities = runSimilarityAnalysis(inputTable, moleculeColumnIdx, descriptorInfo, queryDescriptors, exec);
        ColumnRearranger analysisColumnRearranger = createColumnRearranger(inputTable.getDataTableSpec(), m_settings.getColumnName(), false, similarities);
        BufferedDataTable analysisDataTable = exec.createColumnRearrangeTable(inData[IN_PORT], analysisColumnRearranger, exec);
        return new BufferedDataTable[]{analysisDataTable};
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
        (new OCLCalculateSimilarityNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

//    private ColumnRearranger createColumnRearranger(DataTableSpec in) {
//        ColumnRearranger c = new ColumnRearranger(in);
//        final int moleculeColumnIdx = SpecHelper.getColumnIndex(m_settings.getInputColumnName(), in);
//        DataColumnSpec newSpec = createColumnSpec(in, m_settings.getColumnName());
//        StereoMolecule queryMolecule = m_settings.getFragment();
//        DescriptorInfo descriptorInfo = m_settings.getDescriptor();
//        Object queryDescriptor = DescriptorHelpers.calculateDescriptor(queryMolecule, descriptorInfo);
//        CellFactory factory = new SimilarityCellFactory(newSpec, moleculeColumnIdx, queryDescriptor, descriptorInfo);
//
//
//        c.append(factory);
//
//        return c;
//    }

//    private ColumnRearranger createColumnRearranger(DataTableSpec in, Map<String, Double[]> similarities) {
//        ColumnRearranger c = new ColumnRearranger(in);
//        DataColumnSpec newSpec = createColumnSpec(in, m_settings.getColumnName());
//        CellFactory factory = new OCLCalculateSimilarityListNodeModel.SimilarityCellFactory(newSpec, similarities);
//        c.append(factory);
//        return c;
//    }


//    private DataColumnSpec createColumnSpec(DataTableSpec in, String columnTitle) {
//        String columnName = DataTableSpec.getUniqueColumnName(in, columnTitle);
//
//        return SpecHelper.createDoubleColumnSpec(columnName);
//    }

    //~--- inner classes ------------------------------------------------------

//    private class SimilarityCellFactory extends SingleCellFactory {
//        private DescriptorInfo descriptor;
//        private int moleculeColumnIdx;
//        private Object queryDescriptor;
//
//        //~--- constructors ---------------------------------------------------
//
//        public SimilarityCellFactory(DataColumnSpec columnSpec, int moleculeColumnIdx, Object queryDescriptor, DescriptorInfo descriptor) {
//            super(true, columnSpec);
//            this.moleculeColumnIdx = moleculeColumnIdx;
//            this.queryDescriptor = queryDescriptor;
//            this.descriptor = descriptor;
//        }
//
//        //~--- get methods ----------------------------------------------------
//
//        @Override
//        public DataCell getCell(DataRow dataRow) {
//            DataCell orgCell = dataRow.getCell(moleculeColumnIdx);
//
//            if (orgCell.isMissing()) {
//                return DataType.getMissingCell();
//            }
//
//            if (queryDescriptor == null) {
//                return DataType.getMissingCell();
//            }
//
//            OCLMoleculeDataValue oclMolecule = (OCLMoleculeDataValue) orgCell;
//            double similarity = calculate(oclMolecule, descriptor);
//
//            if (Double.isNaN(similarity)) {
//                return DataType.getMissingCell();
//            }
//
//            return ValueHelper.createDataCell(similarity);
//        }
//
//        //~--- methods --------------------------------------------------------
//
//        private double calculate(OCLMoleculeDataValue oclMolecule, DescriptorInfo descriptor) {
//            return DescriptorHelpers.calculateSimilarity(descriptor, queryDescriptor, oclMolecule.getDescriptor(descriptor));
//        }
//    }
}
