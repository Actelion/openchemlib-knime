package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.reaction.Reaction;
import com.actelion.research.gui.JDrawPanel;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.chem.io.RXNFileParser;
import com.actelion.research.gui.CompoundCollectionPane;
import com.actelion.research.gui.FileHelper;
import com.actelion.research.gui.clipboard.ClipboardHandler;
import com.actelion.research.knime.ui.ClearableComboBox;
import com.actelion.research.knime.ui.DataColumnSpecListCellRenderer;
import info.clearthought.layout.TableLayout;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

public class OCLCombinatorialLibraryNodeDialog extends NodeDialogPane {
    private static final String DEFINE_REACTANTS_TAB = "Define reactants settings";
    private static final String REACTION_TAB = "Reaction settings";
    private static final String TACTIC_ALL = "all";
    private static final String TACTIC_ONE_OF = "one of";

    //~--- fields -------------------------------------------------------------

    private final OCLCombinatorialLibraryNodeSettings m_settings = new OCLCombinatorialLibraryNodeSettings();
    private ActionListener reactionButtonListener = new ReactionButtonListener();
    private Map<String, CompoundCollectionPane> reactantPanelMap = new HashMap<String, CompoundCollectionPane>();

    // private ActionListener                            reactantButtonListener = new ReactantButtonListener();
    private JDrawPanel mDrawPanel;

    // private CompoundCollectionPane[]                  mReactantPane;
    private JButton openReactionButton;
    private JPanel reactantsPane;
    private JButton saveReactionButton;
    private JComboBox<String> tacticComboBox;
    private JPanel tacticPane;
    private JComboBox[] reactantsComboBox;
    private ClearableComboBox[] reactantsNameComboBox;

    //~--- constructors -------------------------------------------------------

    public OCLCombinatorialLibraryNodeDialog() {
        this.addTab(REACTION_TAB, this.createReactionTab());

//      this.addTab(DEFINE_REACTANTS_TAB, this.createReactantsTab());
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
        this.m_settings.loadSettingsForDialog(settings);
        Reaction reaction = m_settings.getReaction();
        if (reaction.getMolecules() != 0) {
            mDrawPanel.getDrawArea().setReaction(reaction);
        }
        tacticComboBox.setSelectedItem(this.m_settings.isGenerateAll()
                ? TACTIC_ALL
                : TACTIC_ONE_OF);

        for (int i = 0; i < OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS; i++) {
            reactantsComboBox[i].removeAllItems();
            reactantsNameComboBox[i].removeAllItems();
            DataTableSpec inputSpecs = specs[i];

            reactantsComboBox[i].setEnabled(inputSpecs != null);
            reactantsNameComboBox[i].setEnabled(inputSpecs != null);

            String reactantColumnName = m_settings.getReactantColumnName(i);
            String reactantNameColumnName = m_settings.getReactantNameColumnName(i);
            DataColumnSpec selectedReactantColumnName = null;
            DataColumnSpec selectedReactantNameColumnName = null;

            if (inputSpecs != null) {
                for (String columnName : inputSpecs.getColumnNames()) {
                    DataColumnSpec columnSpec = inputSpecs.getColumnSpec(columnName);

                    if (columnSpec.getType().isCompatible(OCLMoleculeDataValue.class)
                            || columnSpec.getType().isAdaptable(OCLMoleculeDataValue.class)) {
                        reactantsComboBox[i].addItem(columnSpec);

                        if (columnSpec.getName().equals(reactantColumnName)) {
                            selectedReactantColumnName = columnSpec;
                        }
                    }

                    if (columnSpec.getType().isCompatible(StringValue.class)
                            || columnSpec.getType().isAdaptable(StringValue.class)) {
                        reactantsNameComboBox[i].addItem(columnSpec);

                        if (columnSpec.getName().equals(reactantNameColumnName)) {
                            selectedReactantNameColumnName = columnSpec;
                        }
                    }
                }
            }

            reactantsComboBox[i].setSelectedItem(selectedReactantColumnName);
            reactantsNameComboBox[i].setSelectedItem(selectedReactantNameColumnName);
        }
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
        this.m_settings.setReaction(getReaction());

        for (int i = 0; i < OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS; i++) {
            DataColumnSpec selectedColumnSpec = (DataColumnSpec) reactantsComboBox[i].getSelectedItem();
            DataColumnSpec selectedNameColumnSpec = (DataColumnSpec) reactantsNameComboBox[i].getSelectedItem();

            this.m_settings.setReactantColumnName(i,
                    (selectedColumnSpec == null)
                            ? ""
                            : selectedColumnSpec.getName());

            this.m_settings.setReactantNameColumnName(i,
                    (selectedNameColumnSpec == null)
                            ? ""
                            : selectedNameColumnSpec.getName());
        }

        this.m_settings.setGenerateAll(tacticComboBox.getSelectedItem().equals(TACTIC_ALL));
        this.m_settings.saveSettings(settings);
    }

    private void createReactantsPane() {
        reactantsComboBox = new JComboBox[OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS];
        reactantsNameComboBox = new ClearableComboBox[OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS];

        double[][] sizes = new double[2][];

        sizes[0] = new double[]{4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4};
        sizes[1] = new double[2 + OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS * 4];
        sizes[1][0] = 4;
        sizes[1][sizes[1].length - 1] = 4;

        for (int i = 0; i < OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS; i++) {
            sizes[1][4 * i + 1] = TableLayout.PREFERRED;
            sizes[1][4 * i + 2] = 4;
            sizes[1][4 * i + 3] = TableLayout.PREFERRED;
            sizes[1][4 * i + 4] = 12;
        }

        TableLayout tableLayout = new TableLayout(sizes);

        reactantsPane = new JPanel();
        reactantsPane.setLayout(tableLayout);
        reactantsPane.setBorder(BorderFactory.createTitledBorder("Reactants"));

        int row = 1;

        for (int i = 0; i < OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS; i++) {
            reactantsComboBox[i] = new JComboBox<DataColumnSpec>();
            reactantsComboBox[i].setPreferredSize(new Dimension(200, (int) reactantsComboBox[i].getPreferredSize().getHeight()));
            reactantsComboBox[i].setRenderer(new DataColumnSpecListCellRenderer());

            reactantsNameComboBox[i] = new ClearableComboBox<DataColumnSpec>();
            reactantsNameComboBox[i].setPreferredSize(new Dimension(200, (int) reactantsNameComboBox[i].getPreferredSize().getHeight()));
            reactantsNameComboBox[i].setRenderer(new DataColumnSpecListCellRenderer("Generate automatically"));


            reactantsPane.add(new JLabel("Reactant " + (char) (65 + i)), "1," + row + ", 7, " + row);
            row += 2;
            reactantsPane.add(new JLabel("Molecule"), "1," + row);
            reactantsPane.add(reactantsComboBox[i], "3, " + row);
            reactantsPane.add(new JLabel("Name "), "5," + row);
            reactantsPane.add(reactantsNameComboBox[i], "7, " + row);
            row += 2;
        }
    }

    private JPanel createReactionTab() {
        JPanel p = new JPanel();
        double[][] sizes = {
                {4, TableLayout.FILL, 4, TableLayout.PREFERRED, 4}, {
                4, TableLayout.PREFERRED, 4, TableLayout.FILL, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED,
                4
        }
        };
        TableLayout layout = new TableLayout(sizes);

        p.setLayout(layout);

        int row = 1;
        StereoMolecule fragment = new StereoMolecule();

        fragment.setFragment(true);
        mDrawPanel = new JDrawPanel(fragment, true);
        mDrawPanel.getDrawArea().setClipboardHandler(new ClipboardHandler());
        openReactionButton = new JButton("Open reaction...");
        openReactionButton.addActionListener(reactionButtonListener);
        saveReactionButton = new JButton("Save reaction...");
        saveReactionButton.addActionListener(reactionButtonListener);
        createTacticPane();
        createReactantsPane();
        p.add(new JLabel("Generic Reaction"), "1," + row + ",3," + row);
        row += 2;
        p.add(mDrawPanel, "1," + row + ",3," + row);
        row += 2;
        p.add(openReactionButton, "1," + row + ",r,c");
        p.add(saveReactionButton, "3," + row);
        row += 2;
        p.add(tacticPane, "1," + row + ",3," + row);
        row += 2;
        p.add(reactantsPane, "1," + row + ",3," + row);

//      mDrawPanel.getDrawArea().addDrawAreaListener(new DrawAreaListener() {
//                  @Override
//                  public void contentChanged(DrawAreaEvent drawAreaEvent) {
//
//      //              if (drawAreaEvent.getType() == DrawAreaEvent.TYPE_MOLECULE_CHANGED) {
////                      updateReactantsPane();
//      //              }
//                  }
//              });
        return p;
    }

    private void createTacticPane() {
        double[][] sizes = new double[][]{
                {TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 4, TableLayout.PREFERRED}, {TableLayout.PREFERRED}
        };
        TableLayout tableLayout = new TableLayout(sizes);

        tacticPane = new JPanel();
        tacticPane.setLayout(tableLayout);
        tacticComboBox = new JComboBox<String>(new String[]{TACTIC_ONE_OF, TACTIC_ALL});
        tacticPane.add(new JLabel("Generate"), "0, 0");
        tacticPane.add(tacticComboBox, "2, 0");
        tacticPane.add(new JLabel("multiple possible products"), "4, 0");
    }

    //~--- get methods --------------------------------------------------------

    // private void updateParentUI(Container container) {
//  if (container == null) {
//      return;
//  }
//
//  if (container.getParent() instanceof JComponent) {
//      ((JComponent) container.getParent()).revalidate();
//      ((JComponent) container.getParent()).updateUI();
//  } else {
//      updateParentUI(container.getParent());
//  }
//  }
//  private void updateReactantsPane() {
//  reactantsPane.removeAll();
//
//  double[][] sizes = new double[2][];
//
//  sizes[0] = new double[] { 4, 500, 4 };
//
//  Reaction reaction        = getReaction();
//  int      reactants       = MAX_REACTANTS;
//  int      actualReactants = reaction.getReactants();
//
//  mReactantPane                 = new CompoundCollectionPane[reactants];
//  sizes[1]                      = new double[1 + reactants * 2 + 2];
//  sizes[1][0]                   = 4;
//  sizes[1][sizes[1].length - 2] = TableLayout.FILL;
//  sizes[1][sizes[1].length - 1] = 4;
//
//  for (int reactantIdx = 0, idx = 1; reactantIdx < reactants; reactantIdx++) {
//      sizes[1][idx++] = 175;
//      sizes[1][idx++] = 4;
//  }
//
//  TableLayout layout = new TableLayout(sizes);
//
//  reactantsPane.setLayout(layout);
//
//  int row = 1;
//
//  for (int reactantIdx = 0; reactantIdx < reactants; reactantIdx++) {
//      JPanel prw = new JPanel();
//
//      prw.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
//      prw.setLayout(new BorderLayout());
//
//      JStructureView sview = new JStructureView(reaction.getReactant(reactantIdx), DnDConstants.ACTION_COPY_OR_MOVE,
//                                 DnDConstants.ACTION_NONE);
//
//      sview.setClipboardHandler(new ClipboardHandler());
//      prw.add(sview, BorderLayout.CENTER);
//
//      JPanel prwb = new JPanel();
//
//      prwb.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
//
//      JButton bload = new JButton("Open File...");
//
//      bload.setActionCommand("open" + reactantIdx);
//      bload.addActionListener(reactantButtonListener);
//      prwb.add(bload);
//      prw.add(prwb, BorderLayout.SOUTH);
//
//      JPanel pr = new JPanel();
//
//      pr.setLayout(new BorderLayout());
//      pr.add(prw, BorderLayout.WEST);
//
//      String                 reactantIdCode       = reaction.getReactant(reactantIdx).getIDCode();
//      CompoundCollectionPane existingReactantPane = reactantPanelMap.get(reactantIdCode);
//
//      if (existingReactantPane != null) {
//          mReactantPane[reactantIdx] = existingReactantPane;
//      } else {
//          mReactantPane[reactantIdx] = new CompoundCollectionPane<StereoMolecule>(new DefaultCompoundCollectionModel.Molecule(),
//                  false);
//          mReactantPane[reactantIdx].setFileSupport(CompoundCollectionPane.FILE_SUPPORT_NONE);
//          mReactantPane[reactantIdx].setEditable(true);
//          reactantPanelMap.put(reactantIdCode, mReactantPane[reactantIdx]);
//      }
//
////    mReactantPane[reactantIdx].setEnabled(reactantIdx < actualReactants);
//
//      pr.add(mReactantPane[reactantIdx], BorderLayout.CENTER);
//      reactantsPane.add(pr, "1," + row);
//      row += 2;
//  }
//
//  reactantsPane.add(tacticPane, "1," + row + ",l,b");
//  reactantsPane.repaint();
//  reactantsPane.updateUI();
//  reactantsPane.revalidate();
//  updateParentUI(reactantsPane);
//  }
    private Reaction getReaction() {
        try {
            return mDrawPanel.getDrawArea().getReaction();
        } catch (Exception e) {
            return new Reaction();
        }
    }

    private boolean isReactionValid(Reaction rxn) {
        try {
            if (rxn.getReactants() < 1) {
                throw new Exception("For combinatorial enumeration you need at least one reactant.");
            }

            if (rxn.getReactants() > 4) {
                throw new Exception("Combinatorial enumeration is limited to a maximum of 4 reactants.");
            }

            if (rxn.getProducts() == 0) {
                throw new Exception("No product defined.");
            }

            rxn.validateMapping();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);

            return false;
        }

        return true;
    }

    //~--- inner classes ------------------------------------------------------

    // private class ReactantButtonListener implements ActionListener {
//  @Override
//  public void actionPerformed(ActionEvent e) {
//      String actionCommand = e.getActionCommand();
//
////    if (actionCommand.equals("OK")) {
////        for (int i = 0; i < getReaction().getReactants(); i++) {
////            if (mReactantPane[i].getModel().getSize() == 0) {
////                JOptionPane.showMessageDialog(null, "Reactant(s) " + (i + 1) + " wasn't defined yet.");
////                return;
////            }
////        }
////    } else if (actionCommand.equals("Cancel")) {
////
////    } else
//      if (actionCommand.startsWith("open")) {
//          int                       reactant  = actionCommand.charAt(4) - '0';
//          ArrayList<StereoMolecule> compounds = new FileHelper(null).readStructuresFromFile(true);
//
//          if (compounds != null) {
//              SSSearcher searcher = new SSSearcher();
//
//              searcher.setFragment(getReaction().getReactant(reactant));
//
//              int matchErrors = 0;
//
//              for (int i = compounds.size() - 1; i >= 0; i--) {
//                  searcher.setMolecule(compounds.get(i));
//
//                  if (!searcher.isFragmentInMolecule()) {
//                      compounds.remove(i);
//                      matchErrors++;
//                  }
//              }
//
//              if (matchErrors != 0) {
//                  String message = (compounds.size() == 0)
//                                   ? "None of your file's compounds have generic reactant " + (char) ('A' + reactant)
//                                     + " as substructure.\n" + "Therefore no compound could be added to the reactant list."
//                                   : "" + matchErrors + " of your file's compounds don't contain generic reactant "
//                                     + (char) ('A' + reactant) + " as substructure.\n"
//                                     + "Therefore these compounds were not added to the reactant list.";
//
//                  JOptionPane.showMessageDialog(null, message);
//              }
//
//              if (compounds.size() == 0) {
//                  compounds = null;
//              }
//          }
//
////          if (compounds != null) {
////              if ((mReactantPane[reactant].getModel().getSize() != 0)
////                      && (0 == JOptionPane.showOptionDialog(null,
////                              "Do you want to add these compounds or to replace the current list?",
////                              "Add Or Replace Compounds",
////                              JOptionPane.DEFAULT_OPTION,
////                              JOptionPane.QUESTION_MESSAGE,
////                              null,
////                              new String[] { "Add", "Replace" },
////                              "Replace"))) {
////                  mReactantPane[reactant].getModel().addCompoundList(compounds);
////              } else {
////                  mReactantPane[reactant].getModel().setCompoundList(compounds);
////              }
////          }
//      }
//  }
//  }
    private class ReactionButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveReactionButton) {
                Reaction rxn = getReaction();

                if (isReactionValid(rxn)) {
                    new FileHelper(null).saveRXNFile(rxn);
                }
            } else if (e.getSource() == openReactionButton) {
                File rxnFile = FileHelper.getFile(null, "Please select a reaction file", FileHelper.cFileTypeRXN);

                if (rxnFile == null) {
                    return;
                }

                try {
                    Reaction reaction = new RXNFileParser().getReaction(rxnFile);

                    // allow for query features
                    for (int i = 0; i < reaction.getMolecules(); i++) {
                        reaction.getMolecule(i).setFragment(true);
                    }

                    mDrawPanel.getDrawArea().setReaction(reaction);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
