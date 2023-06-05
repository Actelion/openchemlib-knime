/*
 * @(#)FileChooserPane.java   16/01/07
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


package com.actelion.research.knime.ui;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.draw2d.FlowLayout;

//~--- JDK imports ------------------------------------------------------------

public class FileChooserPane implements ActionListener {
    private static final String LAST_DIRECTORY = "lastDirectory";

    //~--- fields -------------------------------------------------------------

    private JPanel contentPane = new JPanel();
    private List<FileChangeListener> fileChangeListeners = new ArrayList<FileChangeListener>();
    private FileNameExtensionFilter fileFilter;
    private JButton browseButton;
    private JButton setKnimeUrlButton;
    private String[] extensions;
    private String fileDescription;
    private JTextField fileTextField;
    private Mode mode;

    //~--- constant enums -----------------------------------------------------

    public FileChooserPane(Mode mode, String fileDescription, String[] extensions) {
        this.mode = mode;
        this.fileDescription = fileDescription;
        this.extensions = extensions;
        this.fileFilter = new FileNameExtensionFilter(fileDescription, extensions);
        initUI();
    }

    //~--- constructors -------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == browseButton) {
            Preferences preferences = Preferences.userNodeForPackage(FileChooserPane.class);
            String lastDirectory = preferences.get(LAST_DIRECTORY, System.getProperty("user.home"));
            JFileChooser fileChooser = new JFileChooser(lastDirectory);

            if (extensions != null) {
                fileChooser.setFileFilter(fileFilter);
            }

            if (mode == Mode.OPEN) {
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    if (fileFilter.accept(fileChooser.getSelectedFile())) {
                    	System.out.println("FileChooser : Mode.OPEN -> approve "+fileChooser.getSelectedFile());
                        setSelectedFile(fileChooser.getSelectedFile());
                        preferences.put(LAST_DIRECTORY, getSelectedFile().getPath());
                    }
                    else{
                    	System.out.println("FileChooser : file not approved.. :(");
                    }
                }
            } else if (mode == Mode.SAVE) {
                int result = fileChooser.showSaveDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    if (fileFilter.accept(fileChooser.getSelectedFile())) {
                    	System.out.println("FileChooser : Mode.SAVE -> approve "+fileChooser.getSelectedFile());
                        setSelectedFile(fileChooser.getSelectedFile());
                        preferences.put(LAST_DIRECTORY, getSelectedFile().getParentFile().toString());
                    }
                    else {
                    	System.out.println("FileChooser : file not approved.. :(");
                    }
                }
            }
        }
        else if(e.getSource()==setKnimeUrlButton) {
//        	JDialog dialog = new JDialog()
//        	dialog.setLayout(new java.awt.FlowLayout());
//        	dialog.getContentPane().add(new JLabel("URL: "));
//        	JTextField tf = new JTextField(36);
//        	dialog.getContentPane().add(tf);        	
        	String new_knime_url = (String) JOptionPane.showInputDialog(this.getContentPane(),"URL:","Set KNIME URL",JOptionPane.PLAIN_MESSAGE,null,null,"knime://");
        	if(new_knime_url!=null) {
        		if (mode == Mode.OPEN) {
        			setSelectedFile(new_knime_url);
        		}
        		else if(mode == Mode.SAVE) {
        			setSelectedFile(new_knime_url);
        		}
        	}
        }
    }

    //~--- methods ------------------------------------------------------------

    public void addListener(FileChangeListener fileChangeListener) {
        if (!fileChangeListeners.contains(fileChangeListener)) {
            fileChangeListeners.add(fileChangeListener);
        }
    }

    public void removeListener(FileChangeListener fileChangeListener) {
        fileChangeListeners.remove(fileChangeListener);
    }

    public void setFileFilter(String fileDescription, String[] extensions) {
        this.fileDescription = fileDescription;
        this.extensions = extensions;
        this.fileFilter = new FileNameExtensionFilter(fileDescription, extensions);
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    //~--- get methods --------------------------------------------------------

    public File getSelectedFile() {
        return new File(fileTextField.getText());
    }
    
    public String getSelectedFileString() {
    	return fileTextField.getText();
    }
    
    public String getSelectedFileURLorPath() {
    	return this.fileTextField.getText();
    }

    public void setSelectedFile(String selectedFile) {
    	if (selectedFile == null) {
            fileTextField.setText("");
        } else {
            fileTextField.setText(selectedFile);
        }
        notifyListeners(selectedFile);
    }
    
    public void setSelectedFile(File selectedFile) {
        if (selectedFile == null) {
            fileTextField.setText("");
        } else {
            fileTextField.setText(selectedFile.toString());
        }
        notifyListeners(selectedFile.toString());
    }

    //~--- set methods --------------------------------------------------------

    private void notifyListeners(String selectedFile) {
        for (FileChangeListener fileChangeListener : fileChangeListeners) {
            fileChangeListener.fileChanged(selectedFile);
        }
    }

    private void initUI() {
        double[][] sizes = {
                {TableLayout.FILL, 4, TableLayout.PREFERRED, 4 , TableLayout.PREFERRED}, {TableLayout.PREFERRED}
        };
        TableLayout layout = new TableLayout(sizes);

        contentPane.setLayout(layout);
        fileTextField = new JTextField(15);
        browseButton = new JButton("Browse..");
        browseButton.addActionListener(this);
        setKnimeUrlButton = new JButton("Set KNIME URL..");
        setKnimeUrlButton.addActionListener(this);
        
        contentPane.add(fileTextField, "0,0");
        contentPane.add(browseButton, "2,0");
        contentPane.add(setKnimeUrlButton, "4,0");
    }

    //~--- methods ------------------------------------------------------------

    public enum Mode {OPEN, SAVE;}

    //~--- inner interfaces ---------------------------------------------------

    public interface FileChangeListener {
        void fileChanged(String newFile);
    }
}
