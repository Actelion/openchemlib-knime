package com.actelion.research.knime.data;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.utils.DescriptorHelpers;

import org.knime.core.data.RowKey;

public class DescriptorRow {
    private String rowKey;
    private DescriptorInfo descriptorInfo;
    private byte[] descriptor;

    public DescriptorRow(RowKey rowKey, DescriptorInfo descriptorInfo, byte[] descriptor) {
        this.rowKey = rowKey.getString();
        this.descriptorInfo = descriptorInfo;
        this.descriptor = descriptor;
    }

    public String getRowKey() {
        return rowKey;
    }

    public Object getDescriptor() {
        return DescriptorHelpers.decode(new String(descriptor), descriptorInfo);
    }
}