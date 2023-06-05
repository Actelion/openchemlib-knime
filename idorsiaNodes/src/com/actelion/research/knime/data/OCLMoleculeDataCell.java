package com.actelion.research.knime.data;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.SmilesCreator;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.coords.CoordinateInventor;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.NodeLogger;

import java.util.ArrayList;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

public class OCLMoleculeDataCell extends DataCell implements OCLMoleculeDataValue, SdfValue, MolValue, SmilesValue {
    public static final DataType TYPE = DataType.getType(OCLMoleculeDataCell.class);
    private static final OCLMoleculeDataCellSerializer SERIALIZER = new OCLMoleculeDataCellSerializer();
    private static NodeLogger LOG = NodeLogger.getLogger(OCLMoleculeDataCell.class);

    //~--- fields -------------------------------------------------------------
    //private List<String> calculatedDescriptors = new ArrayList<String>();

    // ~--- fields -------------------------------------------------------------
    //private byte[] coordinates;
    private boolean fragment;
    private byte[] idCode;

    //~--- constructors -------------------------------------------------------

    //  private StereoMolecule molecule;
    public OCLMoleculeDataCell(StereoMolecule molecule) {

//      this.molecule = molecule;
        Canonizer canonizer = new Canonizer(molecule);

        this.idCode = canonizer.getIDCode().getBytes();
        //this.coordinates = canonizer.getEncodedCoordinates().getBytes();
    }

    public OCLMoleculeDataCell(String idCode) {
        this(idCode, null, false);
    }

    public OCLMoleculeDataCell(String idCode, boolean fragment) {
        this(idCode, null, fragment);
    }

    public OCLMoleculeDataCell(String idCode, String coordinates) {
        this(idCode, coordinates, false);
    }

    // TODO: remove second parameter (and maybe third as well..) from Constructor (should break only in one or two places..) 
    public OCLMoleculeDataCell(String idCode, String coordinates, boolean fragment) {
    	if(idCode.contains("<NL>")) {
    		IDCodeParser icp = new IDCodeParser();
    		StereoMolecule assembled_splits = new StereoMolecule();
    		String splits[] = idCode.split("<NL>");
    		for(String si : splits) {
    			StereoMolecule smi = new StereoMolecule();
    			icp.parse(smi, si);
    			smi.ensureHelperArrays(StereoMolecule.cHelperCIP);
    			assembled_splits.addFragment(smi, 0, null);
    		}
    		assembled_splits.ensureHelperArrays(StereoMolecule.cHelperCIP);
    		this.idCode = assembled_splits.getIDCode().getBytes();
    	}
    	else {
    		this.idCode = idCode.getBytes();
    	}
        //this.coordinates = coordinates == null ? new byte[0] : coordinates.getBytes();
        this.fragment = fragment;
    }

    //~--- get methods --------------------------------------------------------

    // ~--- constructors -------------------------------------------------------
    public static final OCLMoleculeDataCellSerializer getCellSerializer() {
        return SERIALIZER;
    }

    //~--- methods ------------------------------------------------------------

    // ~--- methods ------------------------------------------------------------
    @Override
    public int hashCode() {
        return (idCode != null)
                ? new String(idCode).hashCode()
                : 0;
    }

    @Override
    public String toString() {
        return "Molecule " + new String(idCode);
    }


    // ~--- get methods --------------------------------------------------------
    @Override
    public String getIdCode() {
        return new String(idCode);
    }

//    @Override
//    public String[] getIdCodeAndCoordinates() {
//        return new String[]{getIdCode(), getCoordinates()};
//    }

    @Override
    public String getMolValue() {
        MolfileCreator molfileCreator = new MolfileCreator(getMolecule());

        return molfileCreator.getMolfile();
    }

    @Override
    public StereoMolecule getMolecule() {
//        boolean needsCoordinates = false;
        StereoMolecule molecule = new StereoMolecule();

        molecule.setFragment(fragment);
        String idCodeString = new String(this.idCode);
//        String coordinatesString = new String(this.coordinates);
//        String[] idCodeParts = idCodeString.split("<NL>", -1);
//        String[] coordinatesParts = coordinatesString.split("<NL>", -1);
//
//        if (idCodeParts.length != coordinatesParts.length) {
//            needsCoordinates = true;
//        }
//
//        IDCodeParser idCodeParser = new IDCodeParser();
//
//        for (int i = 0; i < idCodeParts.length; i++) {
//            String idCodePart = idCodeParts[i];
//            String coordinatesPart = "";
//
//            if (!needsCoordinates) {
//                coordinatesPart = coordinatesParts[i];
//            }
//
//            StereoMolecule part = new StereoMolecule();
//
//            part.setFragment(fragment);
//            idCodeParser.parse(part, idCodePart, coordinatesPart);
//            molecule.addMolecule(part);
//        }
//
//        if (needsCoordinates) {
//            CoordinateInventor coordinateInventor = new CoordinateInventor();
//
//            coordinateInventor.invent(molecule);
//            this.coordinates = molecule.getIDCoordinates().getBytes();
//        }
        
        //IDCodeParser idCodeParser = new IDCodeParser();
        IDCodeParser idCodeParser = new IDCodeParser(true);
        //System.out.println("getMolecule() -> "+idCodeString);
        idCodeParser.parse(molecule, idCodeString);
        
        return molecule;
    }

    @Override
    public String getSdfValue() {
        MolfileCreator molfileCreator = new MolfileCreator(getMolecule());

        return molfileCreator.getMolfile() + "\n$$$$";
    }

    @Override
    public String getSmilesValue() {
//        SmilesCreator smilesCreator = new SmilesCreator();
//        return smilesCreator.generateSmiles(getMolecule());
    	IsomericSmilesCreator creator = new IsomericSmilesCreator(getMolecule());    			
    	return creator.getSmiles();
    }

    public boolean isFragment() {
        return fragment;
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }
    
    // ~--- methods ------------------------------------------------------------
    @Override
    protected boolean equalsDataCell(DataCell dc) {
        if (dc instanceof OCLMoleculeDataCell) {
            OCLMoleculeDataCell moleculeDataCell = (OCLMoleculeDataCell) dc;

            return moleculeDataCell.getIdCode().equals(getIdCode()) && (moleculeDataCell.fragment == fragment);
        }

        return false;
    }

    //~--- get methods --------------------------------------------------------

    // NOTE: this will be reimplemented as a helper function which takes as parameter the TableDescription and then
    //       finds all descriptors which have as originaMolecule the given column. (or something like this..)
    // 
    
//    List<String> getCalulatedDescriptors() {
//        return calculatedDescriptors;
//    }

    //~--- methods ------------------------------------------------------------

//    private String getCoordinates() {
//        return new String(coordinates);
//    }
}
