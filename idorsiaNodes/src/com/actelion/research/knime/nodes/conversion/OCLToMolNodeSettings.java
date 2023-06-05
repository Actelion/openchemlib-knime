package com.actelion.research.knime.nodes.conversion;

import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLToMolNodeSettings extends AbstractOCLNodeSettings {
    private static final String     DEFAULT_NEW_COLUMN_NAME     = "Molecule";
    private static final String     DEFAULT_INPUT_COLUMN_NAME   = "";
    private static final String     NEW_COLUMN_NAME             = "newColumnName";
    private static final String     INPUT_COLUMN_NAME           = "inputColumnName";
    private static final String     REMOVE_INPUT_COLUMN         = "removeInputColumn";
    private static final String     OUTPUT_TYPE                 = "outputType";
    private static final boolean    DEFAULT_REMOVE_INPUT_COLUMN = false;
    private static final OutputType DEFAULT_OUTPUT_TYPE         = OutputType.SDF;

    //~--- fields -------------------------------------------------------------

    private String     newColumnName;
    private String     inputColumnName;
    private boolean    removeInputColumn;
    private OutputType outputType;

    //~--- constructors -------------------------------------------------------

    public OCLToMolNodeSettings() {
        this.inputColumnName = DEFAULT_INPUT_COLUMN_NAME;
        this.newColumnName   = DEFAULT_NEW_COLUMN_NAME;
    }

    //~--- enums --------------------------------------------------------------

    public enum OutputType {
        MOL("Mol", MolValue.class), SDF("Sdf", SdfValue.class), SMILES("SMILES", SmilesValue.class)
        ;

        private String                     description;
        private Class<? extends DataValue> type;

        //~--- constructors ---------------------------------------------------

        OutputType(String description, Class<? extends DataValue> type) {
            this.description = description;
            this.type        = type;
        }

        //~--- methods --------------------------------------------------------

        public static OutputType fromString(String s) {
            if (s == null) {
                return null;
            }

            for (OutputType outputType : OutputType.values()) {
                if (outputType.description.equals(s)) {
                    return outputType;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return description;
        }
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
        settings.addString(INPUT_COLUMN_NAME, inputColumnName);
        settings.addString(NEW_COLUMN_NAME, newColumnName);
        settings.addBoolean(REMOVE_INPUT_COLUMN, removeInputColumn);
        settings.addString(OUTPUT_TYPE, outputType.toString());
    }

    //~--- get methods --------------------------------------------------------

    public String getInputColumnName() {
        return inputColumnName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public boolean isRemoveInputColumn() {
        return removeInputColumn;
    }

    //~--- set methods --------------------------------------------------------

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public void setRemoveInputColumn(boolean removeInputColumn) {
        this.removeInputColumn = removeInputColumn;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        this.inputColumnName   = settings.getString(INPUT_COLUMN_NAME, DEFAULT_INPUT_COLUMN_NAME);
        this.newColumnName     = settings.getString(NEW_COLUMN_NAME, DEFAULT_NEW_COLUMN_NAME);
        this.removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN, DEFAULT_REMOVE_INPUT_COLUMN);

        String outputTypeString = settings.getString(OUTPUT_TYPE, null);

        this.outputType = OutputType.fromString(outputTypeString);
        outputType      = (outputType == null)
                          ? DEFAULT_OUTPUT_TYPE
                          : outputType;
    }
}
