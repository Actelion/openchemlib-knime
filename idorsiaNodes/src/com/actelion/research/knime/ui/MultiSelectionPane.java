/*
 * @(#)MultiSelectionPane.java   16/01/04
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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

public class MultiSelectionPane implements ActionListener {
    private JPanel                     contentPane    = new JPanel();
    private List<String>               availableItems = new ArrayList<String>();
    private List<String>               selectedItems  = new ArrayList<String>();
    private JButton                    addAllButton;
    private JButton                    addButton;
    private JList<String>              availableList;
    private ListBasedListModel<String> availableListModel;
    private JButton                    removeAllButton;
    private JButton                    removeButton;
    private JList<String>              selectedList;
    private ListBasedListModel<String> selectedListModel;

    //~--- constructors -------------------------------------------------------

    public MultiSelectionPane() {
        iniUI();
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            List<String> selectedValuesList = availableList.getSelectedValuesList();

            selectedListModel.addAll(selectedValuesList);
            availableListModel.removeAll(selectedValuesList);
        } else if (e.getSource() == addAllButton) {
            selectedListModel.addAll(availableItems);
            availableListModel.removeAll();
        } else if (e.getSource() == removeButton) {
            List<String> selectedValuesList = selectedList.getSelectedValuesList();

            availableListModel.addAll(selectedValuesList);
            selectedListModel.removeAll(selectedValuesList);
        } else if (e.getSource() == removeAllButton) {
            availableListModel.addAll(selectedItems);
            selectedListModel.removeAll();
        }
    }

    //~--- get methods --------------------------------------------------------

    public JPanel getContentPane() {
        return contentPane;
    }

    public String[] getSelectedItems() {
        return selectedItems.toArray(new String[selectedItems.size()]);
    }

    //~--- set methods --------------------------------------------------------

    public void setSelection(List<String> selectedColumns, List<String> allColumns) {
        List<String> currentSelection = new ArrayList<String>(selectedColumns);

        currentSelection.retainAll(allColumns);
        selectedListModel.setValues(currentSelection);

        List<String> availableColumns = new ArrayList<String>(allColumns);

        availableColumns.removeAll(currentSelection);
        availableListModel.setValues(availableColumns);
    }

    //~--- methods ------------------------------------------------------------

    private void iniUI() {
        selectedListModel  = new ListBasedListModel<String>(selectedItems);
        availableListModel = new ListBasedListModel<String>(availableItems);
        selectedList       = new DragAndDropJList<String>(selectedListModel);
        selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selectedList.setBorder(BorderFactory.createTitledBorder("Included columns"));
        availableList = new JList<String>(availableListModel);
        availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availableList.setBorder(BorderFactory.createTitledBorder("Excluded columns"));
        addButton       = new JButton("Add >");
        removeButton    = new JButton("< Remove");
        addAllButton    = new JButton("Add all >>");
        removeAllButton = new JButton("<< Remove all");
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        addAllButton.addActionListener(this);
        removeAllButton.addActionListener(this);

        double[][]  sizes  = new double[][] {
            { TableLayout.FILL, 4, TableLayout.PREFERRED, 4, TableLayout.FILL }, {
                TableLayout.FILL, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.FILL
            }
        };
        TableLayout layout = new TableLayout(sizes);

        contentPane.setLayout(layout);
        contentPane.add(availableList, "0,0,0,6");
        contentPane.add(addAllButton, "2,0,f,b");
        contentPane.add(addButton, "2,2");
        contentPane.add(removeButton, "2,4");
        contentPane.add(removeAllButton, "2,6,f,t");
        contentPane.add(selectedList, "4,0,4,6");
    }

    //~--- inner classes ------------------------------------------------------

    private class DragAndDropJList<T> extends JList<T> {
        public DragAndDropJList(ListModel<T> dataModel) {
            super(dataModel);
            MouseAdapter listener = new ReorderListener(this);
            this.addMouseListener(listener);
            this.addMouseMotionListener(listener);

        }
    }

    class ReorderListener extends MouseAdapter {

        private JList list;
        private int pressIndex = 0;
        private int releaseIndex = 0;

        public ReorderListener(JList list) {
            if (!(list.getModel() instanceof DefaultListModel)) {
                throw new IllegalArgumentException("List must have a DefaultListModel");
            }
            this.list = list;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            pressIndex = list.locationToIndex(e.getPoint());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            releaseIndex = list.locationToIndex(e.getPoint());
            if (releaseIndex != pressIndex && releaseIndex != -1) {
                reorder();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseReleased(e);
            pressIndex = releaseIndex;
        }

        private void reorder() {
            DefaultListModel model = (DefaultListModel) list.getModel();
            Object dragee = model.elementAt(pressIndex);
            model.removeElementAt(pressIndex);
            model.insertElementAt(dragee, releaseIndex);
        }
    }

}
