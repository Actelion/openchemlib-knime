package com.actelion.research.knime.utils;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.data.IncompatibleTypeException;
import com.actelion.research.knime.data.OCLDescriptorDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.data.OCLMoleculeDataValue;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfCell;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.AdapterCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.doublevector.DenseDoubleVectorCell;
import org.knime.core.data.vector.doublevector.DoubleVectorCellFactory;

public class ValueHelper {
    public static DataCell createDataCell(Object value) {
        if (value instanceof String) {
            return new StringCell((String) value);
        } else if (value instanceof Double) {
            return new DoubleCell((Double) value);
        } else if (value instanceof Integer) {
            return new IntCell((Integer) value);
        }

        return null;
    }

    public static OCLMoleculeDataCell createFragmentMoleculeDataCell(String idCode) {
        return new OCLMoleculeDataCell(idCode, true);
    }

    public static IntCell createIntDataCell(int value) {
        return new IntCell(value);
    }

    public static OCLMoleculeDataCell createMoleculeDataCell(String idCode) {
        return new OCLMoleculeDataCell(idCode);
    }


    public static DenseDoubleVectorCell createDoubleVectorDataCell(double[] values) {
        return DoubleVectorCellFactory.createCell(values);
    }

    public static OCLDescriptorDataCell createDescriptorDataCell(DescriptorInfo descriptorInfo, byte[] descriptor) {
        return new OCLDescriptorDataCell(descriptorInfo.shortName, descriptor);
    }

//    public static OCLDescriptorDataCell createDescriptorDataCell(String parentColumnName, OCLMoleculeDataValue molecule, DescriptorInfo descriptorInfo) {
//        Object descriptor = molecule.getDescriptor(descriptorInfo);
//        String encode = DescriptorHelpers.encode(descriptor, descriptorInfo);
//        return new OCLDescriptorDataCell(parentColumnName, descriptorInfo.shortName, encode.getBytes());
//    }

    //~--- get methods --------------------------------------------------------

    public static StereoMolecule getMolecule(DataCell cell) {
        if (cell.isMissing()) {
            return new StereoMolecule();
        }

        if (cell.getType().isCompatible(OCLMoleculeDataValue.class)) {
            return ((OCLMoleculeDataValue) cell).getMolecule();
        } else if (cell.getType().isCompatible(SdfValue.class)) {
            SdfValue sdfCell = (SdfCell) cell;
            String sdfValue = sdfCell.getSdfValue();
            String molValue = sdfValue.replaceAll("\n$$$$", "");
            MolfileParser molfileParser = new MolfileParser();

            return molfileParser.getCompactMolecule(molValue);
        } else if (cell.getType().isCompatible(MolValue.class)) {
            MolValue molCell = (MolValue) cell;
            String molValue = molCell.getMolValue();
            MolfileParser molfileParser = new MolfileParser();

            return molfileParser.getCompactMolecule(molValue);
        }

        return new StereoMolecule();
    }

    public static OCLMoleculeDataValue getOCLCell(DataCell cell) throws IncompatibleTypeException {
        if (cell.getType().isCompatible(OCLMoleculeDataValue.class)) {
            return (OCLMoleculeDataValue) cell;
        } else if (cell.getType().isAdaptable(OCLMoleculeDataValue.class)) {
            AdapterCell adapterCell = (AdapterCell) cell;

            return adapterCell.getAdapter(OCLMoleculeDataValue.class);
        }

        throw new IncompatibleTypeException("The cell cannot be converted to OCLMoleculeDataValue");
    }

    public static SdfValue getSDFCell(DataCell cell) throws IncompatibleTypeException {
        if (cell.getType().isCompatible(SdfValue.class)) {
            return (SdfValue) cell;
        } else if (cell.getType().isAdaptable(SdfValue.class)) {
            AdapterCell adapterCell = (AdapterCell) cell;

            return adapterCell.getAdapter(SdfValue.class);
        }

        throw new IncompatibleTypeException("The cell cannot be converted to SdfValue");
    }

    public static MolValue getMolCell(DataCell cell) throws IncompatibleTypeException {
        if (cell.getType().isCompatible(MolValue.class)) {
            return (MolValue) cell;
        } else if (cell.getType().isAdaptable(MolValue.class)) {
            AdapterCell adapterCell = (AdapterCell) cell;

            return adapterCell.getAdapter(MolValue.class);
        }

        throw new IncompatibleTypeException("The cell cannot be converted to MolValue");
    }

    public static StringValue getStringCell(DataCell cell) throws IncompatibleTypeException {
        if (cell.getType().isCompatible(StringValue.class)) {
            return (StringValue) cell;
        } else if (cell.getType().isAdaptable(StringValue.class)) {
            AdapterCell adapterCell = (AdapterCell) cell;

            return adapterCell.getAdapter(StringValue.class);
        }

        throw new IncompatibleTypeException("The cell cannot be converted to StringValue");
    }

}
