package com.actelion.research.knime.data;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

public class OCLMoleculeDataCellSerializer implements DataCellSerializer<OCLMoleculeDataCell> {
    private static final String NL = "<NL>";

    //~--- methods ------------------------------------------------------------

    @Override
    public OCLMoleculeDataCell deserialize(DataCellDataInput input) throws IOException {
        String idCode = input.readLine();
        OCLMoleculeDataCell oclMoleculeDataCell = new OCLMoleculeDataCell(idCode, "", false);
        return oclMoleculeDataCell;
//        String coordinates = input.readLine();
//        String isFragmentString = input.readLine();
//        boolean isFragment = Boolean.parseBoolean(isFragmentString);
//        OCLMoleculeDataCell oclMoleculeDataCell = new OCLMoleculeDataCell(idCode, coordinates, isFragment);
//        return oclMoleculeDataCell;
    }

    @Override
    public void serialize(OCLMoleculeDataCell oclMoleculeDataCell, DataCellDataOutput output) throws IOException {
    	output.write( (oclMoleculeDataCell.getIdCode()+"\n").getBytes());
//        byte[] ldelim = "\n".getBytes();
//        //String[] idCodeAndCoordinates = oclMoleculeDataCell.getIdCodeAndCoordinates();
//        String idCodeWithoutCoordinates = oclMoleculeDataCell.getIdCode();
//
//        if (idCodeWithoutCoordinates[0].contains("\n")) {
//            System.out.println("");
//        }
//
//        output.write(((idCodeWithoutCoordinates[0] == null)
//                ? ""
//                : idCodeWithoutCoordinates[0]).getBytes());
//        output.write(ldelim);
//        output.write(((idCodeWithoutCoordinates[1] == null)
//                ? ""
//                : idCodeWithoutCoordinates[1]).getBytes());
//        output.write(ldelim);
//        output.write(Boolean.toString(oclMoleculeDataCell.isFragment()).getBytes());
//        output.write(ldelim);
    }
}
