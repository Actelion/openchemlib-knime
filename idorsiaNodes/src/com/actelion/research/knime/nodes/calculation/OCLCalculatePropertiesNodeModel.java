package com.actelion.research.knime.nodes.calculation;

import com.actelion.research.chem.AtomFunctionAnalyzer;
import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.MolecularFormula;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.NastyFunctionDetector;
import com.actelion.research.chem.RingCollection;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.conf.MolecularFlexibilityCalculator;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.prediction.CLogPPredictor;
import com.actelion.research.chem.prediction.DruglikenessPredictor;
import com.actelion.research.chem.prediction.DruglikenessPredictorWithIndex;
import com.actelion.research.chem.prediction.SolubilityPredictor;
import com.actelion.research.chem.prediction.TotalSurfaceAreaPredictor;
import com.actelion.research.chem.prediction.ToxicityPredictor;
import com.actelion.research.knime.data.OCLDescriptorDataValue;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.utils.DescriptorHelpers;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;
import com.actelion.research.util.DoubleFormat;

import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

//~--- JDK imports ------------------------------------------------------------

public class OCLCalculatePropertiesNodeModel extends NodeModel {
    public static final int IN_PORT = 0;
    static final String[] TAB_GROUP = {"Druglikeness", "LE & Tox", "Shape & Counts", "Functional Groups"};
    private static final String CHEMPROPERTY_LIST_SEPARATOR = "\t";
    private static final String CHEMPROPERTY_LIST_SEPARATOR_REGEX = "\\t";
    private static final String CHEMPROPERTY_OPTION_SEPARATOR = "|";
    private static final String PROPERTY_STRUCTURE_COLUMN = "structureColumn";
    private static final String PROPERTY_CHEMPROPERTY_LIST = "propertyList";
    private static final int PREDICTOR_COUNT = 8;
    private static final int PREDICTOR_LOGP = 0;
    private static final int PREDICTOR_LOGS = 1;    
    private static final int PREDICTOR_SURFACE = 3;
    private static final int PREDICTOR_DRUGLIKENESS = 4;
    private static final int PREDICTOR_TOXICITY = 5;
    private static final int PREDICTOR_NASTY_FUNCTIONS = 6;
    private static final int PREDICTOR_FLEXIBILITY = 7;
    private static final int PREDICTOR_FLAG_LOGP = (1 << PREDICTOR_LOGP);
    private static final int PREDICTOR_FLAG_LOGS = (1 << PREDICTOR_LOGS);    
    private static final int PREDICTOR_FLAG_SURFACE = (1 << PREDICTOR_SURFACE);
    private static final int PREDICTOR_FLAG_DRUGLIKENESS = (1 << PREDICTOR_DRUGLIKENESS);
    private static final int PREDICTOR_FLAG_TOXICITY = (1 << PREDICTOR_TOXICITY);
    private static final int PREDICTOR_FLAG_NASTY_FUNCTIONS = (1 << PREDICTOR_NASTY_FUNCTIONS);
    private static final int PREDICTOR_FLAG_FLEXIBILITY = (1 << PREDICTOR_FLEXIBILITY);
    private static final int PROPERTY_COUNT = 45;
    private static final int TOTAL_WEIGHT = 0;
    private static final int FRAGMENT_WEIGHT = 1;
    private static final int FRAGMENT_ABS_WEIGHT = 2;
    private static final int LOGP = 3;
    private static final int LOGS = 4;    
    private static final int ACCEPTORS = 8;
    private static final int DONORS = 9;
    private static final int SASA = 10;
    private static final int REL_PSA = 11;
    private static final int TPSA = 12;
    private static final int DRUGLIKENESS = 13;
    private static final int LE = 14;
    //	private static final int SE = ;
    private static final int LLE = 15;
    private static final int LELP = 16;
    private static final int MUTAGENIC = 17;
    private static final int TUMORIGENIC = 18;
    private static final int REPRODUCTIVE_EFECTIVE = 19;
    private static final int IRRITANT = 20;
    //private static final int NASTY_FUNCTIONS = 21;
    private static final int SHAPE = 22;
    private static final int FLEXIBILITY = 23;
    private static final int COMPLEXITY = 24;
    private static final int FRAGMENTS = 25;
    private static final int HEAVY_ATOMS = 26;
    private static final int NONCARBON_ATOMS = 27;
    private static final int METAL_ATOMS = 28;
    private static final int NEGATIVE_ATOMS = 29;
    private static final int STEREOCENTERS = 30;
    private static final int ROTATABLE_BONDS = 31;
    private static final int RING_CLOSURES = 32;
    private static final int SMALL_RINGS = 33;
    private static final int AROMATIC_RINGS = 34;
    private static final int AROMATIC_ATOMS = 35;
    private static final int SP3_ATOMS = 36;
    private static final int SYMMETRIC_ATOMS = 37;
    private static final int ALL_AMIDES = 38;
    private static final int ALL_AMINES = 39;
    private static final int ALKYL_AMINES = 40;
    private static final int ARYL_AMINES = 41;
    private static final int AROMATIC_NITROGEN = 42;
    private static final int BASIC_NITROGEN = 43;
    private static final int ACIDIC_OXYGEN = 44;
    private static final int CARBON_ATOMS = 1000;
    private static final int SP3_CARBON_ATOMS = 1001;
    private static final String[] PROPERTY_CODE = {"totalWeight", "fragmentWeight", "fragmentAbsWeight", "logP", "logS",
            "acceptors", "donors", "sasa", "rpsa", "tpsa", "druglikeness",
            "le", /*"se",*/ "lle", "lelp", "mutagenic", "tumorigenic", "reproEffective", "irritant", "nasty",
            "shape", "flexibility", "complexity", "fragments", "heavyAtoms", "carbonAtoms", "nonCHAtoms", "metalAtoms", "negAtoms",
            "stereoCenters", "rotBonds", "closures", "rings", "aromRings", "aromAtoms", "sp3Atoms", "sp3CarbonAtoms", "symmetricAtoms",
            "amides", "amines", "alkylAmines", "arylAmines", "aromN", "basicN", "acidicO"};
    static List<OCLProperty> propertyTable = new ArrayList<OCLProperty>();

    //~--- static initializers ------------------------------------------------

    static {
        addProperty(TOTAL_WEIGHT, 0, "Total Molweight", "Total molweight in g/mol; natural abundance", DoubleCell.TYPE);
        addProperty(FRAGMENT_WEIGHT, 0, "Molweight", "Molweight of largest fragment in g/mol; natural abundance", DoubleCell.TYPE);
        addProperty(FRAGMENT_ABS_WEIGHT, 0, "Absolute Weight", "Absolute weight of largest fragment in g/mol", DoubleCell.TYPE);
        addProperty(LOGP, 0, "cLogP", "cLogP; P: conc(octanol)/conc(water)", null, PREDICTOR_FLAG_LOGP, DoubleCell.TYPE);
        addProperty(LOGS, 0, "cLogS", "cLogS; S: water solubility in mol/l, pH=7.5, 25C", null, PREDICTOR_FLAG_LOGS, DoubleCell.TYPE);
        addProperty(ACCEPTORS, 0, "H-Acceptors", "H-Acceptors", IntCell.TYPE);
        addProperty(DONORS, 0, "H-Donors", "H-Donors", IntCell.TYPE);
        addProperty(SASA, 0, "Total Surface Area", "Total Surface Area (SAS Approximation, Van der Waals radii, 1.4Å probe)", null,
                PREDICTOR_FLAG_SURFACE, DoubleCell.TYPE);
        addProperty(REL_PSA, 0, "Relative PSA", "Relative Polar Surface Area (from polar and non-polar SAS Approximation)", null,
                PREDICTOR_FLAG_SURFACE, DoubleCell.TYPE);
        addProperty(TPSA, 0, "Polar Surface Area", "Polar Surface Area (P. Ertl approach)", null, PREDICTOR_FLAG_SURFACE, DoubleCell.TYPE);
        addProperty(DRUGLIKENESS, 0, "Druglikeness", "Druglikeness", DescriptorConstants.DESCRIPTOR_FFP512.name,
                PREDICTOR_FLAG_DRUGLIKENESS, DoubleCell.TYPE);
        addProperty(MUTAGENIC, 1, "Mutagenic", "Mutagenic", null, PREDICTOR_FLAG_TOXICITY, StringCell.TYPE);
        addProperty(TUMORIGENIC, 1, "Tumorigenic", "Tumorigenic", null, PREDICTOR_FLAG_TOXICITY, StringCell.TYPE);
        addProperty(REPRODUCTIVE_EFECTIVE, 1, "Reproductive Effective", "Reproductive Effective", null, PREDICTOR_FLAG_TOXICITY,
                StringCell.TYPE);
        addProperty(IRRITANT, 1, "Irritant", "Irritant", null, PREDICTOR_FLAG_TOXICITY, StringCell.TYPE);
        //addProperty(NASTY_FUNCTIONS, 1, "Nasty Functions", "Nasty Functions", DescriptorConstants.DESCRIPTOR_FFP512.name,
        //        PREDICTOR_FLAG_NASTY_FUNCTIONS, StringCell.TYPE);        
        addProperty(SHAPE, 2, "Shape Index", "Molecular Shape Index (spherical < 0.5 < linear)", DoubleCell.TYPE);
        addProperty(FLEXIBILITY, 2, "Molecular Flexibility", "Molecular Flexibility (low < 0.5 < high)", null, PREDICTOR_FLAG_FLEXIBILITY,
                DoubleCell.TYPE);
        addProperty(COMPLEXITY, 2, "Molecular Complexity", "Molecular Complexity (low < 0.5 < high)", DoubleCell.TYPE);
        addProperty(HEAVY_ATOMS, 2, "Non-H Atoms", "Non-Hydrogen Atom Count", IntCell.TYPE);
        addProperty(CARBON_ATOMS, 2, "C Atoms", "Carbon Atom Count", IntCell.TYPE);
        addProperty(NONCARBON_ATOMS, 2, "Non-C/H Atoms", "Non-Carbon/Hydrogen Atom Count", IntCell.TYPE);
        addProperty(METAL_ATOMS, 2, "Metal-Atoms", "Metal-Atom Count", IntCell.TYPE);
        addProperty(NEGATIVE_ATOMS, 2, "Electronegative Atoms", "Electronegative Atom Count (N, O, P, S, F, Cl, Br, I, As, Se)",
                IntCell.TYPE);
        addProperty(STEREOCENTERS, 2, "Stereo Centers", "Stereo Center Count", IntCell.TYPE);
        addProperty(ROTATABLE_BONDS, 2, "Rotatable Bonds", "Rotatable Bond Count", IntCell.TYPE);
        addProperty(RING_CLOSURES, 2, "Rings Closures", "Ring Closure Count", IntCell.TYPE);
        addProperty(SMALL_RINGS, 2, "Small Rings", "Small Ring Count (all rings up to 7 members)", IntCell.TYPE);
        addProperty(AROMATIC_RINGS, 2, "Aromatic Rings", "Aromatic Ring Count", IntCell.TYPE);
        addProperty(AROMATIC_ATOMS, 2, "Aromatic Atoms", "Aromatic Atom Count", IntCell.TYPE);
        addProperty(SP3_ATOMS, 2, "sp3-Atoms", "sp3-Atom Count", IntCell.TYPE);
        addProperty(SP3_CARBON_ATOMS, 2, "Carbon sp3-Atoms", "Carbon sp3-Atom Count", IntCell.TYPE);
        addProperty(SYMMETRIC_ATOMS, 2, "Symmetric atoms", "Symmetric Atom Count", IntCell.TYPE);
        addProperty(ALL_AMIDES, 3, "Amides", "Amide Nitrogen Count (includes imides and sulfonamides)", IntCell.TYPE);
        addProperty(ALL_AMINES, 3, "Amines", "Amine Count (excludes enamines, aminales, etc.)", IntCell.TYPE);
        addProperty(ALKYL_AMINES, 3, "Alkyl-Amines", "Alkyl-Amine Count (excludes Aryl-,Alkyl-Amines)", IntCell.TYPE);
        addProperty(ARYL_AMINES, 3, "Aromatic Amines", "Aryl-Amine Count (includes Aryl-,Alkyl-Amines)", IntCell.TYPE);
        addProperty(AROMATIC_NITROGEN, 3, "Aromatic Nitrogens", "Aromatic Nitrogen Atom Count", IntCell.TYPE);
        addProperty(BASIC_NITROGEN, 3, "Basic Nitrogens", "Basic Nitrogen Atom Count (rough estimate: pKa above 7)", IntCell.TYPE);
        addProperty(ACIDIC_OXYGEN, 3, "Acidic Oxygens", "Acidic Oxygen Atom Count (rough estimate: pKa below 7)", IntCell.TYPE);        

    }

    //~--- fields -------------------------------------------------------------

    private OCLCalculatePropertiesNodeSettings m_settings = new OCLCalculatePropertiesNodeSettings();
    private Object[] mPredictor;

    //~--- constructors -------------------------------------------------------

    // static final String[][] TAB_HEADER = {null, {null,
    // "Ki or IC50 in nmol/l"}, null, null};
    // private TreeMap<String, OCLProperty> propertyMap;
    // ~--- constructors -------------------------------------------------------
    public OCLCalculatePropertiesNodeModel() {
        super(1, 1);
        mPredictor = new Object[PREDICTOR_COUNT];

//      initSettingsModels();
    }

    //~--- methods ------------------------------------------------------------

    private static void addProperty(int type, int tab, String columnTitle, String description, DataType dataType) {
        addProperty(type, tab, columnTitle, description, null, 0, dataType);
    }

    private static void addProperty(int type, int tab, String columnTitle, String description, String descriptorName, int predictorFlags,
                                    DataType dataType) {
        OCLProperty property = new OCLProperty(type, tab, columnTitle, description, descriptorName, predictorFlags, dataType);

        propertyTable.add(property);

        // propertyMap.put(PROPERTY_CODE[type], property);
    }

    // ~--- methods ------------------------------------------------------------
    public double assessMolecularComplexity(StereoMolecule mol) {
        final int MAX_BOND_COUNT = 7;
        int bondCount = Math.min(mol.getBonds() / 2, MAX_BOND_COUNT);

        mol.ensureHelperArrays(Molecule.cHelperRings);

        StereoMolecule fragment = new StereoMolecule(mol.getAtoms(), mol.getBonds());
        TreeSet<String> fragmentSet = new TreeSet<String>();
        int[] atomMap = new int[mol.getAllAtoms()];
        boolean[][] bondsTouch = new boolean[mol.getBonds()][mol.getBonds()];

        for (int atom = 0; atom < mol.getAtoms(); atom++) {
            for (int i = 1; i < mol.getConnAtoms(atom); i++) {
                for (int j = 0; j < i; j++) {
                    int bond1 = mol.getConnBond(atom, i);
                    int bond2 = mol.getConnBond(atom, j);

                    bondsTouch[bond1][bond2] = true;
                    bondsTouch[bond2][bond1] = true;
                }
            }
        }

        boolean[] bondIsMember = new boolean[mol.getBonds()];
        int maxLevel = bondCount - 2;
        int[] levelBond = new int[maxLevel + 1];

        for (int rootBond = 0; rootBond < mol.getBonds(); rootBond++) {
            bondIsMember[rootBond] = true;

            int level = 0;

            levelBond[0] = rootBond;

            while (true) {
                boolean levelBondFound = false;

                while (!levelBondFound && (levelBond[level] < mol.getBonds() - 1)) {
                    levelBond[level]++;

                    if (!bondIsMember[levelBond[level]]) {
                        for (int bond = rootBond; bond < mol.getBonds(); bond++) {
                            if (bondIsMember[bond] && bondsTouch[bond][levelBond[level]]) {
                                levelBondFound = true;

                                break;
                            }
                        }
                    }
                }

                if (levelBondFound) {
                    bondIsMember[levelBond[level]] = true;

                    if (level == maxLevel) {
                        mol.copyMoleculeByBonds(fragment, bondIsMember, true, atomMap);
                        fragmentSet.add(new Canonizer(fragment).getIDCode());
                        bondIsMember[levelBond[level]] = false;
                    } else {
                        level++;
                        levelBond[level] = rootBond;
                    }
                } else {
                    if (--level < 0) {
                        break;
                    }

                    bondIsMember[levelBond[level]] = false;
                }
            }
        }

        return Math.log(fragmentSet.size()) / bondCount;
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        boolean hasMoleculeColumn = false;
        boolean inputColumnNameFound = false;
        DataTableSpec inputSpec = inSpecs[IN_PORT];
        String inputColumnName = (m_settings == null)
                ? null
                : m_settings.getInputColumnName();

        for (int a = 0; a < inputSpec.getNumColumns(); a++) {
            DataColumnSpec columnSpec = inputSpec.getColumnSpec(a);
            DataType dataType = columnSpec.getType();

            if (dataType.isCompatible(OCLMoleculeDataValue.class) || dataType.isCompatible(SdfValue.class)) {
                hasMoleculeColumn = true;
            }

            if ((inputColumnName != null) && inputColumnName.equals(columnSpec.getName())) {
                inputColumnNameFound = true;
            }
        }

        if (!hasMoleculeColumn) {
            throw new InvalidSettingsException("Input table must contain at "
                    + "least one column containing a suitable molecule representation");
        }

        if (!inputColumnNameFound) {
            throw new InvalidSettingsException("Input table does not contain a column named " + inputColumnName + ". Please "
                    + "(re-)configure the node.");
        }

        ColumnRearranger columnRearranger = createColumnRearranger(inputSpec);
        DataTableSpec result = columnRearranger.createSpec();

        return new DataTableSpec[]{result};
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        ColumnRearranger columnRearranger = createColumnRearranger(inData[IN_PORT].getDataTableSpec());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[IN_PORT], columnRearranger, exec);
        
        return new BufferedDataTable[]{out};
    }


    @Override
    protected void loadInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        m_settings.loadSettingsForModel(nodeSettingsRO);
    }

    @Override
    protected void reset() {
    }

    @Override
    protected void saveInternals(File file, ExecutionMonitor executionMonitor) throws IOException, CanceledExecutionException {
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO nodeSettingsWO) {
        m_settings.saveSettings(nodeSettingsWO);
    }

    @Override
    protected void validateSettings(NodeSettingsRO nodeSettingsRO) throws InvalidSettingsException {
        (new OCLCalculatePropertiesNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private double assessMolecularShape(StereoMolecule mol) {
        mol.ensureHelperArrays(Molecule.cHelperRings);

        if (mol.getAtoms() == 0) {
            return -1;
        }

        if (mol.getBonds() == 0) {
            return 0;
        }

        int maxLength = 0;

        for (int atom = 0; atom < mol.getAtoms(); atom++) {
            if ((mol.getConnAtoms(atom) == 1) || mol.isRingAtom(atom)) {
                maxLength = Math.max(maxLength, findHighestAtomDistance(mol, atom));
            }
        }

        return (double) (maxLength + 1) / (double) mol.getAtoms();
    }

    private ColumnRearranger createColumnRearranger(DataTableSpec in) {
        ColumnRearranger c = new ColumnRearranger(in);
        final int moleculeColumnIdx = SpecHelper.getColumnIndex(in, m_settings.getInputColumnName());
        final int pathFingerprintColumnIdx = SpecHelper.getDescriptorColumnIndex(in, moleculeColumnIdx, DescriptorConstants.DESCRIPTOR_PFP512);

        for (int a = 0; a < propertyTable.size(); a++) {
            OCLProperty oclProperty = propertyTable.get(a);
            boolean isSelected = this.m_settings.isCalculateAllProperties() ? true : this.m_settings.calculateProperty(a);

            if (isSelected) {
                ensurePredictor(oclProperty.predictorFlags);

                DataColumnSpec newSpec = createColumnSpec(in, oclProperty);
                CellFactory factory = new PropertyCellFactory(newSpec, moleculeColumnIdx, pathFingerprintColumnIdx, oclProperty);

                c.append(factory);
            }
        }

        return c;
    }

    private DataColumnSpec createColumnSpec(DataTableSpec in, OCLProperty oclProperty) {
        String columnName = DataTableSpec.getUniqueColumnName(in, oclProperty.columnTitle);

        return SpecHelper.createColumnSpec(columnName, oclProperty.dataType, null);
    }

    private void ensurePredictor(int predictorFlags) {
        for (int i = 0; i < PREDICTOR_COUNT; i++) {
            int flag = (1 << i);

            if ((predictorFlags & flag) != 0 && (mPredictor[i] == null)) {
                mPredictor[i] = (flag == PREDICTOR_FLAG_LOGP)
                        ? new CLogPPredictor()
                        : (flag == PREDICTOR_FLAG_LOGS)
                        ? new SolubilityPredictor()
                        : (flag == PREDICTOR_FLAG_SURFACE)
                        ? new TotalSurfaceAreaPredictor()
                        : (flag == PREDICTOR_FLAG_DRUGLIKENESS)
                        ? new DruglikenessPredictor() //new DruglikenessPredictorWithIndex()
                        : (flag == PREDICTOR_FLAG_TOXICITY)
                        ? new ToxicityPredictor()
                        : (flag == PREDICTOR_FLAG_NASTY_FUNCTIONS)
                        ? new NastyFunctionDetector()
                        : (flag == PREDICTOR_FLAG_FLEXIBILITY)
                        ? new MolecularFlexibilityCalculator()
                        : null;
            }
        }
    }

    private int findHighestAtomDistance(StereoMolecule mol, int startAtom) {
        int[] graphLevel = new int[mol.getAtoms()];
        int[] graphAtom = new int[mol.getAtoms()];

        graphAtom[0] = startAtom;
        graphLevel[startAtom] = 1;

        int current = 0;
        int highest = 0;

        while (current <= highest /* && graphLevel[current] <= maxLength */) {
            int parent = graphAtom[current];

            for (int i = 0; i < mol.getConnAtoms(parent); i++) {
                int candidate = mol.getConnAtom(parent, i);

                if (graphLevel[candidate] == 0) {
                    graphAtom[++highest] = candidate;
                    graphLevel[candidate] = graphLevel[parent] + 1;
                }
            }

            current++;
        }

        return graphLevel[graphAtom[highest]] - 1;
    }

    //~--- inner classes ------------------------------------------------------

    //  private void initSettingsModels() {
//      inputColumnModel = new SettingsModelColumnName(CFGKEY_INPUT_COLUMN, DEFAULT_INPUT_COLUMN);
//
//      for (int a = 0; a < propertyTable.size(); a++) {
//          settingsModels.add(new SettingsModelBoolean(PROPERTY_CODE[a], false));
//      }
//  }
    // ~--- inner classes ------------------------------------------------------
    // private DataColumnSpec createLargestFragmentColumnSpec() {
    // DataColumnSpecCreator colSpecCreator = new
    // DataColumnSpecCreator(outputColumnNameModel.getStringValue(),
    // OCLMoleculeDataCell.TYPE);
    // return colSpecCreator.createSpec();
    // }
    static class OCLProperty {
        public final String columnTitle;
        public final DataType dataType;
        public final String description;
        public final String descriptorName;

        // public final String dependentColumnFilter; // if not null, e.g.
        // 'ic50', serves as substring to prioritize numerical columns for
        // selection, lower case!!!
        public final int tab, type;
        private final int predictorFlags;

        //~--- constructors ---------------------------------------------------

        // ~--- constructors ---------------------------------------------------

        /**
         * @param columnTitle
         * @param description
         * @param descriptorName
         */
        public OCLProperty(int type, int tab, String columnTitle, String description, String descriptorName, int predictorFlags,
                           DataType dataType) {
            this.type = type;
            this.tab = tab;
            this.columnTitle = columnTitle;
            this.description = description;
            this.descriptorName = descriptorName;
            this.predictorFlags = predictorFlags;
            this.dataType = dataType;
        }
    }


    private class PropertyCellFactory extends SingleCellFactory {
        private final int pathFingerprintColumnIdx;
        private int moleculeColumnIdx;
        private OCLProperty property;

        //~--- constructors ---------------------------------------------------

        // ~--- constructors ---------------------------------------------------
        PropertyCellFactory(DataColumnSpec columnSpec, int moleculeColumnIdx, int pathFingerprintColumnIdx, OCLProperty property) {
            super(columnSpec);
            this.moleculeColumnIdx = moleculeColumnIdx;
            this.pathFingerprintColumnIdx = pathFingerprintColumnIdx;
            this.property = property;
        }

        //~--- get methods ----------------------------------------------------

        // ~--- get methods ----------------------------------------------------
        @Override
        public DataCell getCell(DataRow dataRow) {
            DataCell moleculeCell = dataRow.getCell(moleculeColumnIdx);

            if (moleculeCell.isMissing()) {
                return DataType.getMissingCell();
            }

            OCLMoleculeDataValue oclMolecule = (OCLMoleculeDataValue) moleculeCell;
            OCLDescriptorDataValue oclPathFingerPrint = pathFingerprintColumnIdx < 0 ? null : (OCLDescriptorDataValue) dataRow.getCell(pathFingerprintColumnIdx);
            Object calculatedValue = calculate(oclMolecule, oclPathFingerPrint);

            if (calculatedValue == null) {
                return DataType.getMissingCell();
            }

            return ValueHelper.createDataCell(calculatedValue);
        }

        //~--- methods --------------------------------------------------------

        // ~--- methods --------------------------------------------------------
        private Object calculate(OCLMoleculeDataValue moleculeCell, OCLDescriptorDataValue pathFingerprintCell) {
            StereoMolecule fullMolecule = moleculeCell.getMolecule();
            StereoMolecule mol = fullMolecule.getCompactCopy();

            mol.stripSmallFragments();

            if ((mol == null) || (mol.getAtoms() == 0)) {
                return null;
            }

            int count = 0;            
            double totalWeight = -1;
            if (mol.getAllAtoms() != 0) {
                totalWeight = new MolecularFormula(mol).getRelativeWeight();
                mol.stripSmallFragments();                
            }

            try {
                switch (this.property.type) {
                    case TOTAL_WEIGHT:
                        return Double.parseDouble(DoubleFormat.toString(totalWeight, 6));

                    case FRAGMENT_WEIGHT:
                        return Double.parseDouble(DoubleFormat.toString(new MolecularFormula(mol).getRelativeWeight(), 6));

                    case FRAGMENT_ABS_WEIGHT:
                        return Double.parseDouble(DoubleFormat.toString(new MolecularFormula(mol).getAbsoluteWeight(), 9));

                    case LOGP:
                        try {
                            return Double.parseDouble(DoubleFormat.toString(((CLogPPredictor) mPredictor[PREDICTOR_LOGP]).assessCLogP(mol)));
                        } catch (Exception e) {
                            return null;
                        }
                    case LOGS:
                        return Double.parseDouble(
                                DoubleFormat.toString(((SolubilityPredictor) mPredictor[PREDICTOR_LOGS]).assessSolubility(mol)));

                    case ACCEPTORS:
                        for (int atom = 0; atom < mol.getAllAtoms(); atom++) {
                            if ((mol.getAtomicNo(atom) == 7) || (mol.getAtomicNo(atom) == 8)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case DONORS:
                        for (int atom = 0; atom < mol.getAllAtoms(); atom++) {
                            if (((mol.getAtomicNo(atom) == 7) || (mol.getAtomicNo(atom) == 8)) && (mol.getAllHydrogens(atom) > 0)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case SASA:
                        return Double.parseDouble(
                                DoubleFormat.toString(((TotalSurfaceAreaPredictor) mPredictor[PREDICTOR_SURFACE]).assessTotalSurfaceArea(mol)));

                    case REL_PSA:
                        return Double.parseDouble(
                                DoubleFormat.toString(
                                        ((TotalSurfaceAreaPredictor) mPredictor[PREDICTOR_SURFACE]).assessRelativePolarSurfaceArea(mol)));

                    case TPSA:
                        return Double.parseDouble(
                                DoubleFormat.toString(((TotalSurfaceAreaPredictor) mPredictor[PREDICTOR_SURFACE]).assessPSA(mol)));

                    case DRUGLIKENESS: {
//                        int[] pathFingerprint = ensurePathFingerprint(pathFingerprintCell, mol);
//                        return Double.parseDouble(
//                                DoubleFormat.toString(((DruglikenessPredictorWithIndex) mPredictor[PREDICTOR_DRUGLIKENESS]).assessDruglikeness(mol, pathFingerprint,
//                                        null)));
                    	return Double.parseDouble(
                              DoubleFormat.toString(((DruglikenessPredictor) mPredictor[PREDICTOR_DRUGLIKENESS]).assessDruglikeness(mol, null)));                    	
                    	
                    }

                    case MUTAGENIC:
                        return ToxicityPredictor
                                .RISK_NAME[((ToxicityPredictor) mPredictor[PREDICTOR_TOXICITY]).assessRisk(mol, ToxicityPredictor.cRiskTypeMutagenic, null)];

                    case TUMORIGENIC:
                        return ToxicityPredictor
                                .RISK_NAME[((ToxicityPredictor) mPredictor[PREDICTOR_TOXICITY]).assessRisk(mol, ToxicityPredictor.cRiskTypeTumorigenic, null)];

                    case REPRODUCTIVE_EFECTIVE:
                        return ToxicityPredictor
                                .RISK_NAME[((ToxicityPredictor) mPredictor[PREDICTOR_TOXICITY]).assessRisk(mol, ToxicityPredictor.cRiskTypeReproductiveEffective, null)];

                    case IRRITANT:
                        return ToxicityPredictor
                                .RISK_NAME[((ToxicityPredictor) mPredictor[PREDICTOR_TOXICITY]).assessRisk(mol, ToxicityPredictor.cRiskTypeIrritant, null)];

                /*
                 * case HERG_RISK: value =
                 * ((RiskOf_hERGActPredictor)predictor[PREDICTOR_HERG
                 * ]).assess_hERGRisk(mol, mPro gressDialog); break;
                 */
//                    case NASTY_FUNCTIONS: {
//                        int[] pathFingerprint = ensurePathFingerprint(pathFingerprintCell, mol);
//                        return ((NastyFunctionDetector) mPredictor[PREDICTOR_NASTY_FUNCTIONS]).getNastyFunctionString(mol, pathFingerprint);
//                    }

                    case SHAPE:
                        return Double.parseDouble(DoubleFormat.toString(assessMolecularShape(mol)));

                    case FLEXIBILITY:
                        return Double.parseDouble(
                                DoubleFormat.toString(
                                        ((MolecularFlexibilityCalculator) mPredictor[PREDICTOR_FLEXIBILITY]).calculateMolecularFlexibility(mol)));

                    case COMPLEXITY:
                        return Double.parseDouble(DoubleFormat.toString(assessMolecularComplexity(mol)));

                    case HEAVY_ATOMS:
                        return new Integer(mol.getAtoms());

                    case NONCARBON_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperNeighbours);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (mol.getAtomicNo(atom) != 6) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case METAL_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperNeighbours);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (mol.isMetalAtom(atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case NEGATIVE_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperNeighbours);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (mol.isElectronegative(atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case STEREOCENTERS:
                        return new Integer(mol.getStereoCenterCount());

                    case ROTATABLE_BONDS:
                        return new Integer(mol.getRotatableBondCount());

                    case RING_CLOSURES:
                        int[] fNo = new int[mol.getAllAtoms()];
                        int fragments = mol.getFragmentNumbers(fNo, false, false);
                        return new Integer(fragments + (mol.getAllBonds() - mol.getAllAtoms()));

                    case SMALL_RINGS:
                        mol.ensureHelperArrays(Molecule.cHelperRings);
                        return new Integer(mol.getRingSet().getSize());
                    case AROMATIC_RINGS:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        RingCollection rc = mol.getRingSet();

                        for (int i = 0; i < rc.getSize(); i++) {
                            if (rc.isAromatic(i)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case AROMATIC_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (mol.isAromaticAtom(atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case SP3_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (((mol.getAtomicNo(atom) == 6) && (mol.getAtomPi(atom) == 0))
                                    || ((mol.getAtomicNo(atom) == 7) && !mol.isFlatNitrogen(atom))
                                    || ((mol.getAtomicNo(atom) == 8) && (mol.getAtomPi(atom) == 0) && !mol.isAromaticAtom(atom))
                                    || (mol.getAtomicNo(atom) == 15)
                                    || ((mol.getAtomicNo(atom) == 16) && !mol.isAromaticAtom(atom))) {
                                count++;
                            }
                        }

                        return new Integer(count);
                    case CARBON_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperNeighbours);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (mol.getAtomicNo(atom) == 6) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case SP3_CARBON_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (((mol.getAtomicNo(atom) == 6) && (mol.getAtomPi(atom) == 0))) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case SYMMETRIC_ATOMS:
                        mol.ensureHelperArrays(Molecule.cHelperSymmetrySimple);

                        int maxRank = 0;

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (maxRank < mol.getSymmetryRank(atom)) {
                                maxRank = mol.getSymmetryRank(atom);
                            }
                        }

                        return new Integer(mol.getAtoms() - maxRank);

                    case ALL_AMIDES:
                        mol.ensureHelperArrays(Molecule.cHelperNeighbours);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (AtomFunctionAnalyzer.isAmide(mol, atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case ALL_AMINES:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (AtomFunctionAnalyzer.isAmine(mol, atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case ALKYL_AMINES:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (AtomFunctionAnalyzer.isAlkylAmine(mol, atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case ARYL_AMINES:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (AtomFunctionAnalyzer.isArylAmine(mol, atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case AROMATIC_NITROGEN:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if ((mol.getAtomicNo(atom) == 7) && mol.isAromaticAtom(atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case BASIC_NITROGEN:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (AtomFunctionAnalyzer.isBasicNitrogen(mol, atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);

                    case ACIDIC_OXYGEN:
                        mol.ensureHelperArrays(Molecule.cHelperRings);

                        for (int atom = 0; atom < mol.getAtoms(); atom++) {
                            if (AtomFunctionAnalyzer.isAcidicOxygen(mol, atom)) {
                                count++;
                            }
                        }

                        return new Integer(count);                   
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return null;
        }

        private int[] ensurePathFingerprint(OCLDescriptorDataValue pathFingerprintCell, StereoMolecule mol) {
            int[] pathFingerprint;
            if (pathFingerprintCell != null) {
                pathFingerprint = (int[]) pathFingerprintCell.getDescriptor();
            } else {
                pathFingerprint = (int[]) DescriptorHelpers.calculateDescriptor(mol, DescriptorConstants.DESCRIPTOR_PFP512);
            }
            return pathFingerprint;
        }
    }
}
