package com.actelion.research.knime.nodes;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

public abstract class DescriptorDependentNodeDialog extends OCLNodeDialogPane {

    private JComboBox<String> moleculeComboBox;
    private JComboBox<DescriptorInfo> descriptorComboBox;
    private Map<String, List<DataColumnSpec>> moleculeColumnToDescriptorSpecMap = new HashMap<>();


    protected void init(JComboBox<String> moleculeComboBox, JComboBox<DescriptorInfo> descriptorComboBox) {
        this.moleculeComboBox = moleculeComboBox;
        this.descriptorComboBox = descriptorComboBox;
        moleculeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateDescriptors();
            }
        });
    }

    protected void fillMoleculeToDescriptorMap(DataTableSpec tableSpecs, List<DataColumnSpec> moleculeColumnSpecs, DescriptorInfo[] allowedDescriptors) {
        for (DataColumnSpec moleculeColumnSpec : moleculeColumnSpecs) {
            String moleculeColumnName = moleculeColumnSpec.getName();
            List<DataColumnSpec> descriptorColumnSpecs = SpecHelper.getDescriptorColumnSpecs(tableSpecs, moleculeColumnSpec);
            List<DataColumnSpec> allowed = new ArrayList<>();
            for (DataColumnSpec descriptorColumnSpec : descriptorColumnSpecs) {
                DescriptorInfo descriptorInfo = SpecHelper.getDescriptorInfo(descriptorColumnSpec);
                if (contains(allowedDescriptors, descriptorInfo)) {
                    allowed.add(descriptorColumnSpec);
                }
            }
            moleculeColumnToDescriptorSpecMap.put(moleculeColumnName, allowed);
        }
    }

    protected void populateMoleculeInputBox(DataTableSpec inputSpecsOne, DescriptorInfo[] allowedDescriptors) {
        moleculeComboBox.removeAllItems();
        List<DataColumnSpec> moleculeColumnSpecs = SpecHelper.getMoleculeColumnSpecs(inputSpecsOne);
        fillMoleculeToDescriptorMap(inputSpecsOne, moleculeColumnSpecs, allowedDescriptors);

        for (DataColumnSpec moleculeColumnSpec : moleculeColumnSpecs) {
            String moleculeColumnName = moleculeColumnSpec.getName();
            moleculeComboBox.addItem(moleculeColumnName);
        }
    }

    protected void populateMoleculeInputBox(DataTableSpec inputSpecsOne) {
        populateMoleculeInputBox(inputSpecsOne, DescriptorHelpers.getDescriptorInfos());
    }

    private boolean contains(DescriptorInfo[] descriptorInfos, DescriptorInfo descriptorInfo) {
        for (DescriptorInfo info : descriptorInfos) {
            if (info == descriptorInfo) {
                return true;
            }
        }
        return false;
    }

    private void updateDescriptors() {
        String moleculeColumn = (String) moleculeComboBox.getSelectedItem();
        List<DataColumnSpec> descriptorColumnSpecs = moleculeColumnToDescriptorSpecMap.get(moleculeColumn);
        descriptorComboBox.setSelectedItem(null);
        descriptorComboBox.removeAllItems();
        if (descriptorColumnSpecs != null) {
            for (DataColumnSpec descriptorColumnSpec : descriptorColumnSpecs) {
                DescriptorInfo descriptorInfo = SpecHelper.getDescriptorInfo(descriptorColumnSpec);
                descriptorComboBox.addItem(descriptorInfo);
            }
        }
        if(descriptorColumnSpecs.isEmpty()) {
        	JOptionPane.showMessageDialog( null, "Please first use the Calculate Descriptor Node to create descriptors!");
        }
    }
}
