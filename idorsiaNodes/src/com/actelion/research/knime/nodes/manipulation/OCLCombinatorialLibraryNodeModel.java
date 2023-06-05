package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.reaction.Reaction;
import com.actelion.research.chem.reaction.Reactor;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.nodes.OCLNodeModel;
import com.actelion.research.knime.utils.ArbitraryNumber;
import com.actelion.research.knime.utils.SpecHelper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//~--- JDK imports ------------------------------------------------------------

public class OCLCombinatorialLibraryNodeModel extends OCLNodeModel {
    private static final int IN_PORT = 0;

    //~--- fields -------------------------------------------------------------

    private final OCLCombinatorialLibraryNodeSettings m_settings = new OCLCombinatorialLibraryNodeSettings();

    //~--- constructors -------------------------------------------------------

    public OCLCombinatorialLibraryNodeModel() {
        super(createOptionalPorts(3, 1, 2, 3), createOptionalPorts(1));
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        int connectedInputs = getConnectedInputs(inSpecs);
        Reaction reaction = this.m_settings.getReaction();

        if (reaction == null) {
            throw new InvalidSettingsException("No reaction defined");
        }

        int productCount = reaction.getProducts();
        int reactantCount = reaction.getReactants();

        if (reactantCount == 0) {
            throw new InvalidSettingsException("Provided reaction has no reactants");
        }

        if (reactantCount > OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS) {
            throw new InvalidSettingsException("Provided reaction has more than the allowed "
                    + OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS + " reactants");
        }


        for (int reactantIdx = 0; reactantIdx < reactantCount; reactantIdx++) {
            DataTableSpec inSpec = inSpecs[reactantIdx];
            int portIdx = reactantIdx + 1;
            if (inSpec == null) {
                throw new InvalidSettingsException("Input port " + portIdx + " is not connected, but needed as input for reactant " + portIdx);
            }
            String reactantColumnName = this.m_settings.getReactantColumnName(reactantIdx);
            int columnIndex = SpecHelper.getColumnIndex(inSpec, reactantColumnName);
            if (columnIndex < 0) {
                throw new InvalidSettingsException("No column from input port " + portIdx + " is selected as source for reactant " + portIdx);
            }
        }


        if (reactantCount != connectedInputs) {
            throw new InvalidSettingsException("Number of reactants in reaction and number of connected inputs does not match");
        }

        if (productCount == 0) {
            throw new InvalidSettingsException("Provided reaction has no product");
        }

        if (productCount > OCLCombinatorialLibraryNodeSettings.MAX_PRODUCTS) {
            throw new InvalidSettingsException("Provided reaction has more than " + OCLCombinatorialLibraryNodeSettings.MAX_PRODUCTS
                    + " product" + ((OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS != 1)
                    ? "s"
                    : ""));
        }

        return getDataTableSpecs();
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
        DataTableSpec[] dataTableSpecs = getDataTableSpecs();
        BufferedDataContainer container = exec.createDataContainer(dataTableSpecs[0]);
        Reaction reaction = m_settings.getReaction();

        List<List<StereoMolecule>> reactantMolecules = getReactantMolecules(inData);

        ReactionResult reactionResult = run(reaction, reactantMolecules, exec);
        int nrColumns = dataTableSpecs[0].getNumColumns();
        int rowCounter = 1;
        int reactants = reaction.getReactants();
        int[] reactantNameColumnIdx = new int[reactants];
        for (int i = 0; i < reactants; i++) {
            String reactantNameColumnName = this.m_settings.getReactantNameColumnName(i);
            reactantNameColumnIdx[i] = SpecHelper.getColumnIndex(inData[i].getDataTableSpec(), reactantNameColumnName);
        }
        int currentRow = 0;
        int rows = reactionResult.reactionProducts.size();
        for (ReactionProduct result : reactionResult.reactionProducts) {
            for (String productIdCode : result.getProducts()) {
                RowKey key = new RowKey("Row " + rowCounter++);
                DataCell[] cells = new DataCell[nrColumns];

                cells[0] = new OCLMoleculeDataCell(productIdCode);

                for (int i = 0; i < result.reactants.length; i++) {
                    int reactantIdx = result.reactants[i];
                    Reactant reactant = reactionResult.reactants[i][reactantIdx];
                    OCLMoleculeDataCell reactantDataCell = new OCLMoleculeDataCell(reactant.getIdCode());
                    StringCell reactantNameDataCell = new StringCell(reactant.getName());
                    cells[1 + 2 * i] = reactantNameDataCell;
                    cells[2 + 2 * i] = reactantDataCell;
                }

                DataRow row = new DefaultRow(key, cells);

                container.addRowToTable(row);
            }
            currentRow++;

            exec.setProgress(0.5 + (double) currentRow / (double) rows);


        }

        container.close();

        return new BufferedDataTable[]{container.getTable()};
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
        (new OCLCombinatorialLibraryNodeSettings()).loadSettingsForModel(nodeSettingsRO);
    }

    private String createReactantString(int reactantIdx) {
        return "" + (char) (65 + reactantIdx);
    }

    private ReactionResult run(Reaction reaction, List<List<StereoMolecule>> reactantMolecules, ExecutionContext exec) throws Exception {
        ReactionResult reactionResult = new ReactionResult(reactantMolecules);

        boolean generateAll = m_settings.isGenerateAll();
        Reactor reactor = new Reactor(reaction, true);
        int reactantCount = reactantMolecules.size();
        int[] base = new int[reactantCount];

        for (int reactantIdx = 0; reactantIdx < reactantCount; reactantIdx++) {
            base[reactantIdx] = reactantMolecules.get(reactantIdx).size();
        }

        ArbitraryNumber arbitraryNumber = new ArbitraryNumber(base);
        int max = arbitraryNumber.getMaxNumber();

        exec.setProgress(0);

        for (int i = 0; i <= max; i++) {
            ReactionProduct reactionProduct = new ReactionProduct();

            arbitraryNumber.setDecimalValue(i);

            int[] reactantIndeces = arbitraryNumber.getInMixedBase();

            for (int reactantIdx = 0; reactantIdx < reactantCount; reactantIdx++) {
                StereoMolecule reactant = reactantMolecules.get(reactantIdx).get(reactantIndeces[reactantIdx]);
                reactor.setReactant(reactantIdx, reactant);
            }
            reactionProduct.addReactant(reactantIndeces);
            
            //ArrayList<StereoMolecule> productList = reactor.getProductList(0);
            ArrayList<StereoMolecule> productList = null;
            if(reactor.getProducts()[0]==null) {
            	System.out.println("Something wrong..");            	
            }
            else {
            	productList = new ArrayList<>( Arrays.stream(reactor.getProducts()[0]).collect(Collectors.toList())  );
            }
            

            if (!productList.isEmpty()) {
                if (generateAll) {
                    for (StereoMolecule product : productList) {
                        reactionProduct.addProduct(product);
                    }
                } else {
                    reactionProduct.addProduct(productList.get(0));
                }
                reactionResult.reactionProducts.add(reactionProduct);
            }
            exec.setProgress((double) i / (double) max / 2.0);
        }


        return reactionResult;
    }

    //~--- get methods --------------------------------------------------------

    private int getConnectedInputs(DataTableSpec[] inSpecs) {
        int connectedInputs = 0;

        for (DataTableSpec inSpec : inSpecs) {
            if (inSpec != null) {
                connectedInputs++;
            }
        }

        return connectedInputs;
    }

    private DataTableSpec[] getDataTableSpecs() {
        Reaction reaction = m_settings.getReaction();
        DataColumnSpec[] columnSpecs = new DataColumnSpec[reaction.getReactants() * 2 + 1];
        int columnSpecIdx = 0;

        columnSpecs[columnSpecIdx++] = SpecHelper.createMoleculeColumnSpec("Product");

        for (int reactantIdx = 0; reactantIdx < reaction.getReactants(); reactantIdx++) {
            String reactantString = createReactantString(reactantIdx);

            columnSpecs[columnSpecIdx++] = SpecHelper.createStringColumnSpec("Reactant " + reactantString + " Name");
            columnSpecs[columnSpecIdx++] = SpecHelper.createMoleculeColumnSpec("Reactant " + reactantString);
        }

        DataTableSpec outputSpec = new DataTableSpec(columnSpecs);

        return new DataTableSpec[]{outputSpec};
    }

    private List<List<StereoMolecule>> getReactantMolecules(BufferedDataTable[] inData) {
        List<List<StereoMolecule>> reactants = new ArrayList<List<StereoMolecule>>();

        for (int reactantIdx = 0; reactantIdx < OCLCombinatorialLibraryNodeSettings.MAX_REACTANTS; reactantIdx++) {
            BufferedDataTable dataTable = inData[reactantIdx];

            if (dataTable != null) {
                String reactantNameColumnName = m_settings.getReactantNameColumnName(reactantIdx);
                int reactantNameColumnIdx = SpecHelper.getColumnIndex(dataTable.getDataTableSpec(), reactantNameColumnName);

                List<StereoMolecule> molecules = new ArrayList<StereoMolecule>();
                String reactantColumnName = m_settings.getReactantColumnName(reactantIdx);
                int columnIdx = SpecHelper.getColumnIndex(dataTable.getDataTableSpec(), reactantColumnName);
                CloseableRowIterator rowIterator = dataTable.iterator();
                int idx = 1;
                while (rowIterator.hasNext()) {
                    DataRow next = rowIterator.next();
                    DataCell molCell = next.getCell(columnIdx);
                    StringValue nameCell = null;
                    if (reactantNameColumnIdx > -1) {
                        DataCell cell = next.getCell(reactantNameColumnIdx);
                        if (cell.getType().isCompatible(StringValue.class)) {
                            nameCell = (StringValue) cell;
                        }
                    }

                    if (molCell.getType().isCompatible(OCLMoleculeDataValue.class)) {
                        StereoMolecule mol = ((OCLMoleculeDataValue) molCell).getMolecule().getCompactCopy();
                        if (nameCell != null) {
                            mol.setName(nameCell.getStringValue());
                        } else {
                            mol.setName("Molecule " + idx++);
                        }
                        molecules.add(mol);
                    }
                }
                reactants.add(molecules);
            }


        }

        return reactants;

//      for (BufferedDataTable data : inData) {
//          if (data != null) {
//              CloseableRowIterator rowIterator = data.iterator();
//              while(rowIterator.hasNext()){
//                  DataRow next = rowIterator.next();
//
//              }
//          }
//      }
//      return null;
    }

    //~--- inner classes ------------------------------------------------------

    private class Reactant {
        private byte[] idCode;
        private byte[] name;

        //~--- constructors ---------------------------------------------------

        public Reactant(StereoMolecule molecule) {
            this.idCode = molecule.getIDCode().getBytes();
            String name = molecule.getName();
            name = name == null ? "" : name;
            this.name = name.getBytes();
        }

        public String getName() {
            return new String(name);
        }

        public String getIdCode() {
            return new String(idCode);
        }


    }

    private class ReactionResult {
        private Reactant[][] reactants;
        private List<ReactionProduct> reactionProducts = new ArrayList<ReactionProduct>();

        private ReactionResult(List<List<StereoMolecule>> reactantMolecules) {

            reactants = new Reactant[reactantMolecules.size()][];
            for (int reactantIdx = 0; reactantIdx < reactantMolecules.size(); reactantIdx++) {
                List<StereoMolecule> molecules = reactantMolecules.get(reactantIdx);
                reactants[reactantIdx] = new Reactant[molecules.size()];
                for (int molIdx = 0; molIdx < molecules.size(); molIdx++) {
                    StereoMolecule molecule = molecules.get(molIdx);
                    reactants[reactantIdx][molIdx] = new Reactant(molecule);
                }
            }

        }

    }


    private class ReactionProduct {
        int[] reactants;
        List<byte[]> products = new ArrayList<byte[]>();

        //~--- methods --------------------------------------------------------

        public void addReactant(int[] idxs) {
            this.reactants = idxs;
        }

        public void setProduct(List<String> products) {
            for (String product : products) {
                this.products.add(product.getBytes());
            }
        }

        public List<String> getProducts() {
            List<String> result = new ArrayList<String>();
            for (byte[] product : products) {
                result.add(new String(product));
            }
            return result;
        }

        public void addProduct(StereoMolecule product) {
            products.add(product.getIDCode().getBytes());
        }
    }
}
