package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.gui.JEditableStructureView;
import com.actelion.research.gui.clipboard.ClipboardHandler;
import info.clearthought.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import javax.swing.*;
import java.awt.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLSubstructureFilterNodeDialog extends NodeDialogPane {
    private final OCLSubstructureFilterNodeSettings m_settings = new OCLSubstructureFilterNodeSettings();
    private JComboBox<String>                       inputColumnComboBox;
    private JEditableStructureView mStructureView;

    //~--- constructors -------------------------------------------------------

    public OCLSubstructureFilterNodeDialog() {
        this.addTab("Write file settings", this.createSimilarityTab());
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        mStructureView.structureChanged(this.m_settings.getFragment());

        // Input column
        String        selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecs          = specs[0];

        inputColumnComboBox.removeAllItems();

        for (String columnName : inputSpecs.getColumnNames()) {
            DataColumnSpec columnSpec = inputSpecs.getColumnSpec(columnName);

            if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)) {
                inputColumnComboBox.addItem(columnName);
            }
        }

        inputColumnComboBox.setSelectedItem(selectedInputColumn);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setFragment(mStructureView.getMolecule());
        this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        this.m_settings.saveSettings(settings);
    }

    private JPanel createSimilarityTab() {
        JPanel      p      = new JPanel();
        double[][]  sizes  = {
            { 4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4 }, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 2, TableLayout.FILL, 4
            }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        inputColumnComboBox = new JComboBox<String>();
        p.add(new JLabel("Input column"), "1," + row);
        p.add(inputColumnComboBox, "3," + row);
        row += 2;

        StereoMolecule fragment = new StereoMolecule();
        fragment.setFragment(true);
        mStructureView = new JEditableStructureView(fragment);
        mStructureView.setClipboardHandler(new ClipboardHandler());
        mStructureView.setMinimumSize(new Dimension(100, 100));
        mStructureView.setPreferredSize(new Dimension(100, 100));
        mStructureView.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        mStructureView.setBackground(Color.white);
        p.add(new JLabel("Substructure"), "1," + row + ",3," + row);
        row += 2;
        p.add(mStructureView, "1," + row + ",3," + row);

        return p;
    }
}
