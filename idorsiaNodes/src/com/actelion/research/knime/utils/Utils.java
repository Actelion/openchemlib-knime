package com.actelion.research.knime.utils;

import com.actelion.research.chem.io.DWARFileParser;
import com.actelion.research.chem.io.SDFileParser;

import java.io.File;

public class Utils {

    public static MultiStructureCompoundParserAdapter getCompoundFileParser(File file) {
        if (file.getName().toLowerCase().endsWith(".sdf")) {
            SDFileParser sdFileParser = new SDFileParser(file);
            String[] fieldNames = sdFileParser.getFieldNames();
            sdFileParser.close();
            sdFileParser = new SDFileParser(file, fieldNames);
            return new MultiStructureCompoundParserAdapter(sdFileParser);
        } else if (file.getName().toLowerCase().endsWith(".dwar")) {
            return new MultiStructureCompoundParserAdapter(new DWARFileParser(file));
        }
        return null;
    }
}
