package com.actelion.research.knime.utils.dwar;/*
 * @(#)DWARFileHandler.java   16/05/23
 *
 * Copyright (c) 2010-2011 Actelion Pharmaceuticals Ltd.
 *
 *  Gewerbestrasse 16, CH-4123 Allschwil, Switzerland
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information
 *  of Actelion Pharmaceuticals Ltd. ("Confidential Information").  You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Actelion Pharmaceuticals Ltd.
 *
* @author Modest von Korff
 * @version 1.0
 *          07.12.2004 MvK: Start implementation
 *          01 Mar. 2007 MvK: Update for new ode standard
 *          14 Jan. 2011 MvK: id added
 *          24.07.2013 ODEFileHandler --> DWARFileHandler
 *          03.01.2014 Processing empty dwar files without error.
 */

import com.actelion.research.io.StringReadChannel;
import com.actelion.research.util.ErrorHashMap;
//import com.actelion.research.util.ExceptionParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DWARFileHandler implements DWARInterface {
    public static final String SEP_TABLE_HEADER = "\t";
    public static final String VERSION_ODE_2 = "2";
    // Should be the actual version.
    public static final String VERSION_ODE_3_1 = "3.1";
    public static final String PATTERN_VERSION_DWAR_3X = "3.*";
    public static final String VAR_NAME_VERSION_ODE = "version";
    // Used in Oracle HTS DB table
    public static final String HEADER_COOR_ORACLE = "COORDINATES";
    public static final String HEADER_FP_OLD = "fingerprint_1.2.1";
    public static final String TAG_DW_HITLIST_START = "<hitlist data>";
    public static final String TAG_DW_FILEINFO_END = "</datawarrior-fileinfo>";
    public static final String TAG_DW_EXPLANATION_END = "</datawarrior explanation>";
    public static final String TAG_DW_MACROLIST_START = "<datawarrior macroList>";
    public static final String TAG_DW_MACROLIST_END = "</datawarrior macroList>";
    public static final String TAG_DW_PROPERIES_START = "<datawarrior properties>";
    public static final String TAG_DW_COLUMN_PROPERTIES_START = "<column properties>";
    public static final String TAG_DW_COLUMN_PROPERTIES_END = "</column properties>";
    public static final String DW_FILE_INFO = "datawarrior-fileinfo";
    private static final int LIMIT_HEADER_LINES = 1000;
    private static final String[] TAG_DW_PROPERTIES = {TAG_DW_PROPERIES_START, TAG_DW_HITLIST_START, "<column properties>",
            "<detail data>"};
    public static boolean VERBOSE = false;
    private static HashSet<String> hsEndListMolecules;

    //~--- fields -------------------------------------------------------------

    private URL url;
    private DWARHeader header;
    private String versionODE;
    private ErrorHashMap errorHashMap;
    private DWARReaderThread dwarReaderThread;
    private Thread thread;
    private long idRec;

    //~--- constructors -------------------------------------------------------

    public DWARFileHandler(File file) throws NoSuchFieldException, MalformedURLException, IOException {
        this(file.toURI().toURL());
    }

    /**
     * Opens a file
     *
     * @param sFile input
     */
    public DWARFileHandler(String sFile) throws NoSuchFieldException, MalformedURLException, IOException {
        this(new File(sFile));
    }

    public DWARFileHandler(URL url) throws NoSuchFieldException, IOException {
        this.url = url;
        init();
    }

    //~--- methods ------------------------------------------------------------

    public static StringReadChannel skipLinesUntilHeader(URL url, String version) throws IOException, NoSuchFieldException {
        InputStream is = url.openStream();
        StringReadChannel channel = new StringReadChannel(Channels.newChannel(is));

        if (version.matches(PATTERN_VERSION_DWAR_3X)) {
            StringReadChannel.skipUntilLineMatchesRegEx(channel, TAG_DW_FILEINFO_END);

            if (containsLine(url, TAG_DW_EXPLANATION_END, LIMIT_HEADER_LINES)) {
                StringReadChannel.skipUntilLineMatchesRegEx(channel, TAG_DW_EXPLANATION_END);
            }

            if (containsLine(url, TAG_DW_MACROLIST_END, LIMIT_HEADER_LINES)) {
                StringReadChannel.skipUntilLineMatchesRegEx(channel, TAG_DW_MACROLIST_END);                               
            }
                        
            if (containsLine(url, TAG_DW_COLUMN_PROPERTIES_END, LIMIT_HEADER_LINES)) {            
                StringReadChannel.skipUntilLineMatchesRegEx(channel, TAG_DW_COLUMN_PROPERTIES_END);
            }
        } else {
            throw new RuntimeException("Unknown dwar version: " + version + ".");
        }

        // Header line
        // Only line before rows for the old ODE format 2.x
        // channel.readLine();
        return channel;
    }

    //~--- get methods --------------------------------------------------------

    public static boolean isEndOfTableData(String line) {
        if (hsEndListMolecules == null) {
            hsEndListMolecules = new HashSet<String>();

            for (int i = 0; i < TAG_DW_PROPERTIES.length; i++) {
                hsEndListMolecules.add(TAG_DW_PROPERTIES[i]);
            }
        }

        return hsEndListMolecules.contains(line.trim());
    }

    //~--- methods ------------------------------------------------------------

    private static boolean containsLine(URL url, String regex, int limitLines) throws IOException {
        boolean contains = false;
        InputStream is = url.openStream();
        StringReadChannel channel = new StringReadChannel(Channels.newChannel(is));
        int cc = 0;

        while (channel.hasMoreLines()) {
            String line = channel.readLine();

            if (line.matches(regex)) {
                contains = true;

                break;
            }

            cc++;

            if (cc == limitLines) {
                break;
            }
        }

        channel.close();
        is.close();

        return contains;
    }

    private static void readColProperties(URL url, DWARHeader odeHeader) throws IOException {
        if (url == null) {
            throw new NullPointerException("URL is null");
        }

        InputStream is = url.openStream();
        StringReadChannel channel = new StringReadChannel(Channels.newChannel(is));

        try {
            StringReadChannel.skipUntilLineMatchesRegEx(channel, DWARFileHandler.TAG_DW_COLUMN_PROPERTIES_START);
        } catch (NoSuchFieldException e) {

//          NoSuchFieldException ex = new NoSuchFieldException("No col props in dwar file.");
//          ex.printStackTrace();
        }

        DWARHeader.parseColProperties(odeHeader, channel);
        is.close();
        channel.close();
    }

    public void close() {
        if (VERBOSE) {
            System.out.println("finalize()");

            Exception ex = new Exception();
            //String s = ExceptionParser.parse(ex);

            System.out.println(ex.getMessage());
        }

        if (dwarReaderThread != null) {
            dwarReaderThread.setFinalize(true);
        }

        try {
            if (thread != null) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (errorHashMap.hasErrors()) {
            System.err.println(errorHashMap.toString());
        }

        if ((dwarReaderThread != null) && (dwarReaderThread.getErrors().hasErrors())) {
            System.err.println(dwarReaderThread.getErrors().toString());
        }

        if (VERBOSE) {
            System.out.println("finalize() finished.");
        }
    }

    public DWARRecord next() {
        DWARRecord rec = dwarReaderThread.next();

        if (rec == null) {
            System.err.println("DWARFileHandler next() DWARRecord is null!");

            return null;
        }

        rec.setID(idRec++);

        return rec;
    }

    public synchronized void reset() {
        if (VERBOSE) {
            System.out.println("ODEFilehandler reset()");
        }

        idRec = 0;
        close();
        dwarReaderThread = new DWARReaderThread(url, header, versionODE);
        thread = new Thread(dwarReaderThread);
        thread.start();

        if (VERBOSE) {
            System.out.println("ODEFilehandler reset() finished.");
        }
    }

    public void reset(String tag) {
        reset();
    }

    public int size() {
        return header.getDWFileInfo().getRowCount();
    }

    public String toString() {
        return toStringHeader();
    }

    public String toStringHeader() {
        StringBuilder str = new StringBuilder();

        for (int ii = 0; ii < header.size(); ii++) {
            str.append(header.get(ii));

            if (ii < header.size() - 1) {
                str.append(SEP_TABLE_HEADER);
            }
        }

        return str.toString();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Shallow copy
     */
    public DWARHeader getDWARHeader() {
        return header;
    }

    public List<String> getHeader() {
        return new ArrayList<String>(header.get());
    }

    public String getSource() {
        return url.getFile();
    }

    public boolean hasMore() {
        if (dwarReaderThread == null) {
            return false;
        }

        return dwarReaderThread.hasMore();
    }

    //~--- methods ------------------------------------------------------------

    private int countRecords(URL url) throws NoSuchFieldException, IOException {
        int nRecs = 0;
        StringReadChannel channel = skipLinesUntilHeader(url, versionODE);

        channel.readLine();

        String line = "";

        while (channel.hasMoreLines()) {
            line = channel.readLine();

            if (line.matches(TAG_DW_PROPERIES_START)) {
                break;
            }

            nRecs++;
        }

        channel.close();

        return nRecs;
    }

    private void init() throws IOException, NoSuchFieldException {
        errorHashMap = new ErrorHashMap();

        StringReadChannel channel = new StringReadChannel(Channels.newChannel(url.openStream()));
        String line = channel.readLine().trim();

        channel.close();

        if (line.matches(".*" + DW_FILE_INFO + ".*")) {
            initHeaderVersion3_x();
        } else {
            initHeaderVersion2_x();
        }

        for (int i = 0; i < header.size(); i++) {
            String tag = header.get(i);

            if (tag == null) {
                throw new RuntimeException("Header tag is null for url '" + url.toString() + "'.");
            }
        }

        if (header.getDWFileInfo().getRowCount() > 0) {
            dwarReaderThread = new DWARReaderThread(url, header, versionODE);
            thread = new Thread(dwarReaderThread);
            thread.start();
        }
    }

    private void initHeaderVersion2_x() throws IOException {
        InputStream is = url.openStream();
        StringReadChannel channel = new StringReadChannel(Channels.newChannel(is));
        String line = channel.readLine().trim();
        int iCounter = 0;

        versionODE = VERSION_ODE_2;
        header = DWARHeader.getStructureDWARHeader();

        boolean bMolsEnd = false;

        while ((line = channel.readLine()) != null) {
            if (isEndOfTableData(line)) {
                bMolsEnd = true;
            }

            if (!bMolsEnd) {
                iCounter++;

                if ((iCounter % 10000 == 0) && VERBOSE) {
                    System.out.println("DWARFileHandler count " + iCounter);
                }
            } else if (line.matches(TAG_DW_COLUMN_PROPERTIES_START)) {
                DWARHeader.parseColProperties(header, channel);
            }
        }

        header.getDWFileInfo().setRowCount(iCounter);
        is.close();
        channel.close();
    }

    private void initHeaderVersion3_x() throws NoSuchFieldException, IOException {
        versionODE = VERSION_ODE_3_1;

        // Read the table header tags
        // Has to be done first because the order of the tags is of importance.
        String lineHeader = getHeaderLine();
        List<String> liHeader = DWARHeader.parseTableHeader(lineHeader);

        header = new DWARHeader(liHeader);

        DataWarriorFileInfo dwInfo = readDWInfo();

        header.setDWFileInfo(dwInfo);

        if (hasColProperties()) {
            readColProperties(url, header);
        }

        if (header.getDWFileInfo().getRowCount() < 1) {
            int nRecs = countRecords(url);

            header.getDWFileInfo().setRowCount(nRecs);
        }
    }

    private DataWarriorFileInfo readDWInfo() throws IOException, NoSuchFieldException {

        // Read the column property information.
        InputStream is = url.openStream();
        StringReadChannel channel = new StringReadChannel(Channels.newChannel(is));
        String sStartFileInfoRegEx = ".*" + DataWarriorFileInfo.FTAG_DW_FILE_INO + ".*";

        StringReadChannel.skipUntilLineMatchesRegEx(channel, sStartFileInfoRegEx);

        DataWarriorFileInfo dwInfo = new DataWarriorFileInfo(channel);

        is.close();
        channel.close();

        return dwInfo;
    }

    //~--- get methods --------------------------------------------------------

    private String getHeaderLine() throws IOException, NoSuchFieldException {
        String lineHeader = null;
        StringReadChannel channel = skipLinesUntilHeader(url, versionODE);

        lineHeader = channel.readLine();
        channel.close();

        return lineHeader;
    }

    private boolean hasColProperties() throws IOException {
        return containsLine(url, TAG_DW_COLUMN_PROPERTIES_START, LIMIT_HEADER_LINES);
    }
}
