package com.actelion.research.knime.nodes.io;

import com.actelion.research.chem.io.DWARFileParser;
import com.actelion.research.chem.io.DWARFileParser.SpecialField;
import com.actelion.research.knime.ui.FileChooserPane;
import com.actelion.research.knime.ui.MultiSelectionPane;
import com.actelion.research.knime.utils.StructureFileHelpers;

import info.clearthought.layout.TableLayout;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel;
import org.knime.core.util.FileUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.*;

import static com.actelion.research.knime.utils.StructureFileHelpers.extractColumnNames;

//~--- JDK imports ------------------------------------------------------------

/**
 * <code>NodeDialog</code> for the "OCLFileReader" Node.
 * <p/>
 * <p/>
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Actelion Pharmaceuticals Ltd.
 */
public class OCLFileReaderNodeDialog extends NodeDialogPane implements FileChooserPane.FileChangeListener {
    private final OCLFileReaderNodeSettings m_settings = new OCLFileReaderNodeSettings();
    private JComboBox<String> inputColumnComboBox;
    private FileChooserPane inputFilePanel;
    private MultiSelectionPane columnSelectionPane;
    private MultiSelectionPane columnSelectionPaneSpecial;

    private Map<String,Map<String,DWARFileParser.SpecialField>> specialColumnsInfo;
    
    //~--- constructors -------------------------------------------------------

    public OCLFileReaderNodeDialog() {
        this.addTab("Read file settings", this.createInputFilePanel());
    }

    //~--- methods ------------------------------------------------------------

    public static File resolveFilePathOrKnimeURL(String path) {
    	String path_lc = path.toLowerCase();
    	File   file    = null;
    	if(path_lc.startsWith("knime://")) {
    		try {
    			file = FileUtil.getFileFromURL(new URL(path));
    		}
    		catch(Exception ex) {
    			return null;
    		}
    	}
    	else {
    		file = new File(path);
    	}
    	return file;
    }
    
    @Override
    public void fileChanged(String newFileURL) {
    	
    	File newFile = resolveFilePathOrKnimeURL(newFileURL);
    	
        List<String> allColumns = extractColumnNames(newFile);
        specialColumnsInfo = StructureFileHelpers.extractStructuresWithAdditionalInformation(newFile);
        
        // quick check if everything is alright..
        boolean file_is_ok = true;
        for(String si :  specialColumnsInfo.keySet()) {
        	if( specialColumnsInfo.get(si) == null ) {
        		file_is_ok = false;
        	}
        }
        
        if(!file_is_ok) {
        	this.m_settings.setInputFile("");
        	JOptionPane.showMessageDialog(null, "Error.. Something is not right with the supplied file.");
        	this.inputFilePanel.setSelectedFile(new File(""));
        	this.columnSelectionPane.setSelection(Collections.<String>emptyList(), new ArrayList<>());
        	this.columnSelectionPaneSpecial.setSelection(Collections.<String>emptyList(), new ArrayList<>());
        	return;
        }
        
        Map<String,String> specialColumnNames_array[] = createSpecialColumnNames(specialColumnsInfo);
        Map<String,String> specialColumnNames      = specialColumnNames_array[0];
        Map<String,String> specialColumnNames_inv  = specialColumnNames_array[1];
             
        List<String> allColumnsSpecial = new ArrayList<>(specialColumnNames_inv.keySet());
        
        columnSelectionPane.setSelection(Collections.<String>emptyList(), allColumns);                      
        columnSelectionPaneSpecial.setSelection(Collections.<String>emptyList(), allColumnsSpecial);
    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
    	
    	System.out.println("OCLFileReaderNode::loadSettingsFrom -> entered!");
    	
        this.m_settings.loadSettingsForDialog(settings);

        System.out.println("::loadSettingsFrom : InputFile= "+this.m_settings.getInputFile());
        
        
        String inputFileString = this.m_settings.getInputFile();
        File inputFile = resolveFilePathOrKnimeURL(inputFileString);//new File(inputFileString);

        specialColumnsInfo = StructureFileHelpers.extractStructuresWithAdditionalInformation(inputFile);
        
        Map<String,String> specialColumnNames_array[] = createSpecialColumnNames(specialColumnsInfo);
        Map<String,String> specialColumnNames      = specialColumnNames_array[0];
        Map<String,String> specialColumnNames_inv  = specialColumnNames_array[1];                       
        
        
        inputFilePanel.setSelectedFile(inputFile);

        List<String> allColumns = extractColumnNames(inputFile);
        List<String> allColumnsSpecial = new ArrayList<>(specialColumnNames_inv.keySet());
        
        List<String> selectedColumns_a = Arrays.asList(this.m_settings.getSelectedColumns(this.m_settings.isIncludeAllColumns()));        
        List<String> selectedColumnsSpecial_a = Arrays.asList(this.m_settings.getSelectedColumnsSpecial(this.m_settings.isIncludeAllColumns()));

        
        List<String> selectedColumns = selectedColumns_a;
        List<String> selectedColumnsSpecial = selectedColumnsSpecial_a.stream().map( si -> specialColumnNames.get(si) ).collect(Collectors.toList());
        
        
        columnSelectionPane.setSelection(selectedColumns, allColumns);
        columnSelectionPaneSpecial.setSelection(selectedColumnsSpecial, allColumnsSpecial);
             
        System.out.println("OCLFileReaderNode::loadSettingsFrom -> done!");
        
    }
    
    /**
     * Returns array with [0] : Map from special name to columnName , [1] : the inverse map
     *
     * NOTE! Here we actually also filter and show only colunns that we can interpret..
     * 
     * @param specialFields
     * @return
     */
    @SuppressWarnings("unchecked")
	private Map<String,String>[] createSpecialColumnNames( Map<String,Map<String,DWARFileParser.SpecialField>> specialFields ) {
    	Map<String,String> result     = new HashMap<>();    	
    	
    	// NOTE! If we encounter dependency trees with more than two levels, we will get columns multiple times.
    	//       Therefore we should check if we add them multiple times
    	
    	for(String ki : specialFields.keySet()) {
    		result.put(ki,ki+"[OCLMolecule]");    		
    		for( String si : specialFields.get(ki).keySet()  ) {
    			SpecialField sfi = specialFields.get(ki).get(si);
    			if(result.containsKey(si)) {
    				// duplicate, i.e. we skip this one.. (OR.. we could add more description to the description string in "result"..)
    				//continue;
    			}    			
    			
    			// check if we know this column type..
    			// TODO..
    			
    			result.put(si,si+"["+ki+":"+sfi.type+"]");
    		}    		
    	}    	    	
    	    	    
    	// add inverse map:
    	Map<String,String> result_inv = new HashMap<>();
    	for(String ki:result.keySet()) {
    		System.out.println("ColNameMap: "+ ki +"<->" + result.get(ki) );
    		result_inv.put(result.get(ki),ki);
    	}
    	
    	if(result_inv.size() != result.size() ) {
    		throw new Error("Created ambiguous special column names..");
    	}
    	
    	return new Map[] { result , result_inv };
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    	
        this.m_settings.setInputFile(inputFilePanel.getSelectedFileURLorPath());    	
    	
        String inputFileString = this.m_settings.getInputFile();
        File inputFile = null;
        inputFile = resolveFilePathOrKnimeURL(inputFileString);
        
        
        System.out.println("::saveSettingsTo -> "+inputFileString);
        
        if(!inputFile.exists()) {        	
        	this.m_settings.setInputFile(inputFileString);
        	throw new InvalidSettingsException("File not found! Please specify an existing file!");
        }
        else {	               
	        specialColumnsInfo = StructureFileHelpers.extractStructuresWithAdditionalInformation(inputFile);        
	        Map<String,String> specialColumnNames_array[] = createSpecialColumnNames(specialColumnsInfo);
	        Map<String,String> specialColumnNames      = specialColumnNames_array[0];
	        Map<String,String> specialColumnNames_inv  = specialColumnNames_array[1];
	        
            this.m_settings.setSelectedColumns(columnSelectionPane.getSelectedItems());        
            this.m_settings.setSelectedColumnsSpecial(columnSelectionPaneSpecial.getSelectedItems(),specialColumnNames_inv);
        }

        this.m_settings.saveSettings(settings);
    }

    private MultiSelectionPane createColumnSelectionPane() {
        return new MultiSelectionPane();
    }

    private JPanel createInputFilePanel() {
        JPanel p = new JPanel();
        
        double pref = TableLayout.PREFERRED;
        double fill = TableLayout.FILL;
        
        double[][] sizes = {
                {4, pref, 4, fill, 4}, {4, pref,4 , pref, 4, fill, 4 ,pref,4,fill,4}
        };
        TableLayout layout = new TableLayout(sizes);

        inputFilePanel = createOutputFilePane();        
        
        columnSelectionPane = createColumnSelectionPane();
        columnSelectionPaneSpecial = createColumnSelectionPane();
        
        p.setLayout(layout);

        int row = 1;

        p.add(new JLabel("Input file"), "1," + row);        
        p.add(inputFilePanel.getContentPane(), "3, " + row);
        row += 2;
        p.add(new JLabel("Data columns"), "1," + row);
        row += 2;
        p.add(columnSelectionPane.getContentPane(), "1," + row + ", 3, " + row);
        row += 2;
        p.add(new JLabel("Special columns"), "1," + row);
        row += 2;
        p.add(columnSelectionPaneSpecial.getContentPane(), "1," + row + ", 3, " + row);
        
        inputFilePanel.addListener(this);

        return p;
    }

    private FileChooserPane createOutputFilePane() {
        return new FileChooserPane(FileChooserPane.Mode.OPEN, "Compound file", new String[]{"dwar", "sdf"});
    }


}
