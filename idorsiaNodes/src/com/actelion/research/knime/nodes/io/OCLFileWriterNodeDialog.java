package com.actelion.research.knime.nodes.io;

import com.actelion.research.gui.JEditableStructureView;
import com.actelion.research.knime.ui.FileChooserPane;
import com.actelion.research.knime.ui.MultiSelectionPane;

import info.clearthought.layout.TableLayout;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLFileWriterNodeDialog extends NodeDialogPane {
    private final OCLFileWriterNodeSettings m_settings = new OCLFileWriterNodeSettings();
    private JComboBox<String> inputColumnComboBox;
    private JComboBox<FileFormat> fileFormatComboBox;
    private JEditableStructureView mStructureView;
    private FileChooserPane outputFilePane;
    private FileChooserPane templateFilePane;
    private JCheckBox overwriteCheckbox;
    private JCheckBox createJSONOutput;
    private MultiSelectionPane columnSelectionPane;
    private Map<FileFormat, List<String>> fileFormat2ColumnMap = new HashMap<>();
    //~--- constructors -------------------------------------------------------

    public OCLFileWriterNodeDialog() {
        this.addTab("Write file settings", this.createSimilarityTab());
        fileFormatComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateFileFormat();
            }
        });
    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {

        this.m_settings.loadSettingsForDialog(settings);

        String outputFileString = this.m_settings.getOutputFile();
        FileFormat fileFormat = this.m_settings.getFileFormat();
        File outputFile = new File(outputFileString);
        outputFilePane.setSelectedFile(outputFile);
       

        overwriteCheckbox.setSelected(this.m_settings.isOverwriteFile());

        DataTableSpec inputSpecs = specs[0];
        buildFileTypeToColumnMap(inputSpecs);
        List<String> selectedColumns = Arrays.asList(this.m_settings.getSelectedColumns());
        columnSelectionPane.setSelection(selectedColumns, fileFormat2ColumnMap.get(fileFormat));
        fileFormatComboBox.setSelectedItem(m_settings.getFileFormat());

        String templateFileString = this.m_settings.getTemplateFile();
        File templateFile = new File(templateFileString);
        templateFilePane.setSelectedFile(templateFile);
        
        boolean create_json = this.m_settings.isCreateJSON();
        createJSONOutput.setSelected(create_json);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setOutputFile(outputFilePane.getSelectedFile().toString());
        this.m_settings.setOverwriteFile(overwriteCheckbox.isSelected());
        this.m_settings.setSelectedColumns(columnSelectionPane.getSelectedItems());        
        this.m_settings.setFileFormat((FileFormat) fileFormatComboBox.getSelectedItem());
        this.m_settings.setTemplateFile(this.templateFilePane.getSelectedFile().toString());
        this.m_settings.setCreateJSON(createJSONOutput.isSelected());
        this.m_settings.saveSettings(settings);
    }


    //~--- methods ------------------------------------------------------------

    private void buildFileTypeToColumnMap(DataTableSpec inputSpecs) {
        fileFormat2ColumnMap.clear();
        for (String columnName : inputSpecs.getColumnNames()) {
            for (FileFormat fileFormat : FileFormat.values()) {
                List<String> columns = fileFormat2ColumnMap.get(fileFormat);
                if (columns == null) {
                    columns = new ArrayList<>();
                    fileFormat2ColumnMap.put(fileFormat, columns);
                }
                if (fileFormat != null && fileFormat.supports(inputSpecs.getColumnSpec(columnName))) {
                    columns.add(columnName);
                }
            }
        }
    }

    private void updateFileFormat() {
        FileFormat fileFormat = (FileFormat) fileFormatComboBox.getSelectedItem();
        if (outputFilePane != null) {
            outputFilePane.setFileFilter(fileFormat.getFileDescription(), fileFormat.getExtensions());
        }

        List<String> newSelection = new ArrayList<>(Arrays.asList(columnSelectionPane.getSelectedItems()));
        List<String> availableColumns = fileFormat2ColumnMap.get(fileFormat);
        newSelection.retainAll(availableColumns);
        columnSelectionPane.setSelection(newSelection, availableColumns);
        
        if(fileFormat==FileFormat.DWAR) {
        	this.templateFilePane.getContentPane().setEnabled(true);
        }
        else {
        	this.templateFilePane.getContentPane().setEnabled(false);
        }
    }

    private JPanel createSimilarityTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4}, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4
        }
        };
        TableLayout layout = new TableLayout(sizes);
        fileFormatComboBox = new JComboBox<>(FileFormat.values());
        outputFilePane = createOutputFilePane();
        overwriteCheckbox = new JCheckBox("Overwrite file?");
        createJSONOutput  = new JCheckBox("Create JSON output?");
        columnSelectionPane = createColumnSelectionPane();
        templateFilePane = createTemplateFilePane();
        
        p.setLayout(layout);

        int row = 1;
        p.add(new JLabel("Output file"), "1," + row);
        p.add(outputFilePane.getContentPane(), "3, " + row);
        row += 2;

        p.add(new JLabel("Format"), "1," + row);
        p.add(fileFormatComboBox, "3, " + row);
        row += 2;

        JPanel p_overwrite_and_template = new JPanel();
        p_overwrite_and_template.setLayout(new FlowLayout(FlowLayout.LEFT));
        p_overwrite_and_template.add(createJSONOutput);
        p_overwrite_and_template.add(new JLabel(" "));
        p_overwrite_and_template.add(overwriteCheckbox);
        p_overwrite_and_template.add(new JLabel("   Template File / Blueprint File "));
        p_overwrite_and_template.add(templateFilePane.getContentPane());
        //p.add(overwriteCheckbox, "1, " + row + ", 3," + row);
        p.add(p_overwrite_and_template,"1, " + row + ", 3," + row);
        row += 2;
        p.add(columnSelectionPane.getContentPane(), "1," + row + ", 3, " + row);

        return p;
    }

    private MultiSelectionPane createColumnSelectionPane() {
        return new MultiSelectionPane();
    }

    private FileChooserPane createOutputFilePane() {
        FileFormat fileFormat = (FileFormat) fileFormatComboBox.getSelectedItem();
        return new FileChooserPane(FileChooserPane.Mode.SAVE, fileFormat.getFileDescription(), fileFormat.getExtensions());
    }
    
    private FileChooserPane createTemplateFilePane() {
    	return new FileChooserPane(FileChooserPane.Mode.SAVE, "DWAT/DWAR File", new String[] {"dwat","dwar"});
    }
}
