package com.actelion.research.knime.nodes.io;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.chem.io.CompoundTableConstants;
import com.actelion.research.chem.io.DWARFileParser;
import com.actelion.research.chem.io.DWARFileParser.SpecialField;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.DescriptorHandlerShapeOneConf;
import com.actelion.research.chem.phesa.PheSAMolecule;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLDescriptorHelper;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLPheSAMoleculeDataCell;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.MultiStructureCompoundParserAdapter;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.Utils;
import com.actelion.research.knime.utils.ValueHelper;

import org.knime.base.data.filter.row.dialog.model.ColumnSpec;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.util.FileUtil;
import org.knime.core.util.KnimeFileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.actelion.research.knime.utils.StructureFileHelpers.extractColumnNames;

//~--- JDK imports ------------------------------------------------------------

/**
 * This is the model implementation of OCLFileReader.
 *
 * @author Actelion Pharmaceuticals Ltd.
 */
public class OCLFileReaderNodeModel extends NodeModel {
	private final OCLFileReaderNodeSettings m_settings = new OCLFileReaderNodeSettings();

	private static final NodeLogger LOGGER = NodeLogger.getLogger(OCLFileReaderNodeModel.class);

	// ~--- constructors -------------------------------------------------------

	protected OCLFileReaderNodeModel() {
		super(0, 1);
	}

	// ~--- methods ------------------------------------------------------------

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return getDataTableSpecs();
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

		String selectedFile = m_settings.getInputFile();

		String[] columns_data = getSelectedColumns();
		String[] columns_special = getSelectedSpecialColumns();

		List<String> col_names_data_to_process = new ArrayList<>();
		List<String> col_names_special_to_process = new ArrayList<>();
		DataTableSpec[] dataTableSpecs = getDataTableSpecs(col_names_data_to_process, col_names_special_to_process);

		DataTableSpec outputTableSpecs = dataTableSpecs[0];
		BufferedDataContainer container = exec.createDataContainer(outputTableSpecs);

		// MultiStructureCompoundParserAdapter parser = Utils.getCompoundFileParser(new
		// File(selectedFile));
		DWARFileParser parser = new DWARFileParser( OCLFileReaderNodeDialog.resolveFilePathOrKnimeURL(selectedFile) );

		if (parser == null) {
			throw new Exception("Sorry, unsupported file format (" + selectedFile + ")");
		}

		Map<String, SpecialField> sf_map = parser.getSpecialFieldMap();

		String[] fieldNames = parser.getFieldNames();
		String[] fieldNamesSpecial = parser.getSpecialFieldMap().keySet().stream().toArray(String[]::new);

		int[] selectedFieldIdxs_special = new int[columns_special.length];
		int[] selectedFieldIdxs_data = new int[columns_data.length];
		int idx = 0;

//        for (String column : columns_data) {
//            int fieldIdx = 0;
//
//            for (String fieldName : fieldNames) {
//                if (column.equals(fieldName)) {
//                    selectedFieldIdxs_data[idx] = fieldIdx;
//                    idx++;
//                }
//                fieldIdx++;
//            }
//        }
//        
//        for (String column : columns_special) {
//            int fieldIdx = 0;
//            for (String fieldName : fieldNamesSpecial) {
//                if (column.equals(fieldName)) {
//                    selectedFieldIdxs_special[idx] = fieldIdx;
//                    idx++;
//                }
//                fieldIdx++;
//            }
//        }

		Map<String, DescriptorInfo> descriptors_info = new HashMap<>();
		descriptors_info.putAll(OCLDescriptorHelper.getAvailableDescriptors2D());
		descriptors_info.putAll(OCLDescriptorHelper.getAvailableDescriptors3D());

		Set<String> descriptors_2d = new HashSet<>(OCLDescriptorHelper.getAvailableDescriptors2D().keySet());
		Set<String> descriptors_3d = new HashSet<>(OCLDescriptorHelper.getAvailableDescriptors3D().keySet());

		int counter = 1;

		System.out.println("total number of columns: " + (columns_data.length + columns_special.length));

		while (parser.next()) {
			RowKey key = new RowKey("Row " + counter);
			// DataCell[] cells = new DataCell[columns_data.length + columns_special.length
			// ];
			DataCell[] cells = new DataCell[col_names_data_to_process.size() + col_names_special_to_process.size()];
			int currentCellIdx = 0;

			// int col_idx = 0;

			// start with the special columns..
			// TODO: order: 1. structures , 2. coordinates , 3. descriptors, 4. data
			// (CURRENTLY WE DON't DO THIS!)

			// for(String si : fieldNamesSpecial) {
			for (String si : col_names_special_to_process) {
				SpecialField sf = sf_map.get(si);
				String sf_type = sf_map.get(si).type;

				if (sf_type.equals(CompoundTableConstants.cColumnTypeIDCode)) {
					// create structure column
					String idCode = parser.getSpecialFieldData(sf.fieldIndex);
					if (idCode != null) {
						cells[currentCellIdx] = new OCLMoleculeDataCell(idCode);
					} else {
						cells[currentCellIdx] = DataType.getMissingCell();
					}
				} else if (sf_type.equals(CompoundTableConstants.cColumnType3DCoordinates)) {
					// create conformer set column
					System.out.println("sf.parent = " + sf.parent);
					String id_column = sf.parent;
					String id_code = parser.getSpecialFieldData(parser.getSpecialFieldIndex(id_column));
					String conformers_raw = parser.getSpecialFieldData(sf.fieldIndex);

					// check how this should be done really efficiently...
					System.out.println("conformers_raw = " + conformers_raw);

					String[] conformers_split = conformers_raw.split(" "); // hmm, which constant should this be?..
					cells[currentCellIdx] = new OCLConformerListDataCell(id_code, conformers_split);
				} else if (descriptors_2d.contains(sf_type)) {
					String stringDescriptorValue = parser.getSpecialFieldData(sf.fieldIndex);
					cells[currentCellIdx] = ValueHelper.createDescriptorDataCell(descriptors_info.get(sf_type),
							stringDescriptorValue.getBytes());
				} else if (sf_type.equals(DescriptorConstants.DESCRIPTOR_ShapeAlignSingleConf.shortName)) {
					String stringDescriptorValue = parser.getSpecialFieldData(sf.fieldIndex);
					PheSAMolecule phesamol = DescriptorHandlerShapeOneConf.getDefaultInstance()
							.decode(stringDescriptorValue);
					cells[currentCellIdx] = new OCLPheSAMoleculeDataCell(phesamol);
				} else if (sf_type.equals(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName)) {
					String stringDescriptorValue = parser.getSpecialFieldData(sf.fieldIndex);
					PheSAMolecule phesamol = DescriptorHandlerShape.getDefaultInstance().decode(stringDescriptorValue);
					cells[currentCellIdx] = new OCLPheSAMoleculeDataCell(phesamol);
				} else if (descriptors_3d.contains(sf_type)) {
					// here we should actually set the 3d coordinates field as parent
					String stringDescriptorValue = parser.getSpecialFieldData(sf.fieldIndex);
					cells[currentCellIdx] = ValueHelper.createDescriptorDataCell(descriptors_info.get(sf_type),
							stringDescriptorValue.getBytes());
				} else {
					cells[currentCellIdx] = DataType.getMissingCell();
				}

				currentCellIdx++;
			}

			// add normal data:
			// for (int selectedFieldIdx : selectedFieldIdxs_data) {
			for (String si : col_names_data_to_process) {
				// String fieldData = parser.getFieldData( selectedFieldIdx);
				String fieldData = parser.getFieldData(parser.getFieldIndex(si));

				if ((fieldData == null) || fieldData.isEmpty()) {
					cells[currentCellIdx++] = DataType.getMissingCell();
				} else {
					cells[currentCellIdx++] = new StringCell(fieldData);
				}
			}

			DataRow row = new DefaultRow(key, cells);

			container.addRowToTable(row);
			counter++;
		}

		container.close();
		parser.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	protected BufferedDataTable[] execute_old(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		String selectedFile = m_settings.getInputFile();
		String[] columns = getSelectedColumns();

		DataTableSpec[] dataTableSpecs = getDataTableSpecs();
		DataTableSpec outputTableSpecs = dataTableSpecs[0];
		BufferedDataContainer container = exec.createDataContainer(outputTableSpecs);
		MultiStructureCompoundParserAdapter parser = Utils.getCompoundFileParser(new File(selectedFile));

		if (parser == null) {
			throw new Exception("Sorry, unsupported file format (" + selectedFile + ")");
		}

		String[] fieldNames = parser.getFieldNames();
		int[] selectedFieldIdxs = new int[columns.length];
		int idx = 0;

		for (String column : columns) {
			int fieldIdx = 0;

			for (String fieldName : fieldNames) {
				if (column.equals(fieldName)) {
					selectedFieldIdxs[idx++] = fieldIdx;
				}

				fieldIdx++;
			}
		}

		int counter = 1;
		int structureCount = parser.getStructureCountPerEntry();
		int descriptorCount = parser.getDescriptorCountPerEntry();
		while (parser.next()) {
			RowKey key = new RowKey("Row " + counter);
			DataCell[] cells = new DataCell[columns.length + structureCount + descriptorCount];
			int currentCellIdx = 0;
			for (int i = 0; i < structureCount; i++, currentCellIdx++) {
				MultiStructureCompoundParserAdapter.Structure structureSummary = parser.getStructureSummary(i);
				if (parser.getIDCode(structureSummary.getColumnName()) == null) {
					cells[currentCellIdx] = DataType.getMissingCell();
				} else {
					OCLMoleculeDataCell oclMoleculeDataCell = new OCLMoleculeDataCell(
							parser.getIDCode(structureSummary.getColumnName()),
							parser.getCoordinates(structureSummary.getColumnName()));
					cells[currentCellIdx] = oclMoleculeDataCell;
				}
			}

			for (int i = 0; i < descriptorCount; i++, currentCellIdx++) {
				MultiStructureCompoundParserAdapter.Descriptor descriptorSummary = parser.getDescriptorSummary(i);
				String stringDescriptorValue = parser.getDescriptorValueAsString(descriptorSummary.getParentName(),
						descriptorSummary.getDescriptorInfo());
				cells[currentCellIdx] = ValueHelper.createDescriptorDataCell(descriptorSummary.getDescriptorInfo(),
						stringDescriptorValue.getBytes());
			}

			for (int selectedFieldIdx : selectedFieldIdxs) {
				String fieldData = parser.getFieldData(selectedFieldIdx);

				if ((fieldData == null) || fieldData.isEmpty()) {
					cells[currentCellIdx++] = DataType.getMissingCell();
				} else {
					cells[currentCellIdx++] = new StringCell(fieldData);
				}
			}

			DataRow row = new DefaultRow(key, cells);

			container.addRowToTable(row);
			counter++;
		}

		container.close();
		parser.close();

		return new BufferedDataTable[] { container.getTable() };
	}

	@Override
	protected void loadInternals(File file, ExecutionMonitor executionMonitor)
			throws IOException, CanceledExecutionException {
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
		m_settings.loadSettingsForModel(nodeSettingsRO);
	}

	@Override
	protected void reset() {
	}

	@Override
	protected void saveInternals(File file, ExecutionMonitor executionMonitor)
			throws IOException, CanceledExecutionException {
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO nodeSettingsWO) {
		m_settings.saveSettings(nodeSettingsWO);
	}

	@Override
	protected void validateSettings(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
		// (new OCLFileWriterNodeSettings()).loadSettingsForModel(nodeSettingsRO);

		OCLFileReaderNodeSettings settings = new OCLFileReaderNodeSettings();
		settings.loadSettingsForModel(nodeSettingsRO);

		// check if these settings with respect to SpecialColumns can work:
		String sc[] = settings.getSelectedColumnsSpecial(settings.isIncludeAllColumns());

		List<String> sc_list = new ArrayList<>(Arrays.stream(sc).collect(Collectors.toList()));

		String selectedFile = settings.getInputFile();

		File fileSelectedFile = null;

		if (selectedFile.toLowerCase().startsWith("knime://")) {
			// Path pathSelectedFile = null;
			try {
				fileSelectedFile = FileUtil.getFileFromURL(new URL(selectedFile));
			} catch (Exception ex) {
				throw new InvalidSettingsException("Problem with knime URL", ex);
			}
		} else {
			fileSelectedFile = new File(selectedFile);
		}

		DWARFileParser parser = new DWARFileParser(fileSelectedFile);// new DWARFileParser(new File(selectedFile));

		if (parser == null) {
			throw new InvalidSettingsException("Sorry, unsupported file format (" + selectedFile + ")");
		}

		Map<String, SpecialField> sf_map = parser.getSpecialFieldMap();

		if (sf_map == null) {
			System.out.println("[warn] getSpecialFieldMap() returned null?");
			sf_map = new HashMap<>();
		}

		for (String si : sc_list) {
			SpecialField sf = sf_map.get(si);
			// String sf_type = sf_map.get(si).type;
			// check if parent column is selected too:
			if (sf == null) {
				LOGGER.warn("Missing special field entry: " + si);
				continue;
			}
			if (sf.parent == null) {
				continue;
			}
			if (sf.parent.equals("")) {
				continue;
			}
			if (!sc_list.contains(sf.parent)) {
				throw new InvalidSettingsException(
						"Error: for column " + si + " you must also include parent column " + sf.parent);
			}
		}

	}

	private String[] getSelectedColumns() {
		String[] columns = m_settings.getSelectedColumns(m_settings.isIncludeAllColumns());
		return columns;
	}

	private String[] getSelectedSpecialColumns() {
		String[] columns_special = m_settings.getSelectedColumnsSpecial(m_settings.isIncludeAllColumns());
		return columns_special;
	}

	// ~--- get methods --------------------------------------------------------

	private DataTableSpec[] getDataTableSpecs() throws InvalidSettingsException {
		return getDataTableSpecs(new ArrayList<>(), new ArrayList<>());
	}

	private DataTableSpec[] getDataTableSpecs(List<String> col_names_data, List<String> col_names_special)
			throws InvalidSettingsException {
		String selectedFile = m_settings.getInputFile();
		String[] columns_data = getSelectedColumns();
		String[] columns_special = getSelectedSpecialColumns();
		// MultiStructureCompoundParserAdapter parser = Utils.getCompoundFileParser(new
		// File(selectedFile));

		File f = OCLFileReaderNodeDialog.resolveFilePathOrKnimeURL(selectedFile);//new File(selectedFile);
		if (!f.exists()) {
			throw new InvalidSettingsException("File not found: " + selectedFile);
		}
		DWARFileParser p = new DWARFileParser(f);

		Map<String, SpecialField> special_fields = p.getSpecialFieldMap();

		System.out.println("\nSpecialFieldMap:");
		for (String ki : special_fields.keySet()) {
			System.out.println(ki + "   --> " + special_fields.get(ki).name);
		}
		System.out.println("\nSpecialColumn selected:");
		for (String csi : columns_special) {
			System.out.println(csi);
		}
		System.out.println();

		List<DataColumnSpec> columnSpecs = new ArrayList<>(); // DataColumnSpec[columns_data.length +
																// columns_special.length];

		Map<String, DataColumnSpec> structureColumnsByName = new HashMap<>();
		int currentColumnIdx = 0;

//        for (int i = 0; i < structureCount; i++, currentColumnIdx++) {
//            MultiStructureCompoundParserAdapter.Structure structureSummary = parser.getStructureSummary(i);
//            String columnName = structureSummary.getColumnName();
//            columnSpecs[currentColumnIdx] = SpecHelper.createMoleculeColumnSpec(columnName);
//            structureColumnsByName.put(columnName, columnSpecs[i]);
//        }
//
//        for (int i = 0; i < descriptorCount; i++, currentColumnIdx++) {
//            MultiStructureCompoundParserAdapter.Descriptor descriptorSummary = parser.getDescriptorSummary(i);
//            columnSpecs[currentColumnIdx] = SpecHelper.createDescriptorColumnSpec(descriptorSummary.getDescriptorInfo(), structureColumnsByName.get(descriptorSummary.getParentName()));
//        }

		Set<String> descriptors_2d = OCLDescriptorHelper.getAvailableDescriptors2D().keySet();
		Set<String> descriptors_3d = OCLDescriptorHelper.getAvailableDescriptors3D().keySet();

		Set<String> all_descriptors = new HashSet<>();
		all_descriptors.addAll(descriptors_2d);
		// all_descriptors.addAll(descriptors_3d);

		// 1. pass: generate all molecule columns
		// 2. pass: generate 3D coordinates
		// 3. pass: generate all descriptor columns

		for (String columnName : columns_special) {

			System.out.println("columnName = " + columnName);

			// find out how to handle based on type id:
			SpecialField sfi = special_fields.get(columnName);
			if (sfi.type.equals(CompoundTableConstants.cColumnTypeIDCode)) {
				// create molecule column
				// columnSpecs[currentColumnIdx] =
				// SpecHelper.createMoleculeColumnSpec(columnName);
				columnSpecs.add(SpecHelper.createMoleculeColumnSpec(columnName));
				structureColumnsByName.put(columnName, columnSpecs.get(currentColumnIdx));
				col_names_special.add(columnName);
				currentColumnIdx++;
			}
		}

		for (String columnName : columns_special) {
			SpecialField sfi = special_fields.get(columnName);

			if (sfi.type.equals(CompoundTableConstants.cColumnType3DCoordinates)) {
				DataColumnSpec parentColumnSpec = structureColumnsByName.get(sfi.parent);
				// columnSpecs[currentColumnIdx] =
				// SpecHelper.createDescriptorColumnSpec(DescriptorHelper.getDescriptorInfo(sfi.type),
				// parentColumnSpec);
				// columnSpecs.add(SpecHelper.createDescriptorColumnSpec(DescriptorHelper.getDescriptorInfo(sfi.type),
				// parentColumnSpec) );
				columnSpecs.add(SpecHelper.createConformerSetColumnSpec(columnName, parentColumnSpec));
				col_names_special.add(columnName);
				currentColumnIdx++;
			}
		}

		for (String columnName : columns_special) {
			SpecialField sfi = special_fields.get(columnName);

			if (all_descriptors.contains(sfi.type)) {
				DataColumnSpec parentColumnSpec = structureColumnsByName.get(sfi.parent);
				// columnSpecs[currentColumnIdx] =
				// SpecHelper.createDescriptorColumnSpec(DescriptorHelper.getDescriptorInfo(sfi.type),
				// parentColumnSpec);
				columnSpecs.add(SpecHelper.createDescriptorColumnSpec(DescriptorHelper.getDescriptorInfo(sfi.type),
						parentColumnSpec));
				col_names_special.add(columnName);
				currentColumnIdx++;
			} else if (sfi.type.equals(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName)
					|| sfi.type.equals(DescriptorConstants.DESCRIPTOR_ShapeAlignSingleConf.shortName)) {
				DataColumnSpec parentColumnSpec = structureColumnsByName.get(sfi.parent);
				columnSpecs.add(
						SpecHelper.createPhesaMoleculeColumnSpec("PheSAMolecule[" + parentColumnSpec.getName() + "]"));
				col_names_special.add(columnName);
				currentColumnIdx++;
			}

		}

		for (String column : columns_data) {
			// columnSpecs[currentColumnIdx++] = SpecHelper.createStringColumnSpec(column);
			columnSpecs.add(SpecHelper.createStringColumnSpec(column));
			col_names_data.add(column);
			currentColumnIdx++;
		}

		DataColumnSpec columnSpecsArray[] = new DataColumnSpec[columnSpecs.size()];
		for (int zi = 0; zi < columnSpecsArray.length; zi++) {
			columnSpecsArray[zi] = columnSpecs.get(zi);
		}

		DataTableSpec outputSpec = new DataTableSpec(columnSpecsArray);

		return new DataTableSpec[] { outputSpec };
	}
}
