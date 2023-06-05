package com.actelion.research.knime.nodes.io;

import com.actelion.research.knime.utils.SpecHelper;

import org.knime.core.data.DataColumnSpec;

public enum FileFormat {
    DWAR("DataWarrior file", ".DWAR file", new String[]{"dwar"}), SDF_V2("SDF Version 2", ".SDF file", new String[]{"sdf"}), SDF_V3("SDF Version 3", ".SDF file", new String[]{"sdf"});

    private final String fileDescription;
    private final String[] extensions;
    private String description;

    FileFormat(String description, String fileDescription, String[] extensions) {
        this.description = description;
        this.fileDescription = fileDescription;
        this.extensions = extensions;
    }

    public static FileFormat fromString(String s) {
        for (FileFormat fileFormat : FileFormat.values()) {
            if (fileFormat.getDescription().equalsIgnoreCase(s)) {
                return fileFormat;
            }
        }
        return DWAR;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public boolean supports(DataColumnSpec columnSpec) {
        return this == DWAR || !SpecHelper.isDescriptorSpec(columnSpec);
    }
}
