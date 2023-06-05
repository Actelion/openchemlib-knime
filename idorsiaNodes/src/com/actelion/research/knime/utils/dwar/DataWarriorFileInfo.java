package com.actelion.research.knime.utils.dwar;


import com.actelion.research.io.StringReadChannel;
import com.actelion.research.util.IO;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DataWarriorFileInfo {

    public static final int INIT_ROWCOUNT = -1;

    public static final String DEFAULT_APPLICATION = "ODEFileHandler";


    public static final String FTAG_DW_FILE_INO = "datawarrior-fileinfo";

    public static final String FTAG_VAR_VERSION = "version";

    public static final String FTAG_VAR_ROWCOUNT = "rowcount";

    public static final String FTAG_VAR_APPLICATION = "application";

    private static final String ROWCOUNT_PLACEHOLDER = "-000000001";

    private String version;

    private int rowcount;

    private String application;

    public DataWarriorFileInfo() {
        init();
    }

    public DataWarriorFileInfo(InputStream in) throws Exception {
        init();

        String line = IO.readLine(in);
        String sEndRegEx = ".*/" + FTAG_DW_FILE_INO + ".*";
        while (!line.matches(sEndRegEx)) {
            extract(line);
            line = IO.readLine(in);
        }
    }

    public DataWarriorFileInfo(StringReadChannel in) throws IOException {
        init();

        String line = in.readLine();
        String sEndRegEx = ".*/" + FTAG_DW_FILE_INO + ".*";
        while (!line.matches(sEndRegEx)) {
            extract(line);
            line = in.readLine();
        }
    }

    public static String toStringDefault() {
        StringBuilder sb = new StringBuilder();

        sb.append("<datawarrior-fileinfo>" + "\n");
        sb.append("<version=\"" + DWARFileHandler.VERSION_ODE_3_1 + "\">" + "\n");
        // sb.append("<rowcount=\"-1\">"+"\n");
        sb.append("<" + FTAG_VAR_APPLICATION + "=\"" + DEFAULT_APPLICATION + "\">" + "\n");
        sb.append("</datawarrior-fileinfo>");

        return sb.toString();
    }

    public void copy(DataWarriorFileInfo d) {
        version = d.version;
        rowcount = d.rowcount;
        application = d.application;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public int getRowCount() {
        return rowcount;
    }

    public void setRowCount(int rowCount) {
        rowcount = rowCount;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void increaseRowCount() {
        rowcount++;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("<datawarrior-fileinfo>" + "\n");
        sb.append("<" + FTAG_VAR_VERSION + "=\"" + version + "\">" + "\n");

        // The real row count is written into the file after adding all records.
        // See ODEFileWriter.close().

        int lenFormat = ROWCOUNT_PLACEHOLDER.length();
        String nfPattern = "";
        for (int i = 0; i < lenFormat; i++) {
            nfPattern += "0";
        }
        NumberFormat nf = new DecimalFormat(nfPattern);
        String sRowCount = nf.format(rowcount);


        sb.append("<" + FTAG_VAR_ROWCOUNT + "=\"" + sRowCount + "\">" + "\n");

        sb.append("<" + FTAG_VAR_APPLICATION + "=\"" + application + "\">" + "\n");
        sb.append("</datawarrior-fileinfo>");

        return sb.toString();
    }

    private void init() {
        version = DWARFileHandler.VERSION_ODE_3_1;

        application = DEFAULT_APPLICATION;

        rowcount = INIT_ROWCOUNT;
    }

    private void extract(String line) throws IOException {
        if (line.matches(".*" + FTAG_VAR_VERSION + ".*")) {
            String nameRowVersion = DWARHeader.readVarName(line);
            if (!nameRowVersion.equals(DWARFileHandler.VAR_NAME_VERSION_ODE)) {
                throw new IOException("Can not read version.");
            }
            version = DWARHeader.readContent(line);

        } else if (line.matches(".*" + FTAG_VAR_ROWCOUNT + ".*")) {
            String sRowCount = DWARHeader.readContent(line);
            rowcount = Integer.parseInt(sRowCount);
        } else if (line.matches(".*" + FTAG_VAR_APPLICATION + ".*")) {
            application = DWARHeader.readContent(line);
        }
    }


}
