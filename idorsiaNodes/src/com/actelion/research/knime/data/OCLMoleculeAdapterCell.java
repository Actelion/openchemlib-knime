package com.actelion.research.knime.data;

import com.actelion.research.chem.StereoMolecule;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.AdapterCell;
import org.knime.core.data.AdapterValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.node.NodeLogger;

import java.io.IOException;
import java.util.Objects;

//~--- JDK imports ------------------------------------------------------------

public class OCLMoleculeAdapterCell extends AdapterCell implements OCLMoleculeDataValue, SdfValue, MolValue, SmilesValue {

    //
    // Constants
    //

    /**
     * The raw type of this adapter cell with only the implemented value classes. The type of the
     * cell may change if additional adapters are added.
     */
    public static final DataType RAW_TYPE = DataType.getType(OCLMoleculeAdapterCell.class);
    /**
     * The logger instance.
     */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(OCLMoleculeAdapterCell.class);
    /**
     * The serial number.
     */
    private static final long serialVersionUID = -994027050919072740L;
    /**
     * The serializer instance.
     */
    @Deprecated
    private static final OCLMoleculeAdapterCellSerializer SERIALIZER = new OCLMoleculeAdapterCellSerializer();

    //~--- constructors -------------------------------------------------------

    /**
     * Creates a new adapter cell and adds the passed in cell.
     *
     * @param cell Cell to be added as first element. Must not be null.
     */
    @SuppressWarnings("unchecked")
    public OCLMoleculeAdapterCell(final DataCell cell) {
        super(cell);
    }

    //
    // Constructors
    //

    /**
     * Creates a new adapter cell for the passed in data input.
     *
     * @param input Input data. Must not be null.
     * @throws IOException Thrown, if it could not be created.
     */
    private OCLMoleculeAdapterCell(final DataCellDataInput input) throws IOException {
        super(input);
    }

    /**
     * Creates a new adapter cell and adds the passed in cell and copies
     * all cells that are contained in the passed in copy parameter.
     *
     * @param copy An existing adapter value, from which all cells are copied into the new adapter
     *             cell. Can be null.
     * @param cell Cell to be added to the existing cell collection. Must not be null.
     */
    @SuppressWarnings("unchecked")
    public OCLMoleculeAdapterCell(final DataCell cell, final AdapterValue copy) {
        super(cell, copy);
    }

    //~--- get methods --------------------------------------------------------

    //
    // Public Static Methods
    //

    /**
     * Returns the cell serializer for OCL Adapter Cells.
     *
     * @return Singleton serializer instance. Never null.
     * @deprecated As of KNIME 3.0 data types are registered via extension point. This method is not
     * used anymore. The serializer is made known in the extension point configuration.
     */
    public static DataCellSerializer<OCLMoleculeAdapterCell> getCellSerializer() {
        return SERIALIZER;
    }

    //~--- methods ------------------------------------------------------------

    @Override
    public int hashCode() {
        int result = 1;

        try {
            final int prime = 31;

            result = prime * result + ((OCLMoleculeDataValue) getAdapterMap().get(OCLMoleculeDataValue.class)).getIdCode().hashCode();
        } catch (Exception exc) {
            LOGGER.error("Unable to calculate hash code for OCL molecule.", exc);
        }

        return result;
    }


    @Override
    public String getIdCode() {
        OCLMoleculeDataValue oclValue = null;

        try {
            oclValue = (OCLMoleculeDataValue) lookupFromAdapterMap(OCLMoleculeDataValue.class);
        } catch (final IllegalArgumentException exc) {

            // Rethrow a better error message, which appears as warning in the node
            throw new IllegalArgumentException("Unable to access IDCode in OCL Adapter Cell.", exc);
        }

        return oclValue.getIdCode();
    }

//    @Override
//    public String[] getIdCodeAndCoordinates() {
//        OCLMoleculeDataValue oclValue = null;
//
//        try {
//            oclValue = (OCLMoleculeDataValue) lookupFromAdapterMap(OCLMoleculeDataValue.class);
//        } catch (final IllegalArgumentException exc) {
//
//            // Rethrow a better error message, which appears as warning in the node
//            throw new IllegalArgumentException("Unable to access IDCode and coordinates in OCL Adapter Cell.", exc);
//        }
//
//        return oclValue.getIdCodeAndCoordinates();
//    }

    @Override
    public String getMolValue() {
        MolValue oclValue = null;

        try {
            oclValue = (MolValue) lookupFromAdapterMap(MolValue.class);
        } catch (final IllegalArgumentException exc) {

            // Rethrow a better error message, which appears as warning in the node
            throw new IllegalArgumentException("Unable to access Molecule in OCL Adapter Cell.", exc);
        }

        return oclValue.getMolValue();
    }

    @Override
    public StereoMolecule getMolecule() {
        OCLMoleculeDataValue oclValue = null;

        try {
            oclValue = (OCLMoleculeDataValue) lookupFromAdapterMap(OCLMoleculeDataValue.class);
        } catch (final IllegalArgumentException exc) {

            // Rethrow a better error message, which appears as warning in the node
            throw new IllegalArgumentException("Unable to access Molecule in OCL Adapter Cell.", exc);
        }

        return oclValue.getMolecule();
    }

    @Override
    public String getSdfValue() {
        SdfValue oclValue = null;

        try {
            oclValue = (SdfValue) lookupFromAdapterMap(SdfValue.class);
        } catch (final IllegalArgumentException exc) {

            // Rethrow a better error message, which appears as warning in the node
            throw new IllegalArgumentException("Unable to access Molecule in OCL Adapter Cell.", exc);
        }

        return oclValue.getSdfValue();
    }

    @Override
    public String getSmilesValue() {
        SmilesValue oclValue = null;

        try {
            oclValue = (SmilesValue) lookupFromAdapterMap(SmilesValue.class);
        } catch (final IllegalArgumentException exc) {

            // Rethrow a better error message, which appears as warning in the node
            throw new IllegalArgumentException("Unable to access Molecule in OCL Adapter Cell.", exc);
        }

        return oclValue.getSmilesValue();
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected boolean equalsDataCell(DataCell dc) {
        return Objects.equals(getIdCode(), (((OCLMoleculeAdapterCell) dc).getIdCode()));
    }

    //~--- inner classes ------------------------------------------------------

    /**
     * Serializer for {@link OCLMoleculeAdapterCell}s.
     *
     * @noreference This class is not intended to be referenced by clients.
     * @since 3.0
     */
    public static final class OCLMoleculeAdapterCellSerializer extends AdapterCellSerializer<OCLMoleculeAdapterCell> {
        @Override
        public OCLMoleculeAdapterCell deserialize(final DataCellDataInput input) throws IOException {

            return new OCLMoleculeAdapterCell(input);
        }

        @Override
        public void serialize(OCLMoleculeAdapterCell cell, DataCellDataOutput output) throws IOException {
            super.serialize(cell, output);
        }
    }
}
