package com.actelion.research.knime.nodes.manipulation;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.nodes.DescriptorDependentNodeDialog;
import com.actelion.research.knime.nodes.calculation.OCLNewCalculateSimilarityListNodeSettings;
import com.actelion.research.knime.nodes.manipulation.DescriptorSettings.DescriptorSettingsGUI;
import com.actelion.research.knime.ui.DescriptorListCellRenderer;
import com.actelion.research.knime.utils.DescriptorHelpers;

import info.clearthought.layout.TableLayout;

/**
 * <code>NodeDialog</code> for the "OCLNewClusterMolecules" node.
 * 
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLNewClusterMoleculesNodeDialog extends DescriptorDependentNodeDialog {

	private final OCLNewClusterMoleculesNodeSettings m_settings = new OCLNewClusterMoleculesNodeSettings();
    private JTextField clusterColumnTextField;
    //private JComboBox<DescriptorInfo> descriptorComboBox;
    //private JComboBox<String> inputColumnComboBox;
    private DescriptorSettings.DescriptorSettingsGUI descriptorGUI;
    private JCheckBox maxClusterCheckBox;
    private JTextField maxClusterTextField;
    private JCheckBox maxSimilarityCheckBox;
    private JTextField maxSimilarityTextField;
    private JTextField representativeColumnTextField;

    //~--- constructors -------------------------------------------------------

    public OCLNewClusterMoleculesNodeDialog() {
        this.addTab("Cluster settings", this.createClusterTab());
        //init(inputColumnComboBox, descriptorComboBox);
        initInteractions();
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void initFromSettings(NodeSettingsRO settings, DataTableSpec[] specs, DataTableSpec[] specs_in) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        clusterColumnTextField.setText(this.m_settings.getClusterColumnName());
        representativeColumnTextField.setText(this.m_settings.getRepresentativeColumnName());
        maxClusterCheckBox.setSelected(this.m_settings.getClusterCutoff() > -1);
        maxClusterTextField.setText((this.m_settings.getClusterCutoff() > -1)
                ? this.m_settings.getClusterCutoff() + ""
                : "");
        maxSimilarityCheckBox.setSelected(this.m_settings.getSimilarityCutoff() > -1);
        maxSimilarityTextField.setText((this.m_settings.getSimilarityCutoff() > -1)
                ? this.m_settings.getSimilarityCutoff() + ""
                : "");

        // Input column
        String selectedInputColumn = this.m_settings.getInputColumnName();
        DataTableSpec inputSpecs = specs[0];

        //populateMoleculeInputBox(inputSpecs);
        //inputColumnComboBox.setSelectedItem(selectedInputColumn);
        //descriptorComboBox.setSelectedItem(this.m_settings.getDescriptor());
        descriptorGUI.initFromSettings(settings, specs_in[0]);

        updateMaxSimilarityTextField();
        updateMaxClusterTextField();
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setClusterColumnName(clusterColumnTextField.getText().trim());
        this.m_settings.setRepresentativeColumnName(representativeColumnTextField.getText().trim());
        this.m_settings.setClusterCutoff(getClusterCutoff());
        this.m_settings.setSimilarityCutoff(getSimilarityCutoff());
        this.m_settings.setDescriptorSettings(this.descriptorGUI.getDescriptorSettings());
        //this.descriptorGUI.getDescriptorSettings().saveSettings(OCLNewClusterMoleculesNodeSettings.DESCRIPTOR_ID_A, settings);
        //this.m_settings.setInputColumnName((String) inputColumnComboBox.getSelectedItem());
        //this.m_settings.setDescriptor((DescriptorInfo) descriptorComboBox.getSelectedItem());
        this.m_settings.saveSettings(settings);
    }

    private JPanel createClusterTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {
                        4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4, TableLayout.PREFERRED, 4
                }, {
                4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4,
                TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4
        }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;

        //inputColumnComboBox = new JComboBox<String>();

        Label headerLabel = new Label("Stop clustering when");

        maxClusterCheckBox = new JCheckBox("number of clusters reaches");
        maxClusterTextField = new JTextField(10);
        clusterColumnTextField = new JTextField(10);
        representativeColumnTextField = new JTextField(10);
        maxSimilarityCheckBox = new JCheckBox("highest similarity falls below");
        maxSimilarityTextField = new JTextField(10);
        
        descriptorGUI = new DescriptorSettingsGUI(OCLNewClusterMoleculesNodeSettings.DESCRIPTOR_ID_A); 

        JLabel descriptorLabel = new JLabel("Descriptor");

        //descriptorComboBox = new JComboBox<DescriptorInfo>();
        //descriptorComboBox.setRenderer(new DescriptorListCellRenderer());
        
        //JPanel jDescriptorPanel = new JPanel();
        //jDescriptorPanel.setLayout(new Borderlayout(9));
        
        
        p.add(new JLabel("Input column"), "1," + row);
        //p.add(inputColumnComboBox, "3," + row + ",5," + row);
        p.add(descriptorGUI, "3," + row + ",5," + row);
        row += 2;
        p.add(new JLabel("Cluster column name"), "1," + row);
        p.add(clusterColumnTextField, "3," + row + ",5," + row);
        row += 2;
        p.add(new JLabel("Representative column name"), "1," + row);
        p.add(representativeColumnTextField, "3," + row + ",5," + row);
        row += 2;
        p.add(headerLabel, "1," + row + ",5," + row);
        row += 2;
        p.add(maxClusterCheckBox, "1," + row + ",3," + row);
        p.add(maxClusterTextField, "5," + row);
        row += 2;
        p.add(maxSimilarityCheckBox, "1," + row + ",3," + row);
        p.add(maxSimilarityTextField, "5," + row);
        row += 2;
        p.add(descriptorLabel, "1," + row);
        //p.add(descriptorComboBox, "3," + row + ",5," + row);
        //populateCompoBox();
        
                

        return p;
    }

    private void initInteractions() {

//      maxSimilarityCheckBox.addChangeListener(new ChangeListener() {
//          @Override
//          public void stateChanged(ChangeEvent e) {
//              maxSimilarityTextField.setEnabled(maxSimilarityCheckBox.isSelected());
//              if(!maxSimilarityTextField.isEnabled()){
//                  maxSimilarityTextField.setText("");
//              }
//          }
//      });
        maxSimilarityCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMaxSimilarityTextField();
            }
        });
        maxClusterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMaxClusterTextField();
            }
        });
        updateMaxSimilarityTextField();
        updateMaxClusterTextField();
    }

//    private void populateCompoBox() {
//        for (DescriptorInfo descriptorInfo : DescriptorHelpers.getDescriptorInfos()) {
//            descriptorComboBox.addItem(descriptorInfo);
//        }        
//    }

    private void updateMaxClusterTextField() {
        maxClusterTextField.setEnabled(maxClusterCheckBox.isSelected());

        if (!maxClusterTextField.isEnabled()) {
            maxClusterTextField.setText("");
        }
    }

    private void updateMaxSimilarityTextField() {
        maxSimilarityTextField.setEnabled(maxSimilarityCheckBox.isSelected());

        if (!maxSimilarityTextField.isEnabled()) {
            maxSimilarityTextField.setText("");
        }
    }

    //~--- get methods --------------------------------------------------------

    private int getClusterCutoff() {
        if (!maxClusterCheckBox.isSelected()) {
            return -1;
        }

        try {
            return Integer.parseInt(maxClusterTextField.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private float getSimilarityCutoff() {
        if (!maxSimilarityCheckBox.isSelected()) {
            return -1;
        }

        try {
            return Float.parseFloat(maxSimilarityTextField.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

