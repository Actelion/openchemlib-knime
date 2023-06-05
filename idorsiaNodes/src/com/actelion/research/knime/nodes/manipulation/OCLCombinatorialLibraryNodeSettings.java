package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.io.RXNFileParser;
import com.actelion.research.chem.reaction.Reaction;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import com.actelion.research.knime.utils.MoleculeHelpers;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

//~--- JDK imports ------------------------------------------------------------

public class OCLCombinatorialLibraryNodeSettings extends AbstractOCLNodeSettings {

    private static final String REACTION = "reaction";
    private static final String REACTANTS = "reactants";
    private static final String PRODUCTS = "products";
    private static final String REACTANT_MOLECULES = "reactantMolecules";
    private static final String KEY_REACTANT = "Reactant";
    private static final String GENERATE_ALL = "generateAll";
    private static final String REACTANT_COLUMN_NAMES = "reactantColumnNames";
    private static final String REACTANT_NAMES_COLUMN_NAMES = "reactantNamesColumnNames";
    public static int MAX_REACTANTS = 3;
    private static final String[] DEFAULT_REACTANT_COLUMN_NAMES = new String[MAX_REACTANTS];
    private static final String[] DEFAULT_REACTANT_NAME_COLUMN_NAMES = new String[MAX_REACTANTS];
    private static final String[] DEFAULT_REACTANTS = new String[]{};
    private static final String[] DEFAULT_PRODUCTS = new String[]{};
    public static int MAX_PRODUCTS = 1;

    //~--- fields -------------------------------------------------------------

    private IDCodeParser idCodeParser = new IDCodeParser();
    private DataCell defaultValue = DataType.getMissingCell();
    private DataCell mapMissingTo = DataType.getMissingCell();
    private String[] reactantColumnName;
    private String[] reactantNameColumnName;

    // private StereoMolecule[][] reactantMolecules = new StereoMolecule[0][0];
    private boolean generateAll;
    private Reaction reaction;

    //~--- constructors -------------------------------------------------------

    public OCLCombinatorialLibraryNodeSettings() {
        this.reaction = new Reaction();
        this.reactantColumnName = DEFAULT_REACTANT_COLUMN_NAMES;
        this.reactantNameColumnName = DEFAULT_REACTANT_NAME_COLUMN_NAMES;
    }

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
        String[] reactantIdCodes = new String[reaction.getReactants()];
        String[] productIdCodes = new String[reaction.getProducts()];

        for (int reactantIdx = 0; reactantIdx < reaction.getReactants(); reactantIdx++) {
            StereoMolecule mol = reaction.getReactant(reactantIdx);

            reactantIdCodes[reactantIdx] = MoleculeHelpers.toReactionMoleculeString(mol);
        }

        for (int productIdx = 0; productIdx < reaction.getProducts(); productIdx++) {
            StereoMolecule mol = reaction.getProduct(productIdx);

            for (int i = 0; i < mol.getAtoms(); i++) {
                System.out.println("Reactant " + (productIdx + 1) + ", Atom map " + (i + 1) + ": " + mol.getAtomMapNo(i));
            }

            productIdCodes[productIdx] = MoleculeHelpers.toReactionMoleculeString(mol);
        }

        settings.addStringArray(REACTANTS, reactantIdCodes);
        settings.addStringArray(PRODUCTS, productIdCodes);
        settings.addStringArray(REACTANT_COLUMN_NAMES, reactantColumnName);
        settings.addStringArray(REACTANT_NAMES_COLUMN_NAMES, reactantNameColumnName);
        settings.addBoolean(GENERATE_ALL, generateAll);
    }

    //~--- get methods --------------------------------------------------------

    public String getReactantColumnName(int reactantIdx) {
        return reactantColumnName[reactantIdx];
    }

    public String getReactantNameColumnName(int reactantIdx) {
        return reactantNameColumnName[reactantIdx];
    }

    public Reaction getReaction() {
        return reaction;
    }

    public boolean isGenerateAll() {
        return generateAll;
    }

    //~--- set methods --------------------------------------------------------

    public void setGenerateAll(boolean generateAll) {
        this.generateAll = generateAll;
    }

    public void setReactantColumnName(int reactantIdx, String columnName) {
        reactantColumnName[reactantIdx] = columnName;
    }

    public void setReactantNameColumnName(int reactantIdx, String columnName) {
        reactantNameColumnName[reactantIdx] = columnName;
    }

    public void setReaction(Reaction reaction) {
        this.reaction = reaction;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {

        String rxnString = settings.getString(REACTION, "");
        // LEGACY: Backwards
        if (!rxnString.isEmpty()) {
            RXNFileParser rxnFileParser = new RXNFileParser();

            try {
                reaction = rxnFileParser.getReaction(rxnString);

                for (int i = 0; i < reaction.getMolecules(); i++) {
                    reaction.getMolecule(i).setFragment(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String[] reactantIdCodes = settings.getStringArray(REACTANTS, DEFAULT_REACTANTS);
            String[] productIdCodes = settings.getStringArray(PRODUCTS, DEFAULT_PRODUCTS);

            this.reaction = new Reaction();

            for (int reactantIdx = 0; reactantIdx < reactantIdCodes.length; reactantIdx++) {
                reaction.addReactant(MoleculeHelpers.fromReactionMoleculeString(reactantIdCodes[reactantIdx]));
            }

            for (int productIdx = 0; productIdx < productIdCodes.length; productIdx++) {
                reaction.addProduct(MoleculeHelpers.fromReactionMoleculeString(productIdCodes[productIdx]));
            }

            try {
                reaction.validateMapping();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.reactantColumnName = settings.getStringArray(REACTANT_COLUMN_NAMES, DEFAULT_REACTANT_COLUMN_NAMES);
        this.reactantNameColumnName = settings.getStringArray(REACTANT_NAMES_COLUMN_NAMES, DEFAULT_REACTANT_NAME_COLUMN_NAMES);
        this.generateAll = settings.getBoolean(GENERATE_ALL, false);
    }

    //~--- get methods --------------------------------------------------------


}
