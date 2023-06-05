package com.actelion.research.knime.ui;

import info.clearthought.layout.TableLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


public class ClearableComboBox<T> extends JPanel implements ActionListener {
    private JComboBox<T> comboBox;
    private JButton clearButton;


    public ClearableComboBox() {
        initUI();
    }

    private void initUI() {
        double[][] sizes = new double[][]{{TableLayout.FILL, 4, TableLayout.PREFERRED}, {TableLayout.PREFERRED}};
        TableLayout tableLayout = new TableLayout(sizes);
        setLayout(tableLayout);

        comboBox = new JComboBox<T>();
        clearButton = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("clear16.png"));
            clearButton.setIcon(new ImageIcon(img));
        } catch (IOException e) {
            e.printStackTrace();
        }


        clearButton.addActionListener(this);
        add(comboBox, "0,0");
        add(clearButton, "2,0");


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearButton) {
            comboBox.setSelectedItem(null);
        }
    }

    public void removeAllItems() {
        comboBox.removeAllItems();
    }

    public void addItem(T item) {
        comboBox.addItem(item);
    }


    public void setSelectedItem(T item) {
        comboBox.setSelectedItem(item);
    }

    public void setRenderer(ListCellRenderer<T> listCellRenderer) {
        comboBox.setRenderer(listCellRenderer);
    }

    public Object getSelectedItem() {
        return comboBox.getSelectedItem();
    }
}
