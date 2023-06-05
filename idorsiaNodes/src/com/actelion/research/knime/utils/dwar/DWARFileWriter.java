package com.actelion.research.knime.utils.dwar;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.util.ConstantsDWAR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Title: DWARFileWriter </p>
 * <p></p>
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 *
 * @author Modest von Korff
 * @version 1.3
 *          19.09.2005 MvK: Start implementation
 *          01 Mar. 2007 MvK: Update for new ode standard
 *          02 Mar. 2010 MvK: IDWARFileWriter interface added
 *          24.07.2013 ODEFileWriter --> DWARFileWriter
 */

public class DWARFileWriter implements IDWARWriter {

    // Molecule name
    public static final String NAME = "Name";

    public static final int MAX_LEN_STRING = 15000000;

    private static final int WIDTH_COL_STRUCTURE = 250;

    private static final int HEIGHT_ROW = 120;

    private FileChannel outChannel;

    private FileOutputStream fout;

    private File fiDWAR;

    private DWARHeader dwarHeader;

    private String dwarFooter;

    int rowcounter;

    /**
     * Don't forget to set the number of rows with <code> setRowCount(int records2add) </code> after calling the constructor. Or use <code> resetRowCount() </code>
     *
     * @param file
     * @throws IOException
     */
    public DWARFileWriter(File file) throws IOException {
        this(file, new ArrayList<String>());
    }

    /**
     * Don't forget to set the number of rows with <code> setRowCount(int records2add) </code> after calling the constructor. Or use <code> resetRowCount() </code>
     *
     * @param file
     * @param headerFields
     * @throws IOException
     */
    public DWARFileWriter(File file, List<String> headerFields) throws IOException {

        DWARHeader header = DWARHeader.getStructureDWARHeader();

        for (String tag : headerFields) {
            header.add(tag);
        }

        init(file, header);
    }

    /**
     * Don't forget to set the number of rows with <code> setRowCount(int records2add) </code> after calling the constructor. Or use <code> resetRowCount() </code>
     *
     * @param file
     * @param header the number of records is copied from the header.
     * @throws IOException
     */
    public DWARFileWriter(File file, DWARHeader header) throws IOException {
        init(file, header);
    }


    private void init(File file, DWARHeader header) throws IOException {
        this.fiDWAR = file;

        this.dwarHeader = new DWARHeader(header);

        rowcounter = 0;


        if (file.exists()) {

            if (file.isDirectory()) {
                String e = "This is a directory '" + file.getAbsolutePath() + "' but it has to be a file.\n";
                throw new IOException(e);
            }

            if (!file.delete()) {
                String e = "Not possible to delete old file '" + file.getAbsolutePath() + "'.\n";
                throw new IOException(e);
            }
        }

        fout = new FileOutputStream(file);

        outChannel = fout.getChannel();

        String str = this.dwarHeader.toStringDWAR() + "\n";

        write2Channel(str.toString());

    }

    public DWARHeader getODEHeader() {
        return dwarHeader;
    }

    public void addWithQueryId(DWARRecord record) {
        write(record);
    }

    public void write(DWARRecord record) {

        try {
            StringBuilder sb = new StringBuilder();
            if (record == null)
                System.err.println("DWARFileWriter: record is null");

            for (int i = 0; i < dwarHeader.size(); i++) {

                if (dwarHeader.isWritable(i)) {

                    String head = dwarHeader.get(i);

                    if (head == null)
                        System.err.println("DWARFileWriter head " + i + " is null");


                    if (record.getAsString(head) != null) {
                        sb.append(record.getODEFormat(head));

                        if (i < dwarHeader.size() - 1)
                            sb.append(DWARFileHandler.SEP_TABLE_HEADER);
                    } else {
                        sb.append(DWARFileHandler.SEP_TABLE_HEADER);
                    }
                }
            }

            sb.append("\n");

            write2Channel(sb.toString());

            rowcounter++;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resets the row count to DataWarriorFileInfo.INIT_ROWCOUNT
     */
    public void resetRowCount() {
        dwarHeader.getDWFileInfo().setRowCount(DataWarriorFileInfo.INIT_ROWCOUNT);
    }

    /**
     * May be set before a record is added.
     *
     * @param records2add
     */
    public void setRowCount(int records2add) {
        dwarHeader.getDWFileInfo().setRowCount(records2add);
    }

    private void write2Channel(String str) throws IOException {

        int len = str.length();

        String substring = "";

        for (int i = 0; i < len; i += MAX_LEN_STRING) {

            try {
                int start = i;

                int end = Math.min(i + MAX_LEN_STRING, len);

                substring = str.substring(start, end);

                byte[] bytes = substring.getBytes();

                ByteBuffer buf = ByteBuffer.allocate(bytes.length);

                buf.put(bytes);

                buf.flip();

                outChannel.write(buf);
            } catch (Exception e) {

                System.err.println("substring " + substring.length() + ".");

                e.printStackTrace();

                outChannel.force(true);
            }

        }

    }

    private List<String> getHeaderList() {
        return dwarHeader.get();
    }

    public DWARHeader getHeader() {
        return dwarHeader;
    }


    public void close() {

        try {

            if (dwarFooter == null) {
                dwarFooter = getPropertiesStandard();
            }

            write2Channel(dwarFooter);

            outChannel.force(true);

            outChannel.close();

            // fout.close();

            if (dwarHeader.getDWFileInfo().getRowCount() != rowcounter) {
                writeRowCount(fiDWAR, rowcounter);

//    			IOException ex = new IOException("Number of added records (" + rowcounter + ") differs from given record number(" + odeHeader.getDWFileInfo().getRowCount() + ").");
//    			ex.printStackTrace();
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeRowCount(File fiDWAR, int rowcount) throws IOException {

        int nMaxChars2Read = 1000;

        FileReader fr = new FileReader(fiDWAR);

        boolean read = true;
        boolean matchRowCount = false;
        boolean startedRowCountIndex = false;

        int ccChars = 0;

        StringBuilder sb = new StringBuilder();

        //
        // Find the start and the end of the row count integer in the dwar file header.
        //
        int startRowCountIndex = -1;
        int endRowCountIndex = -1;
        while (read) {
            char c = (char) fr.read();

            if (c == '\n') {
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }

            if (sb.length() >= DataWarriorFileInfo.FTAG_VAR_ROWCOUNT.length()) {
                String pattern = ".*" + DataWarriorFileInfo.FTAG_VAR_ROWCOUNT + ".*";

                if (sb.toString().matches(pattern)) {
                    matchRowCount = true;
                }
            }

            if (matchRowCount && startedRowCountIndex) {
                if (c == '"') {
                    endRowCountIndex = ccChars;
                    read = false;
                }
            } else if (matchRowCount) {
                if (c == '"') {
                    startRowCountIndex = ccChars + 1;
                    startedRowCountIndex = true;
                }
            }

            ccChars++;

            if (ccChars > nMaxChars2Read) {
                read = false;
            }
        }

        fr.close();

        if (!startedRowCountIndex)
            return;


        //
        // Generate a string with the correct format and write it into the file header at the correct position.
        //
        String formatPattern = "";

        int lenFormat = endRowCountIndex - startRowCountIndex;

        if (lenFormat < Integer.toString(rowcount).length()) {
            System.err.println("ODEFileWriter not enough space to write rowcount " + rowcount + " rowcount replaced by " + DataWarriorFileInfo.INIT_ROWCOUNT + ".");
            rowcount = DataWarriorFileInfo.INIT_ROWCOUNT;
        }

        for (int i = 0; i < lenFormat; i++) {
            formatPattern += "0";
        }

        NumberFormat nf = new DecimalFormat(formatPattern);

        RandomAccessFile raf = new RandomAccessFile(fiDWAR, "rw");

        raf.seek(startRowCountIndex);

        raf.writeBytes(nf.format(rowcount));

        raf.close();

    }

    private String getPropertiesStandard() {
        StringBuilder sb = new StringBuilder();

        String tagStructure = "";
        int nStructures = 0;
        List<String> liHeader = dwarHeader.get();
        for (int i = 0; i < liHeader.size(); i++) {
            if (dwarHeader.isStructure(liHeader.get(i))) {
                if (nStructures == 0) {
                    tagStructure = liHeader.get(i);
                }
                nStructures++;
            }
        }

        sb.append("<datawarrior properties>" + "\n");

        // Excluded 20.11.2013 MvK

//		DescriptorInfo [] arr = DescriptorConstants.DESCRIPTOR_EXTENDED_LIST;
//		
//		for (int i = 0; i < liHeader.size(); i++) {
//			String tag = liHeader.get(i);
//			for (int j = 0; j < arr.length; j++) {
//				if(tag.startsWith(arr[j].shortName)){
//					String str = "<columnVisibility_Table_" + tag + "=\"false\">";
//					sb.append(str + "\n");
//				}
//				
//				if(tag.equals(DWARHeader.TAG_MOLECULE)){
//					String str = "<columnVisibility_Table_" + tag + "=\"false\">";
//					sb.append(str + "\n");
//				}
//			}
//		}

        for (int i = 0; i < liHeader.size(); i++) {

            if (dwarHeader.isStructure(liHeader.get(i))) {
                sb.append("<columnWidth_Table_" + liHeader.get(i) + "=\"" + WIDTH_COL_STRUCTURE + "\">" + "\n");
            }
        }

        sb.append("<detailView=\"height[Data]=0.5;height[Structure]=0.5\">" + "\n");

//		if(nStructures>1)
        sb.append("<mainView=\"Structures of " + tagStructure + "\">" + "\n");
//		else
//			sb.append("<mainView=\"Structures\">"+"\n");

        sb.append("<mainViewCount=\"4\">" + "\n");
        sb.append("<mainViewDockInfo0=\"root\">" + "\n");
        sb.append("<mainViewDockInfo1=\"Table	center\">" + "\n");
        sb.append("<mainViewDockInfo2=\"2D View	center\">" + "\n");
        sb.append("<mainViewDockInfo3=\"3D View	center\">" + "\n");

        sb.append("<mainViewName0=\"Table\">" + "\n");
        sb.append("<mainViewName1=\"2D View\">" + "\n");
        sb.append("<mainViewName2=\"3D View\">" + "\n");


//		if(nStructures>1)
        sb.append("<mainViewName3=\"Structures of " + tagStructure + "\">" + "\n");
//		else
//			sb.append("<mainViewName3=\"Structures\">"+"\n");

        sb.append("<mainViewType0=\"tableView\">" + "\n");
        sb.append("<mainViewType1=\"2Dview\">" + "\n");
        sb.append("<mainViewType2=\"3Dview\">" + "\n");
        sb.append("<mainViewType3=\"structureView\">" + "\n");

//		if(nStructures>1) {
        sb.append("<structureGridColumn_Structures of " + tagStructure + "=\"" + tagStructure + "\">" + "\n");
        sb.append("<structureGridColumns_Structures of " + tagStructure + "=\"6\">" + "\n");
//		} else {
//			sb.append("<structureGridColumn_Structures=\""+tagStructure+"\">"+"\n");
//			sb.append("<structureGridColumns_Structures=\"6\">"+"\n");
//		}

        sb.append("<rowHeight_Table=\"" + HEIGHT_ROW + "\">" + "\n");

        sb.append("</datawarrior properties>" + "\n");

        return sb.toString();
    }

    /**
     * Generates the properties for a 2D view in the dwar file.
     *
     * @param tag1
     * @param tag2
     */
    public void setPropertiesMainView2DChartModeCountBars(String tag1, String tag2) {

        StringBuilder sb = new StringBuilder();
        sb.append("<datawarrior properties>" + "\n");

        sb.append("<axisColumn_2D View_0=\"" + tag1 + "\">" + "\n");
        sb.append("<axisColumn_2D View_1=\"" + tag2 + "\">" + "\n");
        sb.append("<chartMode_2D View=\"count\">" + "\n");
        sb.append("<chartMode_2D View=\"bars\">" + "\n");
        sb.append("<mainView=\"2D View\">" + "\n");
        sb.append("<mainViewCount=\"4\">" + "\n");
        sb.append("<mainViewDockInfo0=\"root\">" + "\n");
        sb.append("<mainViewDockInfo1=\"Table	center\">" + "\n");
        sb.append("<mainViewDockInfo2=\"2D View	center\">" + "\n");
        sb.append("<mainViewName0=\"Table\">" + "\n");
        sb.append("<mainViewName1=\"2D View\">" + "\n");
        sb.append("<mainViewName2=\"3D View\">" + "\n");
        sb.append("<mainViewName3=\"Structures\">" + "\n");
        sb.append("<mainViewType0=\"tableView\">" + "\n");
        sb.append("<mainViewType1=\"2Dview\">" + "\n");
        sb.append("<mainViewType2=\"3Dview\">" + "\n");
        sb.append("<mainViewType3=\"structureView\">" + "\n");
        sb.append("<preferHistogram_2D View=\"true\">" + "\n");
        sb.append("<rowHeight_Table=\"" + HEIGHT_ROW + "\">" + "\n");

        sb.append("</datawarrior properties>" + "\n");

        dwarFooter = sb.toString();

    }

    private String toStringHeader() {
        StringBuilder sb = new StringBuilder();

        List<String> liHeader = getHeaderList();
        int cc = 0;
        for (String tag : liHeader) {
            sb.append(tag);
            if (cc < liHeader.size() - 1)
                sb.append(DWARFileHandler.SEP_TABLE_HEADER);
            cc++;
        }

        return sb.toString();
    }

    public String toString() {
        String str = "";

        str = toStringHeader();

        return str;
    }

    public static final void write(File fiOut, List<DWARRecord> liDWAR, DWARHeader header) throws IOException {

        if (liDWAR.size() == 0)
            throw new RuntimeException("No dwar records in list.");

        DWARFileWriter writer = null;
        if (header == null)
            writer = new DWARFileWriter(fiOut, liDWAR.get(0).getHeaderList());
        else
            writer = new DWARFileWriter(fiOut, header);


        for (DWARRecord rec : liDWAR) {
            writer.write(rec);
        }

        writer.close();
    }

    public static final void write(File fiOut, List<StereoMolecule> li) throws IOException {

        DWARHeader header = DWARHeader.getStructureDWARHeader();

        boolean name = false;
        for (StereoMolecule mol : li) {
            if (mol.getName() != null && mol.getName().length() > 0) {
                name = true;
                break;
            }
        }

        if (name) {
            header.add(NAME);
        }


        DWARFileWriter writer = new DWARFileWriter(fiOut, header);

        for (StereoMolecule mol : li) {

            DWARRecord rec = new DWARRecord(header);

            Canonizer can = new Canonizer(mol);

            rec.setIdCode(can.getIDCode());

            rec.setCoordinates(can.getEncodedCoordinates());

            if (name) {

                if (mol.getName() != null && mol.getName().length() > 0) {
                    rec.addOrReplaceField(NAME, mol.getName());
                }

            }

            writer.write(rec);
        }

        writer.close();
    }

    public static final void writeListIdCode(File fiOut, List<String> liIdCode) throws IOException {

        DWARHeader header = DWARHeader.getStructureDWARHeader();

        DWARFileWriter writer = new DWARFileWriter(fiOut, header);

        for (String idcode : liIdCode) {

            DWARRecord rec = new DWARRecord(header);

            rec.setIdCode(idcode);

            writer.write(rec);
        }

        writer.close();
    }



    /**
     * Writes a single molecule into a tmp file.
     *
     * @param dir
     * @param idcode
     * @param coordinates
     * @return
     * @throws IOException
     */

    public static File write(File dir, String idcode, String coordinates) throws IOException {

        File fi = File.createTempFile("tmp", ConstantsDWAR.DWAR_EXTENSION, dir);

        DWARHeader header = DWARHeader.getStructureDWARHeader();

        DWARFileWriter fw = new DWARFileWriter(fi, header);

        DWARRecord rec = new DWARRecord(header);

        rec.setIdCode(idcode);
        rec.setCoordinates(coordinates);

        fw.write(rec);

        fw.close();

        return fi;

    }

    public int getAdded() {
        return rowcounter;
    }


    public int getRowcounter() {
        return rowcounter;
    }

    /* (non-Javadoc)
     * @see com.actelion.research.chem.dwar.IDWARWriter#isTag(java.lang.String)
     */
    @Override
    public boolean isTag(String tag) {
        return dwarHeader.contains(tag);
    }

}
