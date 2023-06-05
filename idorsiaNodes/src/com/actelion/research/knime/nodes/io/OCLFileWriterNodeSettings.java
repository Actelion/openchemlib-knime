package com.actelion.research.knime.nodes.io;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLFileWriterNodeSettings extends AbstractOCLNodeSettings {
    private static final String OUTPUT_FILE = "outputFile";
    private static final String OVERWRITE_FILE = "overwriteFile";
    private static final String SELECTED_COLUMNS = "selectedColumns";
    private static final String INCLUDE_ALL_COLUMNS = "includeAllColumns";
    private static final String FILE_FORMAT = "fileFormat";
    private static final String DWAR_TEMPLATE_FILE = "templateFile";
    private static final String CREATE_JSON_OUTPUT = "createJSON";

    //~--- fields -------------------------------------------------------------

    private DataCell defaultValue = DataType.getMissingCell();
    private DataCell mapMissingTo = DataType.getMissingCell();
    private String outputFile = "";
    private String[] selectedColumns = new String[0];
    private FileFormat fileFormat = FileFormat.DWAR;
    private boolean includeAllColumns = false;
    private boolean overwriteFile = false;
    private String  templateFile  = "";
    private boolean createJSON    = false;

    //~--- methods ------------------------------------------------------------

    @Override
    public void loadSettingsForDialog(NodeSettingsRO settings) {
        loadSettings(settings);
    }

    @Override
    public void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
        loadSettings(settings);
    }

    @Override
    public void saveSettings(NodeSettingsWO settings) {
        settings.addStringArray(SELECTED_COLUMNS, selectedColumns);
        settings.addString(OUTPUT_FILE, outputFile);
        settings.addBoolean(OVERWRITE_FILE, overwriteFile);
        settings.addBoolean(INCLUDE_ALL_COLUMNS, includeAllColumns);
        settings.addString(FILE_FORMAT, fileFormat.toString());
        settings.addBoolean(CREATE_JSON_OUTPUT, createJSON);
        settings.addString(DWAR_TEMPLATE_FILE, templateFile);
    }

    //~--- get methods --------------------------------------------------------

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String[] getSelectedColumns() {
        return selectedColumns;
    }

    //~--- set methods --------------------------------------------------------

    public void setSelectedColumns(String[] selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    public boolean isOverwriteFile() {
        return overwriteFile;
    }

    public void setOverwriteFile(boolean overwriteFile) {
        this.overwriteFile = overwriteFile;
    }

    public boolean isIncludeAllColumns() {
        return includeAllColumns;
    }

    public void setIncludeAllColumns(boolean includeAllColumns) {
        this.includeAllColumns = includeAllColumns;
    }

    public FileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
    
    public String getTemplateFile() {
    	return templateFile;
    }
    
    public void setTemplateFile(String templateFile) {
    	this.templateFile = templateFile;
    }

    public void setCreateJSON(boolean create) {
    	this.createJSON = create;
    }
    
    public boolean isCreateJSON() {
    	return this.createJSON;
    }
    
    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.selectedColumns = settings.getStringArray(SELECTED_COLUMNS, new String[0]);
        this.outputFile = settings.getString(OUTPUT_FILE, "");
        this.overwriteFile = settings.getBoolean(OVERWRITE_FILE, false);
        this.includeAllColumns = settings.getBoolean(INCLUDE_ALL_COLUMNS, false);
        this.fileFormat = FileFormat.fromString(settings.getString(FILE_FORMAT, ""));
        this.templateFile = settings.getString(DWAR_TEMPLATE_FILE, "");
        this.createJSON   = settings.getBoolean(CREATE_JSON_OUTPUT,false);
    }
}
