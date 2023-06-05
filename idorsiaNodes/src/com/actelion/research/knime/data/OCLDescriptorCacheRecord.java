package com.actelion.research.knime.data;

import java.io.Serializable;

public class OCLDescriptorCacheRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String shortDescriptorName;
    private String encodedDescriptor;
    private String idCode;

    public String getShortDescriptorName() {
        return shortDescriptorName;
    }

    public void setShortDescriptorName(String shortDescriptorName) {
        this.shortDescriptorName = shortDescriptorName;
    }

    public String getEncodedDescriptor() {
        return encodedDescriptor;
    }

    public void setEncodedDescriptor(String encodedDescriptor) {
        this.encodedDescriptor = encodedDescriptor;
    }

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }
}
