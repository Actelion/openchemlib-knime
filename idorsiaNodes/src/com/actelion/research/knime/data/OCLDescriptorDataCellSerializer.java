package com.actelion.research.knime.data;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

public class OCLDescriptorDataCellSerializer implements DataCellSerializer<OCLDescriptorDataCell> {
    @Override
    public OCLDescriptorDataCell deserialize(DataCellDataInput input) throws IOException {
        String descriptorShortName = input.readLine();
        String descriptor = input.readLine();
        OCLDescriptorDataCell oclDescriptorDataCell = new OCLDescriptorDataCell(descriptorShortName, descriptor.getBytes());
        return oclDescriptorDataCell;
    }

    @Override
    public void serialize(OCLDescriptorDataCell oclMoleculeDataCell, DataCellDataOutput output) throws IOException {
        byte[] ldelim = "\n".getBytes();
        output.write(oclMoleculeDataCell.getDescriptorInfo().shortName.getBytes());
        output.write(ldelim);
        output.write(oclMoleculeDataCell.getEncodedDescriptor().getBytes());
        output.write(ldelim);
    }
}
