package com.actelion.research.knime.data;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

import org.knime.chem.types.MolAdapterCell;
import org.knime.chem.types.MolCellFactory;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfAdapterCell;
import org.knime.chem.types.SdfCellFactory;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.AdapterValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellTypeConverter;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.RWAdapterValue;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

public abstract class OCLMoleculeTypeConverter extends DataCellTypeConverter {

    //
    // Constants
    //

    /**
     * Array with the value classes that can be handled by an RDKit Adapter.
     */
    @SuppressWarnings("unchecked")
    public static final Class<? extends DataValue>[] ADAPTABLE_VALUE_CLASSES = new Class[] { OCLMoleculeDataValue.class, SdfValue.class,
            MolValue.class };

    //~--- fields -------------------------------------------------------------

    //
    // Members
    //

    /**
     * The output type of the converter instance.
     */
    private final DataType m_outputType;

    //~--- constructors -------------------------------------------------------

    //
    // Constructors
    //

    /**
     * Creates a converter instance for the specified output type.
     *
     * @param outputType The output type of the converter. Must not be null.
     */
    public OCLMoleculeTypeConverter(final DataType outputType) {
        super(true);    // True means that parallel processing of conversion is allowed
        m_outputType = outputType;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Creates a new converter for a specific source type.
     *
     * @param type Source type that needs to be converted. Must not be null.
     * @return A new converter or null, if no converter available.
     */
    public static OCLMoleculeTypeConverter createConverter(final DataType type) {

        // Process an existing adapter cell - we just want to add here an RDKit cell
        if (type.isCompatible(AdapterValue.class)) {
            if (type.isCompatible(OCLMoleculeDataValue.class)) {

                // We have already an Adapter cell that is compatible with RDKit Mol Value - we return it
                return new OCLMoleculeTypeConverter(type) {

                    /**
                     * {@inheritDoc}
                     * Just returns the existing RDKit Mol Value within a new RDKit Adapter Cell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        return source;
                    }
                };
            }

            if (type.isCompatible(RWAdapterValue.class) && type.isCompatible(SdfValue.class)) {

                // We have a writable adapter cell that contains an SDF value
                // thus we can just add the RDKitCell
                final Class<? extends DataValue>[] arrValueClasses = determineValueClassesToSupport(type);

                return new OCLMoleculeTypeConverter(type.createNewWithAdapter(arrValueClasses)) {

                    /**
                     * {@inheritDoc}
                     * Based on the existing SDF value we create an ROMol object and a SMILES value
                     * and from these two objects an RDKit Cell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        if ((source == null) || source.isMissing()) {
                            return DataType.getMissingCell();
                        }

                        final String strSdf = ((RWAdapterValue) source).getAdapter(SdfValue.class).getSdfValue();

                        return ((RWAdapterValue) source).cloneAndAddAdapter(createOCLMoleculeAdapterCellFromSdf(strSdf), arrValueClasses);
                    }
                };
            } else if (type.isCompatible(RWAdapterValue.class) && type.isCompatible(MolValue.class)) {

                // We have a writable adapter cell that contains a SMILES value
                // thus we can just add the RDKitCell
                final Class<? extends DataValue>[] arrValueClasses = determineValueClassesToSupport(type);

                return new OCLMoleculeTypeConverter(type.createNewWithAdapter(arrValueClasses)) {

                    /**
                     * {@inheritDoc}
                     * Based on the existing SDF value we create an ROMol object and a SMILES value
                     * and from these two objects an RDKit Cell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        if ((source == null) || source.isMissing()) {
                            return DataType.getMissingCell();
                        }

                        final String molValue = ((RWAdapterValue) source).getAdapter(MolValue.class).getMolValue();

                        return ((RWAdapterValue) source).cloneAndAddAdapter(createOCLMoleculeAdapterCellFromMolFile(molValue),
                                arrValueClasses);
                    }
                };
            } else if (type.isAdaptable(SdfValue.class) /* but not based on a RWAdapterValue */) {

                // We have a read only adapter cell that contains an SDF value - we create a new SDF Adapter Cell
                // and add the new RDKit Cell which gets created based on the SDF value
                // Note: This case should not really happen as AdapterCells are today implementing RWAdapterValue,
                // which is handled above
                final Class<? extends DataValue>[] arrValueClasses = determineValueClassesToSupport(SdfAdapterCell.RAW_TYPE);

                return new OCLMoleculeTypeConverter(SdfAdapterCell.RAW_TYPE.createNewWithAdapter(arrValueClasses)) {

                    /**
                     * {@inheritDoc}
                     * Based on the existing SDF value we create an ROMol object and a SMILES value
                     * and from these two objects an RDKit Cell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        if ((source == null) || source.isMissing()) {
                            return DataType.getMissingCell();
                        }

                        final String strSdf = ((AdapterValue) source).getAdapter(SdfValue.class).getSdfValue();

                        return ((RWAdapterValue) SdfCellFactory.createAdapterCell(strSdf)).cloneAndAddAdapter(
                            createOCLMoleculeAdapterCellFromSdf(strSdf), arrValueClasses);
                    }
                };
            }

            // We have a read only adapter cell that contains a SMILES value - we create a new SMILES Adapter Cell
            // and add the new RDKit Cell which gets created based on the SMILES value
            // Note: This case should not really happen as AdapterCells are today implementing RWAdapterValue,
            // which is handled above
            else if (type.isAdaptable(MolValue.class) /* but not based on a RWAdapterValue */) {
                final Class<? extends DataValue>[] arrValueClasses = determineValueClassesToSupport(MolAdapterCell.RAW_TYPE);

                return new OCLMoleculeTypeConverter(MolAdapterCell.RAW_TYPE.createNewWithAdapter(arrValueClasses)) {

                    /**
                     * {@inheritDoc}
                     * Based on the existing SMILES value we create an ROMol object and an RDKit Cell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        if ((source == null) || source.isMissing()) {
                            return DataType.getMissingCell();
                        }

                        final String molValue = ((AdapterValue) source).getAdapter(MolValue.class).getMolValue();

                        return ((RWAdapterValue) MolCellFactory.createAdapterCell(molValue)).cloneAndAddAdapter(
                            createOCLMoleculeAdapterCellFromMolFile(molValue), arrValueClasses);
                    }
                };
            }
        }

        // Process a normal cell (no adapter cell) and create a new Adapter Cell based on the input
        // Note: These cases should not really happen anymore because since KNIME 3.0 molecule output should be done in
        // AdapterCells always, e.g. as SdfAdapterCells or SmilesAdapterCells, which would be handled above
        else {
            if (type.isCompatible(OCLMoleculeDataValue.class)) {

                // We have already an RDKit Mol Value - we just create from it an RDKit Adapter Cell
                return new OCLMoleculeTypeConverter(OCLMoleculeAdapterCell.RAW_TYPE) {

                    /**
                     * {@inheritDoc}
                     * Just returns the existing RDKit Mol Value within a new RDKit Adapter Cell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        if ((source == null) || source.isMissing()) {
                            return DataType.getMissingCell();
                        }

                        return new OCLMoleculeAdapterCell(source);
                    }
                };
            } else if (type.isCompatible(SdfValue.class)) {

                // We have an SDF value - we create a new SDF Adapter Cell with
                // the new RDKit Cell attached
                final Class<? extends DataValue>[] arrValueClasses = determineValueClassesToSupport(SdfAdapterCell.RAW_TYPE);

                return new OCLMoleculeTypeConverter(SdfAdapterCell.RAW_TYPE.createNewWithAdapter(arrValueClasses)) {

                    /**
                     * {@inheritDoc}
                     * Based on the existing SDF value we create an ROMol object and a SMILES value
                     * and from these two objects an RDKit Cell which we attach to a new SdfAdapterCell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        if ((source == null) || source.isMissing()) {
                            return DataType.getMissingCell();
                        }

                        final String strSdf = ((SdfValue) source).getSdfValue();

                        return ((RWAdapterValue) SdfCellFactory.createAdapterCell(strSdf)).cloneAndAddAdapter(
                            createOCLMoleculeAdapterCellFromSdf(strSdf), arrValueClasses);
                    }
                };
            } else if (type.isCompatible(MolValue.class)) {

                // We have a SMILES value - we create a new Mol Adapter Cell with
                // the new RDKit Cell attached
                final Class<? extends DataValue>[] arrValueClasses = determineValueClassesToSupport(MolAdapterCell.RAW_TYPE);

                return new OCLMoleculeTypeConverter(MolAdapterCell.RAW_TYPE.createNewWithAdapter(arrValueClasses)) {

                    /**
                     * {@inheritDoc}
                     * Based on the existing SMILES value we create an ROMol object and an RDKit Cell
                     * and attach it to a new MolAdapterCell.
                     */
                    @Override
                    public DataCell convert(final DataCell source) throws Exception {
                        if ((source == null) || source.isMissing()) {
                            return DataType.getMissingCell();
                        }

                        final String strMol = ((MolValue) source).getMolValue();

                        return ((RWAdapterValue) MolCellFactory.createAdapterCell(strMol)).cloneAndAddAdapter(
                            createOCLMoleculeAdapterCellFromMolFile(strMol), arrValueClasses);
                    }
                };
            }
        }

        return null;
    }

    //
    // Public Static Methods
    //

    /**
     * Creates a new converter for a specific column in a table. The output type and the specific converter that is
     * used is determined automatically from the input type.
     *
     * @param tableSpec   the input table's spec
     * @param columnIndex the index of the column that should be converted.
     * @return A new converter or null, if no converter available.
     */
    public static OCLMoleculeTypeConverter createConverter(final DataTableSpec tableSpec, final int columnIndex) {
        final DataType type = tableSpec.getColumnSpec(columnIndex).getType();

        return createConverter(type);
    }

    public static DataCell createOCLMoleculeAdapterCellFromMolFile(final String molFile) throws OCLMoleculeTypeConverterException {
        DataCell cell = DataType.getMissingCell();

        if ((molFile != null) &&!molFile.trim().isEmpty()) {
            return treatMolFile(molFile);
        }

        return cell;
    }

    public static DataCell createOCLMoleculeAdapterCellFromSdf(final String strSdf) throws OCLMoleculeTypeConverterException {
        DataCell cell = DataType.getMissingCell();

        if ((strSdf != null) &&!strSdf.trim().isEmpty()) {
            String molString = strSdf.replaceAll("\n$$$$", "");

            return treatMolFile(molString);
        }

        return cell;
    }

//  /**
//   * Creates an RDKit Adapter Cell from the passed in SMILES string.
//   *
//   * @param strSmiles SMILES string. Can be null.
//   * @return RDKit Adapter cell.
//   * @throws RDKitTypeConverterException Thrown, if SMILES could not be converted successfully.
//   */
//  public static DataCell createRDKitAdapterCellFromSmiles(final String strSmiles) throws RDKitTypeConverterException {
//      DataCell cell = DataType.getMissingCell();
//
//      if (strSmiles != null) {
//          ROMol mol = null;
//
//          try {
//              Exception excCaught = null;
//
//              // As first step try to parse the input molecule format
//              try {
//                  mol = RWMol.MolFromSmiles(strSmiles, 0, true);
//              } catch (final Exception exc) {
//                  // Parsing failed and RDKit molecule is null
//                  excCaught = exc;
//              }
//
//              // If we got an RDKit molecule, parsing was successful, now create the cell
//              if (mol != null) {
//                  try {
//                      cell = RDKitMolCellFactory.createRDKitAdapterCell(mol, strSmiles);
//                  } catch (final Exception exc) {
//                      excCaught = exc;
//                  }
//              }
//
//              // Do error handling depending on user settings
//              if (mol == null || excCaught != null) {
//                  // Find error message
//                  final StringBuilder sbError = new StringBuilder("SMILES");
//
//                  // Specify error type
//                  if (mol == null) {
//                      sbError.append(" Parsing Error (");
//                  } else {
//                      sbError.append(" Process Error (");
//                  }
//
//                  // Specify exception
//                  if (excCaught != null) {
//                      sbError.append(excCaught.getClass().getSimpleName());
//
//                      // Specify error message
//                      final String strMessage = excCaught.getMessage();
//                      if (strMessage != null) {
//                          sbError.append(" (").append(strMessage).append(")");
//                      }
//                  } else {
//                      sbError.append("Details unknown");
//                  }
//
//                  sbError.append(") for\n" + strSmiles);
//
//                  // Throw an exception - this will lead to a missing cell with the error message
//                  throw new RDKitTypeConverterException(sbError.toString(), excCaught);
//              }
//          } finally {
//              if (mol != null) {
//                  mol.delete();
//              }
//          }
//      }
//
//      return cell;
//  }
    @SuppressWarnings("unchecked")
    protected static Class<? extends DataValue>[] determineValueClassesToSupport(DataType type) {
        return determineValueClassesToSupport(type, SdfValue.class, MolValue.class);
    }

    @SuppressWarnings("unchecked")
    protected static Class<? extends DataValue>[] determineValueClassesToSupport(DataType type,
            Class<? extends DataValue>... valueClasses) {
        List<Class<? extends DataValue>> listValueClasses = new ArrayList<Class<? extends DataValue>>();

        listValueClasses.add(OCLMoleculeDataValue.class);

        if ((valueClasses != null) && (valueClasses.length > 0)) {
            for (Class<? extends DataValue> valueClass : valueClasses) {
                if (!listValueClasses.contains(valueClass) &&!type.isCompatible(valueClass) &&!type.isAdaptable(valueClass)) {
                    listValueClasses.add(valueClass);
                }
            }
        }

        return (Class<? extends DataValue>[]) listValueClasses.toArray(new Class[listValueClasses.size()]);
    }

    private static DataCell treatMolFile(String molString) {
        MolfileParser molfileParser = new MolfileParser();

        try {
            StereoMolecule mol = molfileParser.getCompactMolecule(molString);

            return OCLMoleculeCellFactory.createOCLMoleculeCell(mol);
        } catch (Throwable t) {
            throw new OCLMoleculeTypeConverterException(t.toString(), t);
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getOutputType() {
        return m_outputType;
    }
}
