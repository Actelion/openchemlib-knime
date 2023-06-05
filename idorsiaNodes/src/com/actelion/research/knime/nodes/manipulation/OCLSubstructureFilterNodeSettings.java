package com.actelion.research.knime.nodes.manipulation;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.nodes.AbstractOCLNodeSettings;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class OCLSubstructureFilterNodeSettings extends AbstractOCLNodeSettings {
    private static final String FRAGMENT          = "fragment";
    private static final String INPUT_COLUMN_NAME = "inputColumnName";

    //~--- fields -------------------------------------------------------------

    private IDCodeParser idCodeParser = new IDCodeParser();
    private DataCell     defaultValue = DataType.getMissingCell();
    private DataCell     mapMissingTo = DataType.getMissingCell();

    // private static final String SIMILARITY = "similarity";
    private StereoMolecule fragment;
    private String         inputColumnName;

    //~--- constructors -------------------------------------------------------

    public OCLSubstructureFilterNodeSettings() {
        this.fragment        = new StereoMolecule();
        this.inputColumnName = "";
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
        settings.addStringArray(FRAGMENT, fragment.getIDCode(), fragment.getIDCoordinates());
        settings.addString(INPUT_COLUMN_NAME, inputColumnName);
    }

    //~--- get methods --------------------------------------------------------

    public StereoMolecule getFragment() {
        return fragment;
    }

    public String getInputColumnName() {
        return inputColumnName;
    }

    //~--- set methods --------------------------------------------------------

    public void setFragment(StereoMolecule fragment) {
        this.fragment = fragment;
    }

    public void setInputColumnName(String inputColumnName) {
        this.inputColumnName = inputColumnName;
    }

    //~--- methods ------------------------------------------------------------

    private void loadSettings(NodeSettingsRO settings) {
        String[] fragmentIdCodeCoords = settings.getStringArray(FRAGMENT, (String[]) null);

        this.fragment = new StereoMolecule();

        if (fragmentIdCodeCoords != null) {
            idCodeParser.parse(this.fragment, fragmentIdCodeCoords[0], fragmentIdCodeCoords[1]);
        }
        this.fragment.setFragment(true);
        this.inputColumnName = settings.getString(INPUT_COLUMN_NAME, "");
    }
}
