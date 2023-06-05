package com.actelion.research.knime.nodes.io;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.chem.io.CompoundTableConstants;
import com.actelion.research.chem.io.DWARFileParser;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLConformerListDataValue;
import com.actelion.research.knime.nodes.calculation.OCLConformerGeneratorNodeModel;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SDFileWriter;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;
import com.actelion.research.knime.utils.dwar.DWARFileWriter;
import com.actelion.research.knime.utils.dwar.DWARHeader;
import com.actelion.research.knime.utils.dwar.DWARHeaderTag;
import com.actelion.research.knime.utils.dwar.DWARRecord;
import com.idorsia.research.chem.comm.json.DWARWriter_JSON;
import com.idorsia.research.chem.comm.json.DWARWriter_JSON.ColumnDefinition;
import com.idorsia.research.chem.comm.json.DWARWriter_JSON.FILE_TYPE;
import com.idorsia.research.chem.comm.json.JSONTools;
import com.idorsia.research.chem.comm.json.JSONTools.DWARConversionException;

import org.eclipse.swt.layout.RowData;
import org.json.JSONObject;
import org.knime.base.data.filter.row.dialog.model.ColumnSpec;
import org.knime.base.node.parallel.appender.ColumnDestination;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
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
import org.knime.core.util.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

public class OCLFileWriterNodeModel extends NodeModel {
	
	private static final NodeLogger LOGGER = NodeLogger.getLogger(OCLFileWriterNodeModel.class);

	
    private static final int IN_PORT = 0;

    //~--- fields -------------------------------------------------------------

    private final OCLFileWriterNodeSettings m_settings = new OCLFileWriterNodeSettings();

    //~--- constructors -------------------------------------------------------

    public OCLFileWriterNodeModel() {
        super(1, 2);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String outputFileString = (m_settings == null)
                ? null
                : m_settings.getOutputFile();
        String[] selectedColumns = (m_settings == null)
                ? null
                : getSelectedColumns(inputSpec);
        boolean overwriteFile = (m_settings == null)
                ? false
                : m_settings.isOverwriteFile();

        FileFormat fileFormat = m_settings.getFileFormat();

        if ( ((outputFileString == null) || outputFileString.isEmpty()) && !m_settings.isCreateJSON()) {
            throw new InvalidSettingsException("No output file has been defined and no JSON output is created.");
        }

        if (selectedColumns.length == 0) {
            throw new InvalidSettingsException("At least one column needs to be selected to be written to the file.");
        }


        File outputFile = new File(outputFileString);

        if (outputFile.exists() && !overwriteFile) {
            throw new InvalidSettingsException(
                    "The output file '" + outputFileString
                            + "' exists already. Please select a different file or check the overwrite file option in the node settings.");
        }

        if (outputFile.exists() && !outputFile.canWrite()) {
            throw new InvalidSettingsException("You do not have the necessary permissions to write the outputfile '" + outputFileString
                    + ".");
        }

        Map<String,Integer> availableColumnNames = new HashMap<>();
        List<Pair<Integer,String>> availableColumns = new ArrayList<Pair<Integer,String>>();
        for (int a = 0; a < inputSpec.getNumColumns(); a++) {
            DataColumnSpec columnSpec = inputSpec.getColumnSpec(a);
            availableColumns.add(new Pair<Integer,String>(a,columnSpec.getName()));
            availableColumnNames.put(columnSpec.getName(),a);
        }

        List<Pair<Integer,String>> selectedColumnsA = new ArrayList<>();
        for (String selectedColumn : selectedColumns) {
            if (!availableColumnNames.keySet().contains(selectedColumn)) {
                throw new InvalidSettingsException("Input table does not contain a column named " + selectedColumn + ". Please "
                        + "(re-)configure the node.");
            }
            selectedColumnsA.add(new Pair<Integer,String>(availableColumnNames.get(selectedColumn),selectedColumn));
        }

        if (fileFormat != FileFormat.DWAR) {
            List<DataColumnSpec> moleculeColumnSpecs = new ArrayList<>();
            List<DataColumnSpec> descriptorColumnSpecs = new ArrayList<>();
            for (String selectedColumn : selectedColumns) {
                DataColumnSpec columnSpec = inputSpec.getColumnSpec(selectedColumn);
                if (SpecHelper.isMoleculeSpec(columnSpec)) {
                    moleculeColumnSpecs.add(columnSpec);
                } else if (SpecHelper.isDescriptorSpec(columnSpec)) {
                    moleculeColumnSpecs.add(columnSpec);
                }

                if (moleculeColumnSpecs.size() > 1) {
                    throw new InvalidSettingsException("Multiple structures are not allowed in SD files. Please select only one column with structures.");
                }

                if (!descriptorColumnSpecs.isEmpty()) {
                    throw new InvalidSettingsException("Descriptors cannot be written to SD files. Please remove all descriptor columns from the columns that are selected to be written to the output file.");
                }
            }
        }
        
        DataColumnSpec    outputColSpec_json      = SpecHelper.createStringColumnSpec("json");
        
        // create filtered table:
        final ColumnRearranger c_filtered = createColumnRearrangerFiltered(inputSpec, selectedColumns);       
        DataTableSpec outputTableSpecs = new DataTableSpec(new DataColumnSpec[] {outputColSpec_json});       
        
        return new DataTableSpec[] { outputTableSpecs , c_filtered.createSpec() };
    }

    private ColumnRearranger createColumnRearrangerFiltered(DataTableSpec inputSpec, String[] selectedColumns) {
    	final ColumnRearranger c_filtered = new ColumnRearranger(inputSpec);
        c_filtered.keepOnly(selectedColumns);
        return c_filtered;
    }
    
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        BufferedDataTable inputDataTable = inData[IN_PORT];
        DataTableSpec inputTableSpecs = inputDataTable.getDataTableSpec();

        String outputFileString = (m_settings == null)
                ? null
                : m_settings.getOutputFile();
        String[] selectedColumns = (m_settings == null)
                ? null
                : getSelectedColumns(inputTableSpecs);
        boolean overwriteFile = (m_settings == null)
                ? false
                : m_settings.isOverwriteFile();
        FileFormat fileFormat = m_settings.getFileFormat();

        File outputFile = new File(outputFileString);
        int[] outputColumnsIndexes = new int[selectedColumns.length];

        for (int idx = 0; idx < selectedColumns.length; idx++) {
            String selectedColumn = selectedColumns[idx];

            outputColumnsIndexes[idx] = SpecHelper.getColumnIndex(inputTableSpecs, selectedColumn);
        }


        if(m_settings.getOutputFile()!=null && !m_settings.getOutputFile().isEmpty() ) {
	        switch (fileFormat) {
	            case DWAR: {
	                //writeDwarFile(inputDataTable, inputTableSpecs, selectedColumns, outputFile, outputColumnsIndexes);
	            	//writeDwarFile(inputDataTable, inputTableSpecs, selectedColumns, outputFile);
	            	File dwat_file = null;
	            	if(m_settings.getTemplateFile() != null ) {
	            		if(!m_settings.getTemplateFile().isEmpty()) {
	            			dwat_file = new File(m_settings.getTemplateFile());		
	            		}
	            	}
	            	writeToJSON_or_DWAR(FILE_TYPE.DWAR, inputDataTable, inputTableSpecs, selectedColumns, outputFile, dwat_file);
	                break;
	            }
	            case SDF_V2:
	            case SDF_V3: {
	                int moleculeColumnIdx = -1;
	                int[] otherColumnIndexes = new int[outputColumnsIndexes.length - 1];
	                int idx = 0;
	                for (int outputColumnsIndex : outputColumnsIndexes) {
	                    if (SpecHelper.isMoleculeSpec(inputTableSpecs.getColumnSpec(outputColumnsIndex))) {
	                        moleculeColumnIdx = outputColumnsIndex;
	                    } else {
	                        otherColumnIndexes[idx++] = outputColumnsIndex;
	                    }
	                }
	                writeSDFile(inputDataTable, inputTableSpecs, moleculeColumnIdx, otherColumnIndexes, outputFile, fileFormat);
	            }
	        }
        }
        
        // Create the output table always, but only fill it with data if the create json is checked.
        DataColumnSpec    outputColSpec  = SpecHelper.createStringColumnSpec("json");
        DataTableSpec outputTableSpecs = new DataTableSpec(new DataColumnSpec[] {outputColSpec});
        
        BufferedDataContainer container = exec.createDataContainer(outputTableSpecs);
        
        if(m_settings.isCreateJSON()) {
        	String json_output = writeToJSON_or_DWAR(FILE_TYPE.JSON ,inputDataTable, inputTableSpecs, selectedColumns, null , null);
        	
            RowKey  row_key  = new RowKey("Row 0");
            DataCell[] cells = new DataCell[1];
            cells[0]         = new StringCell(json_output);
            DataRow data_row = new DefaultRow(row_key,cells);
            container.addRowToTable(data_row);
        }
        else {
        	RowKey  row_key  = new RowKey("Row 0");
            DataCell[] cells = new DataCell[1];
            cells[0]         = new StringCell("{}");
            DataRow data_row = new DefaultRow(row_key,cells);
            container.addRowToTable(data_row);
        }
        container.close();
        
        
        ColumnRearranger c_filtered = createColumnRearrangerFiltered(inData[0].getDataTableSpec(),getSelectedColumns(inData[0].getDataTableSpec()));
        BufferedDataTable outTable_filtered = exec.createColumnRearrangeTable(inData[0],
                c_filtered, exec);
        //return new BufferedDataTable[]{outTable};
        
        return new BufferedDataTable[]{container.getTable(), outTable_filtered};
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
        //OCLFileWriterNodeSettings validated_settings = new OCLFileWriterNodeSettings();
        //validated_settings.loadSettingsForModel(nodeSettingsRO);
    	//m_settings.loadSettingsForModel(nodeSettingsRO);
    }

    private void writeSDFile(BufferedDataTable inputDataTable, DataTableSpec inputTableSpecs, int moleculeColumnIdx, int[] otherColumnIndexes, File outputFile, FileFormat fileFormat) throws IOException {
        SDFileWriter writer = new SDFileWriter(outputFile, fileFormat);
        writer.open();
        CloseableRowIterator iterator = inputDataTable.iterator();
        String[] columnNames = new String[otherColumnIndexes.length];
        for (int i = 0; i < otherColumnIndexes.length; i++) {
            columnNames[i] = inputTableSpecs.getColumnSpec(otherColumnIndexes[i]).getName();
        }
        while (iterator.hasNext()) {
            DataRow dataRow = iterator.next();
            StereoMolecule mol = ValueHelper.getMolecule(dataRow.getCell(moleculeColumnIdx));
            writer.writeMolecule(mol);

            for (int i = 0; i < otherColumnIndexes.length; i++) {
                writer.writeField(columnNames[i], dataRow.getCell(otherColumnIndexes[i]).toString());
            }
            writer.endRecord();
        }

        iterator.close();
        writer.close();
    }

    /**
     * Can write either json or dwar.
     * If it is used to create 
     * 
     * @param file_type
     * @param inputDataTable
     * @param inputTableSpecs
     * @param selectedColumns
     * @param outputFile
     * @param dwat_file
     * @return
     */
    private String writeToJSON_or_DWAR(DWARWriter_JSON.FILE_TYPE file_type, BufferedDataTable inputDataTable, DataTableSpec inputTableSpecs, String[] selectedColumns, File outputFile, File dwat_file) throws IOException, DWARConversionException {
    	List<DWARWriter_JSON.ColumnDefinition> column_definitions = new ArrayList<>();
    	
    	// It is possible, that one Knime column has to be expanded into multiple DWAR columns (e.g. ConformerList generates two column)
        // This map resolves the source column in the knime buffered table.
        Map<Integer,Integer> map_target_column_to_source_column = new HashMap<>(); 
        
        int idx_target_column = 0;
        
        Map<String,String> properties_structure = new HashMap<>();
        properties_structure.put(CompoundTableConstants.cColumnPropertySpecialType,CompoundTableConstants.cColumnTypeIDCode);
        
        Map<String,String> properties_3dcoords  = new HashMap<>();
        properties_3dcoords.put(CompoundTableConstants.cColumnPropertySpecialType,CompoundTableConstants.cColumnType3DCoordinates);        
        
        for (int idx = 0; idx < selectedColumns.length; idx++) {
            String selectedColumn = selectedColumns[idx];

            DataColumnSpec columnSpec = inputTableSpecs.getColumnSpec(selectedColumn);
            int source_column_idx     = inputTableSpecs.findColumnIndex(selectedColumn);
            if(SpecHelper.isConformerListSpec(columnSpec)) {
            	System.out.println("Create ConformerListSpec!");            	
            	//System.out.println("header size 1 -> "+header.size());
            	String tag_structure = "Structure["+columnSpec+"]";
            	String tag_coords    = "3DCoordinates["+columnSpec+"]";
                
           // 	header.addStructureHeader(tag_structure);
            	Map<String,String> properties_struc_a = new HashMap<>(properties_structure);
                column_definitions.add(new ColumnDefinition(tag_structure, properties_struc_a));
            	
                //System.out.println("header size 2 -> "+header.size());
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;                      

          //      header.addCoordinates3DHeader(tag_structure, tag_coords);
            	Map<String,String> properties_3d_a = new HashMap<>(properties_3dcoords);
            	properties_3d_a.put(CompoundTableConstants.cColumnPropertyParentColumn,tag_structure);
                column_definitions.add(new ColumnDefinition(tag_coords, properties_3d_a));
                
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;                      
                //System.out.println("header size 3 -> "+header.size());
            }                  
            else if (SpecHelper.isMoleculeSpec(columnSpec)) {
            	System.out.println("Create MoleculeSpec: "+selectedColumn);            	
          //      header.addStructureHeader(selectedColumn);
            	Map<String,String> properties_struc_a = new HashMap<>(properties_structure);
                column_definitions.add(new ColumnDefinition(selectedColumn, properties_struc_a));
            	
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;
            }
            else if (SpecHelper.isDescriptorSpec(columnSpec)) {
            	System.out.println("Create DescriptorSpec: "+selectedColumn);
            	String descriptorName = columnSpec.getProperties().getProperty( SpecHelper.DESCRIPTOR_INFO );
            	
//            	String descriptorName_01 = new String(selectedColumn);
//            	String splits[] = descriptorName_01.split("\\s");
//            	String strippedDescriptorName = splits[0];
            	            
            	//DescriptorInfo di    = DescriptorHelpers.getDescriptorInfoByShortName(strippedDescriptorName);
            	DescriptorInfo di    = DescriptorHelpers.getDescriptorInfoByShortName(descriptorName);
            	
            	if(di!=null) {            		
            		DescriptorHandler dh = DescriptorHelpers.getDescriptorHandler(di);                                	              
	                DataColumnSpec parentColumnSpec = SpecHelper.getParentColumnSpec(inputTableSpecs, columnSpec);
	                if (parentColumnSpec != null) {
	    //                header.add(new DWARHeaderTag(selectedColumn));
	    //                header.addProperty(selectedColumn, DWARHeader.COLPROP_SPTYPE, selectedColumn);
	    //                header.addProperty(selectedColumn, DWARHeader.COLPROP_VERSION, dh.getVersion());
	    //                header.addProperty(selectedColumn, DWARHeader.COLPROP_PARENT, parentColumnSpec.getName());
	                	Map<String,String> descriptor_properties = new HashMap<>();
	                	//descriptor_properties.put(CompoundTableConstants.cColumnPropertySpecialType, selectedColumn);
	                	descriptor_properties.put(CompoundTableConstants.cColumnPropertySpecialType, dh.getInfo().shortName);
	                	descriptor_properties.put(CompoundTableConstants.cColumnPropertyDescriptorVersion, dh.getVersion());
	                	descriptor_properties.put(CompoundTableConstants.cColumnPropertyParentColumn, parentColumnSpec.getName());
	                	column_definitions.add(new ColumnDefinition(selectedColumn,descriptor_properties));
	                    
	                    map_target_column_to_source_column.put(idx_target_column,source_column_idx);
	                    idx_target_column++;
	                }
                }
                else {
                	LOGGER.warn("Problem with descriptor in columns: "+selectedColumn);
                	idx_target_column++;
                }
            }
            else {
            	LOGGER.info("Create DataSpec: "+selectedColumn);
       //         header.add(selectedColumn);
            	column_definitions.add(new ColumnDefinition(selectedColumn));
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;                
            }
        }    	
    	
        
        Map<String,Integer> colNameToColIdx = new HashMap<>();
        for(int zi = 0;zi<column_definitions.size();zi++) {
        	ColumnDefinition cdi = column_definitions.get(zi);
        	colNameToColIdx.put(cdi.getName(),zi);
        }
        
        // now write header, and then write data..
        try {
        	
        	DWARWriter_JSON writer = null;
        	
        	if(file_type == DWARWriter_JSON.FILE_TYPE.JSON) {
        		writer = new DWARWriter_JSON(column_definitions);
        	}
        	else if(file_type == DWARWriter_JSON.FILE_TYPE.DWAR) {
        		// now there are two possibilities, either we have a blueprint ".dwar" file set as template file,
        		// OR we have the "normal" situation with or without a template ".dwat" file.
        		
        		BufferedWriter output = new BufferedWriter( new FileWriter( outputFile ) );        		
        		ArrayList<String> dwat_code = new ArrayList<>();
        		
        		boolean has_blueprint_dwar = false;
        		if(dwat_file != null) {
        			if(dwat_file.getName().toLowerCase().endsWith(".dwar")) {
        				has_blueprint_dwar = true;
        			}
        		}
        		
        		
        		// now we have two ways to initialize the file, depending on if we have a blueprint dwar:
        		if(!has_blueprint_dwar) {
	        		//!! PARSING THE DWAT FILE: skip the first and the last line!!, i.e. the two lines:
	        		//   <datawarrior properties>
	        		//   </datawarrior properties>
	        		if(dwat_file != null) {
		        		// try to parse the template dwat file:
		        		BufferedReader in = new BufferedReader(new FileReader(dwat_file));
		        		String next_line = null;
		        		while( (next_line=in.readLine()) != null ) {
		        			dwat_code.add(next_line);
		        		}
		        		// we remove the first and the last line and hope for the best ;)
		        		dwat_code = new ArrayList<>( dwat_code.subList(1,dwat_code.size()-1) );
		        		in.close();
	        		}
	        		writer = new DWARWriter_JSON(column_definitions,output,dwat_code);
	        		
	        		
	        		
	        	}
        		else {
        			// Just use the DWAR File as blueprint, and supply the column definitions to the
        			// constructor to check if this matches with the node configuration.
        			
        			// NOTE: We do not have to match columns / column names.
        			//
        			//       This means, we change:
        			//		 1. the column order of column_definitions
        			//       2. the map_target_column_to_source_column
        			
        			LOGGER.info("[INFO] Load blueprint .dwar");
        			
        			//System.out.println("[INFO] Rearrange columns based on blueprint col names");
        			//LOGGER.info("Found blueprint .dwar -> Rearrange columns based on blueprint col names");
        			
        			Map<Integer,Integer> map_target_column_to_source_column_adapted = new HashMap<>();
        			List<ColumnDefinition> column_definitions_adapted = new ArrayList<>();
        			
        			Map<String,ColumnDefinition> original_col_defs      = new HashMap<>();
        			Map<String,Integer> original_col_positions = new HashMap<>();
        			for(int zi=0;zi<column_definitions.size();zi++) {
        				ColumnDefinition di = column_definitions.get(zi);
        				original_col_defs.put(di.getName(),di);
        				original_col_positions.put(di.getName(),zi);
        			}
        			
        			// NOTE!! All this code is just to ensure that we find a good mapping.
        			// We do NOT have to use the adapted mapping here in the nodes, the DWARWwriter_JSON takes
        			// as in put the data sorted according the the column definitions during its construction.
        			DWARFileParser parser_blueprint = new DWARFileParser(dwat_file);
        			List<ColumnDefinition> cols_bp = JSONTools.analyzeDWARColumns(parser_blueprint);
        			parser_blueprint.close();
        			for(int zi=0;zi<cols_bp.size();zi++) {
        				ColumnDefinition cdi = cols_bp.get(zi);
        				if(!original_col_defs.containsKey(cdi.getName())) {
        					throw new Exception("Could not map blueprint column name: "+cdi.getName());
        				}
        				else {
        					column_definitions_adapted.add(original_col_defs.get(cdi.getName()));
        					int original_source_col = map_target_column_to_source_column.get(original_col_positions.get(cdi.getName()));
        					map_target_column_to_source_column_adapted.put(zi,original_source_col);
        				}
        				LOGGER.debug("map "+original_col_positions.get(cdi.getName()) + " -> " + zi +" : "+cdi.getName());
        			}
        			// DO NOT change to adapted versions:
        			//column_definitions = column_definitions_adapted;
        			//map_target_column_to_source_column = map_target_column_to_source_column_adapted;
        			writer = new DWARWriter_JSON(dwat_file,output,column_definitions);
        		}
        	}
        	CloseableRowIterator iterator = inputDataTable.iterator();
        
        	while (iterator.hasNext()) {
                DataRow dataRow = iterator.next();                
                //DWARRecord dwarRecord = new DWARRecord();
                List<String> row_i = new ArrayList<>(); 
                for(int zi=0;zi<column_definitions.size();zi++) {
                	row_i.add("");
                }

                //for (int idx = 0; idx < selectedColumns.length; idx++) {
                int idx = 0;
                while (idx < map_target_column_to_source_column.size() ) {
                    //String selectedColumn = selectedColumns[idx];
                    //int cellIdx = columnIdx[idx];
                    //String colName = selectedColumns[map_target_column_to_source_column.get(idx)];
                	
             //   	String targetColName = header.get(idx);
                	String targetColName = column_definitions.get(idx).getName();
                	
                	if(!map_target_column_to_source_column.containsKey(idx)) {
                		//LOG.warn("missing column info for col="+idx);
                		//System.out.println("[WARN] missing column info for col="+idx);
                		LOGGER.warn("missing column info for col="+idx);
                		row_i.set( colNameToColIdx.get(targetColName) , "");
                        idx++;
                        continue;
                	}
                    int source_col_idx = map_target_column_to_source_column.get(idx);
                                    
                    //DataCell cell = dataRow.getCell(cellIdx);
                    DataCell cell = dataRow.getCell(source_col_idx);
                    if(cell.isMissing()) {
                    	row_i.set(colNameToColIdx.get(targetColName) , "");
                    	idx++;
                    }
                    else {
                    	if(SpecHelper.isConformerListSpec(inputTableSpecs.getColumnSpec(source_col_idx))) {
                    		StereoMolecule mol = ((OCLConformerListDataValue)cell).getMolecule2D();
                    		//dwarRecord.addField(targetColName, mol);
                    		row_i.set( colNameToColIdx.get(targetColName) , mol.getIDCode());

                    		idx++;
                    		//targetColName = header.get(idx);
                    		targetColName = column_definitions.get(idx).getName();

                    		//dwarRecord.addField(targetColName, ((OCLConformerListDataValue)cell).getCoordinateIDCodesInOneString());
                    		row_i.set(colNameToColIdx.get(targetColName), ((OCLConformerListDataValue)cell).getCoordinateIDCodesInOneString());

                    		idx++;
                    	}
                    	else if (SpecHelper.isMoleculeSpec(inputTableSpecs.getColumnSpec(source_col_idx))) {
                    		StereoMolecule mol = ValueHelper.getMolecule(cell);
                    		//dwarRecord.addField(targetColName, mol);
                    		row_i.set( colNameToColIdx.get(targetColName) , mol.getIDCode());
                    		idx++;
                    	} else {
                    		//dwarRecord.addField(targetColName, cell.toString());
                    		row_i.set(colNameToColIdx.get(targetColName) , cell.toString());
                    		idx++;
                    	}
                    }
                }
                //dwarFileWriter.write(dwarRecord);
                writer.addDataRow(row_i);
            }
        	iterator.close();        	
        	
        	if(file_type == FILE_TYPE.JSON) {
        		return writer.toString();
        	}
        	else if(file_type == FILE_TYPE.DWAR) {
        		writer.finalizeDWARFile();
        		return "";
        	}
        }
        catch(Exception ex) {
        	ex.printStackTrace();
        	throw new DWARConversionException("exception encountered: "+ex.getMessage());
        }
    	
    	return "";
    }
    
    //private void writeDwarFile(BufferedDataTable inputDataTable, DataTableSpec inputTableSpecs, String[] selectedColumns, File outputFile, int[] columnIdx) throws IOException {
    private void writeDwarFile_old(BufferedDataTable inputDataTable, DataTableSpec inputTableSpecs, String[] selectedColumns, File outputFile) throws IOException {
    	
        DWARHeader header = new DWARHeader();
        
        // It is possible, that one Knime column has to be expanded into multiple DWAR columns (e.g. ConformerList generates two column)
        // This map resolves the source column in the knime buffered table.
        Map<Integer,Integer> map_target_column_to_source_column = new HashMap<>(); 
        
        int idx_target_column = 0;
        
        for (int idx = 0; idx < selectedColumns.length; idx++) {
            String selectedColumn = selectedColumns[idx];

            DataColumnSpec columnSpec = inputTableSpecs.getColumnSpec(selectedColumn);
            int source_column_idx     = inputTableSpecs.findColumnIndex(selectedColumn);
            if(SpecHelper.isConformerListSpec(columnSpec)) {
            	System.out.println("Create ConformerListSpec!");            	
            	//System.out.println("header size 1 -> "+header.size());
            	String tag_structure = "Structure["+columnSpec+"]";
            	String tag_coords    = "3DCoordinates["+columnSpec+"]";
                header.addStructureHeader(tag_structure);            
                //System.out.println("header size 2 -> "+header.size());
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;                      

                header.addCoordinates3DHeader(tag_structure, tag_coords);
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;                      
                //System.out.println("header size 3 -> "+header.size());
            }                  
            else if (SpecHelper.isMoleculeSpec(columnSpec)) {
            	System.out.println("Create MoleculeSpec: "+selectedColumn);            	
                header.addStructureHeader(selectedColumn);
                //map_target_column_to_source_column.put(idx_target_column,idx);
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;
            }
            else if (SpecHelper.isDescriptorSpec(columnSpec)) {
            	System.out.println("Create DescriptorSpec: "+selectedColumn);
            	String descriptorName = columnSpec.getProperties().getProperty( SpecHelper.DESCRIPTOR_INFO );
            	
//            	String descriptorName_01 = new String(selectedColumn);
//            	String splits[] = descriptorName_01.split("\\s");
//            	String strippedDescriptorName = splits[0];
            	            
            	//DescriptorInfo di    = DescriptorHelpers.getDescriptorInfoByShortName(strippedDescriptorName);
            	DescriptorInfo di    = DescriptorHelpers.getDescriptorInfoByShortName(descriptorName);
            	
            	if(di!=null) {            		
            		DescriptorHandler dh = DescriptorHelpers.getDescriptorHandler(di);                                	              
	                DataColumnSpec parentColumnSpec = SpecHelper.getParentColumnSpec(inputTableSpecs, columnSpec);
	                if (parentColumnSpec != null) {
	                    header.add(new DWARHeaderTag(selectedColumn));
	                    header.addProperty(selectedColumn, DWARHeader.COLPROP_SPTYPE, selectedColumn);
	                    header.addProperty(selectedColumn, DWARHeader.COLPROP_VERSION, dh.getVersion());
	                    header.addProperty(selectedColumn, DWARHeader.COLPROP_PARENT, parentColumnSpec.getName());
	                    
	                    map_target_column_to_source_column.put(idx_target_column,source_column_idx);
	                    idx_target_column++;
	                }
                }
                else {
                	System.out.println("Problem with descriptor in columns: "+selectedColumn);                	
                }
            }
            else {
            	System.out.println("Create DataSpec: "+selectedColumn+ "Map: T="+idx_target_column+" S="+source_column_idx);
                header.add(selectedColumn);
                map_target_column_to_source_column.put(idx_target_column,source_column_idx);
                idx_target_column++;                
            }
        }

        DWARFileWriter dwarFileWriter = new DWARFileWriter(outputFile, header);
        

        dwarFileWriter.setRowCount(inputDataTable.getRowCount());

        CloseableRowIterator iterator = inputDataTable.iterator();

        System.out.println("Header: ");
        for(int zi=0;zi<map_target_column_to_source_column.size();zi++) {
        	String targetColName = header.get(zi);
        	System.out.println(targetColName);
        }
        
        while (iterator.hasNext()) {
            DataRow dataRow = iterator.next();
            DWARRecord dwarRecord = new DWARRecord();

            //for (int idx = 0; idx < selectedColumns.length; idx++) {
            int idx = 0;
            while (idx < map_target_column_to_source_column.size() ) {
                //String selectedColumn = selectedColumns[idx];
                //int cellIdx = columnIdx[idx];
                //String colName = selectedColumns[map_target_column_to_source_column.get(idx)];
            	String targetColName = header.get(idx);
                int source_col_idx = map_target_column_to_source_column.get(idx);
                                
                //DataCell cell = dataRow.getCell(cellIdx);
                DataCell cell = dataRow.getCell(source_col_idx);

                if(SpecHelper.isConformerListSpec(inputTableSpecs.getColumnSpec(source_col_idx))) {
                	System.out.println("idx="+idx+" source_col_idx="+ source_col_idx+" : conformerList");
                	StereoMolecule mol = ((OCLConformerListDataValue)cell).getMolecule2D();
                	dwarRecord.addField(targetColName, mol);
                	idx++;
                	targetColName = header.get(idx);
                	dwarRecord.addField(targetColName, ((OCLConformerListDataValue)cell).getCoordinateIDCodesInOneString());
                	idx++;
                }
                else if (SpecHelper.isMoleculeSpec(inputTableSpecs.getColumnSpec(source_col_idx))) {
                	System.out.println("idx="+idx+" source_col_idx="+ source_col_idx+" : Molecule");
                    StereoMolecule mol = ValueHelper.getMolecule(cell);
                    dwarRecord.addField(targetColName, mol);
                    idx++;
                } else {
                	System.out.println("idx="+idx+" source_col_idx="+ source_col_idx+" : Other (Data)");
                    dwarRecord.addField(targetColName, cell.toString());
                    idx++;
                }
            }

            dwarFileWriter.write(dwarRecord);
        }

        iterator.close();
        dwarFileWriter.close();
    }

    private String[] getSelectedColumns(DataTableSpec inputSpec) {
        String[] columns = null;
        if (m_settings.isIncludeAllColumns()) {

            List<String> allColumns = new ArrayList<String>();
            for (int i = 0; i < inputSpec.getNumColumns(); i++) {
                allColumns.add(inputSpec.getColumnSpec(i).getName());
            }

            columns = new String[allColumns.size()];
            for (int i = 0; i < allColumns.size(); i++) {
                columns[i] = allColumns.get(i);
            }
        } else {
            columns = m_settings.getSelectedColumns();
        }
        return columns;
    }


}
