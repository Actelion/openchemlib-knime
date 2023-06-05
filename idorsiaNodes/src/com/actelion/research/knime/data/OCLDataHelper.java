package com.actelion.research.knime.data;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.AdapterCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellTypeConverter;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

public class OCLDataHelper {
    public static DataTableSpec[] convertInputTables(final DataTableSpec[] inSpec) {
        DataTableSpec[] arrConvertedSpecs = null;

        if (inSpec != null) {
            arrConvertedSpecs = new DataTableSpec[inSpec.length];

            // Setup conversions
            for (int i = 0; i < inSpec.length; i++) {
                arrConvertedSpecs[i] = inSpec[i];

                // Conversion makes only sense, if we have an input table and input columns defined
                if (inSpec[i] != null) {
                    List<Integer> toConvert = new ArrayList<Integer>();

                    for (int colIdx = 0; colIdx < inSpec[i].getNumColumns(); colIdx++) {
                        DataColumnSpec columnSpec = inSpec[i].getColumnSpec(colIdx);
                        DataType type = columnSpec.getType();

                        if ((type.isCompatible(SdfValue.class) || type.isCompatible(MolValue.class))
                                && !type.isCompatible(OCLMoleculeDataValue.class)) {
                            toConvert.add(colIdx);
                        }
                    }

                    if (!toConvert.isEmpty()) {
                        final ColumnRearranger rearranger = new ColumnRearranger(inSpec[i]);

                        for (Integer colIdx : toConvert) {
                            DataColumnSpec columnSpec = inSpec[i].getColumnSpec(colIdx);
                            final DataCellTypeConverter converter = OCLMoleculeTypeConverter.createConverter(columnSpec.getType());

                            rearranger.ensureColumnIsConverted(converter, colIdx);
                        }

                        arrConvertedSpecs[i] = rearranger.createSpec();
                    }
                }
            }
        }

        return arrConvertedSpecs;
    }

    public static BufferedDataTable[] convertInputTables(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
        BufferedDataTable[] arrConvertedTables = null;

        if (inData != null) {
            arrConvertedTables = new BufferedDataTable[inData.length];

            // Setup conversions
            final Map<Integer, ColumnRearranger> mapColumnRearrangers = new HashMap<Integer, ColumnRearranger>();

            for (int iTableIndex = 0; iTableIndex < inData.length; iTableIndex++) {
                arrConvertedTables[iTableIndex] = inData[iTableIndex];

                // Conversion makes only sense, if we have an input table and input columns defined
                if (inData[iTableIndex] != null) {
                    List<Integer> toConvert = new ArrayList<Integer>();

                    for (int colIdx = 0; colIdx < inData[iTableIndex].getDataTableSpec().getNumColumns(); colIdx++) {
                        DataColumnSpec columnSpec = inData[iTableIndex].getDataTableSpec().getColumnSpec(colIdx);
                        DataType type = columnSpec.getType();

                        if ((type.isCompatible(SdfValue.class) || type.isCompatible(MolValue.class))
                                && !type.isCompatible(OCLMoleculeDataValue.class)) {
                            toConvert.add(colIdx);
                        }
                    }

                    // Setup a column rearranger and run it for the input table, but only if changes are necessary
                    if (!toConvert.isEmpty()) {

//                      final int[] arrColumnIndex = new int[listConversionColumns.size()];
                        final DataTableSpec tableSpec = inData[iTableIndex].getDataTableSpec();
                        final ColumnRearranger rearranger = new ColumnRearranger(tableSpec);

//                      int iCount = 0;

                        for (Integer colIdx : toConvert) {
                            DataColumnSpec columnSpec = inData[iTableIndex].getDataTableSpec().getColumnSpec(colIdx);
                            final DataCellTypeConverter converter = OCLMoleculeTypeConverter.createConverter(columnSpec.getType());

                            rearranger.ensureColumnIsConverted(converter, colIdx);
                        }

//                      for (final InputDataInfo inputDataInfo : listConversionColumns) {
//                          final DataCellTypeConverter converter = inputDataInfo.getConverter();
//                          final int iColumnIndex = inputDataInfo.getColumnIndex();
//                          arrColumnIndex[iCount++] = iColumnIndex;
//                          rearranger.ensureColumnIsConverted(converter, iColumnIndex);
//                      }
                        // Part 1 of workaround for a bug in ColumnRearranger - without adding new cells we get an
                        // error from internal hashmap: Illegal initial capacity: -2147483648
                        // Additionally, we use this factory to check the success of the auto conversion
                        // and to generate warnings in case that conversion failed.
//                      rearranger.append(new AbstractCellFactory(true, new DataColumnSpecCreator(
//                              DataTableSpec.getUniqueColumnName(tableSpec, "EmptyCells"), IntCell.TYPE).createSpec()) {
//                          private final DataCell[] EMPTY_CELLS = new DataCell[] { DataType.getMissingCell() };
//                          @Override
//                          public DataCell[] getCells(final DataRow row) {
//                              if (row != null) {
//                                  for (int i = 0; i < arrColumnIndex.length; i++) {
//                                      final DataCell cellConverted = row.getCell(arrColumnIndex[i]);
//                                      if (cellConverted instanceof MissingCell) {
//                                          final String strError = ((MissingCell)cellConverted).getError();
//                                          if (strError != null) {
//                                              try {
//                                                  generateAutoConversionError(listConversionColumns.get(i), createShortError(strError));
//                                              }
//                                              catch (final Exception exc) {
//                                                  LOGGER.error(exc);
//                                                  // Ignore it as this would lead to a deadlock in KNIME's rearranger handling
//                                              }
//                                          }
//                                      }
//                                  }
//                              }
//
//                              return EMPTY_CELLS;
//                          }
//
//                          private String createShortError(final String strError) {
//                              String strRet = (strError == null || strError.trim().isEmpty() ? "Unknown error." : strError);
//                              int iIndex = strRet.indexOf("\n");
//                              if (iIndex >= 0) {
//                                  strRet = strRet.substring(0, iIndex);
//                                  iIndex = strRet.lastIndexOf(" for");
//                                  if (iIndex >= 0) {
//                                      strRet = strRet.substring(0, iIndex);
//                                  }
//                              }
//
//                              return strRet;
//                          }
//                      });
                        mapColumnRearrangers.put(iTableIndex, rearranger);
                    }
                }
            }

            final int iCountTablesToBeConverted = mapColumnRearrangers.size();
            int iCount = 1;

            for (int iTableIndex = 0; iTableIndex < inData.length; iTableIndex++) {
                exec.setMessage("Converting input tables for processing (" + iCount + " of " + iCountTablesToBeConverted + ") ...");

                final ColumnRearranger rearranger = mapColumnRearrangers.get(iTableIndex);

                if (rearranger != null) {
                    iCount++;
                    arrConvertedTables[iTableIndex] = exec.createColumnRearrangeTable(inData[iTableIndex], rearranger,
                            exec.createSubProgress(1.0d / iCountTablesToBeConverted));

                    // Part 2 of workaround from above: We need to remove the last column again
//                    final DataTableSpec    tableSpec            = arrConvertedTables[iTableIndex].getDataTableSpec();
//                    final ColumnRearranger rearrangerWorkaround = new ColumnRearranger(tableSpec);

//                    rearrangerWorkaround.remove(tableSpec.getNumColumns() - 1);
//                    arrConvertedTables[iTableIndex] = exec.createColumnRearrangeTable(arrConvertedTables[iTableIndex],
//                            rearrangerWorkaround, exec.createSubProgress(1.0d / iCountTablesToBeConverted / 2.0d));
                }
            }
        }

        exec.setProgress(1.0d);

        return arrConvertedTables;
    }


}
