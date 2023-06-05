package com.actelion.research.knime.utils.dwar;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHandlerFFP512;
import com.actelion.research.chem.descriptor.DescriptorHandlerStandard2DFactory;
import com.actelion.research.io.StringReadChannel;
import com.actelion.research.util.ConstantsDWAR;
//import com.actelion.research.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: DWARHeader </p>
 * <p></p>
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 *
 * @author Modest von Korff
 * @version 2.0
 *          2006 MvK: Start implementation
 *          01 Mar. 2007 MvK: Update for new ode standard
 *          07 May 2009 MvK: Updates for adding descriptor info.
 *          24.07.2013 ODEHeader --> DWARHeader
 */


public class DWARHeader implements DescriptorConstants {

    /**
     * If this tag is given the ODERecord will return a StereoMolecule for it.
     * Should be used with the <code>COL_PROP_DO_NOT_WRITE</code> property.
     */
    public static final String TAG_MOLECULE = "StereoMolecule";

    public static final String TAG_COLOR_INFO = "colorInfo";

    public static final String ACTELION_NO = "Actelion No";


    /**
     * This are the property tags.
     */

    /**
     * If the column property special type is given the column is not shown in the DataWarrior.
     */
    public static final String COLPROP_SPTYPE = "specialType";

    public static final String COLPROP_PARENT = "parent";

    public static final String COLPROP_WRITABLE = "writable";

    public static final String COLPROP_VERSION = "version";


    /**
     * This are the attributes for the property tags.
     */
    public static final String SPTYPE_IDCODE = "idcode";

    public static final String SPTYPE_COORD2D = "idcoordinates2D";

    public static final String SPTYPE_COORD3D = "idcoordinates3D";

    public static final String SPTYPE_FRAGFP = DescriptorConstants.DESCRIPTOR_FFP512.shortName;

    public static final String SPTYPE_COLOR_INFO = "atomColorInfo";

    public static final String SPTYPE_COLOR_INTERNAL = "internal";


    public static final String PARENT_STRUCTURE = ConstantsDWAR.TAG_IDCODE2;


    public static final String COL_PROP_ORIG_COL_NAME = "columnName";

    public static final String COL_PROP_VAR_PROP = "columnProperty";

    public static final String COL_PROP_DISPLAYABLE = "displayable";

    public static final String COL_PROP_DO_NOT_WRITE = "do_not_write";

    private ConcurrentHashMap<String, DWARHeaderTag> hmSynonym_Header;

    // The integer is the index for the order in the header line.

    private ConcurrentHashMap<String, Integer> hmHeaderTag_Index;

    private ConcurrentHashMap<Integer, DWARHeaderTag> hmIndex_Header;

    private int indexCounter;

    private DataWarriorFileInfo dwFileInfo;

    // Name of header tag for toCompare function in DWARRecord.
    private String mTagCompare;

    protected int mCol2Compare;

    private IDCodeParser parser;

    public DWARHeader() {
        init();
    }


    /**
     * Deep copy constructor.
     *
     * @param h
     */
    public DWARHeader(DWARHeader h) {
        init();
        copy(h);
    }

    public DWARHeader(String[] arrHeader) {
        List<String> liHeader = new ArrayList<String>();
        for (int i = 0; i < arrHeader.length; i++) {
            liHeader.add(arrHeader[i]);
        }
        init();
        add(liHeader);
    }

    public DWARHeader(List<String> liHeader) {
        init();
        add(liHeader);
    }

    /**
     * Checks for descriptor. If the tag is a descriptor name the property 'structure' is added.
     *
     * @param tag
     */
    public void add(String tag) {

        if (tag == null) {
            throw new RuntimeException("Given header tag is null!");
        }

        if (contains(tag))
            return;

        DescriptorHandler dh = DescriptorHandlerStandard2DFactory.getFactory().create(tag);

        if (dh != null) {

            DWARHeaderTag header = new DWARHeaderTag(tag);

            add(header);

            // String shortName = dh.getInfo().shortName;

            addProperty(tag, COLPROP_SPTYPE, tag);

            addProperty(tag, COLPROP_VERSION, dh.getVersion());

            addProperty(tag, COLPROP_PARENT, PARENT_STRUCTURE);

        } else if (tag.equals(SPTYPE_IDCODE) || tag.equals(ConstantsDWAR.TAG_IDCODE2)) {

            DWARHeaderTag header = new DWARHeaderTag(PARENT_STRUCTURE);
            add(header);
            try {
                addProperty(PARENT_STRUCTURE, COLPROP_SPTYPE, SPTYPE_IDCODE);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (tag.equals(SPTYPE_COORD2D) || tag.equals(SPTYPE_COORD3D)) {
            DWARHeaderTag header = new DWARHeaderTag(tag);
            add(header);
            try {
                addProperty(tag, COLPROP_PARENT, PARENT_STRUCTURE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            DWARHeaderTag header = new DWARHeaderTag(tag);
            add(header);

        }
    }

    /**
     * Color info for atoms in DWAR files.
     * colorInfo
     *
     * @param tagColor
     */
    public void addColorHeader(String tagColor, String tagStructure) {
        DWARHeaderTag header = new DWARHeaderTag(tagColor);
        add(header);

        addProperty(tagColor, COLPROP_SPTYPE, SPTYPE_COLOR_INFO);

        addProperty(tagColor, COLPROP_PARENT, tagStructure);

    }

    public void addColorHeader(String tag) {
        DWARHeaderTag header = new DWARHeaderTag(tag);
        add(header);

        addProperty(tag, COLPROP_SPTYPE, SPTYPE_COLOR_INFO);

        addProperty(tag, COLPROP_PARENT, PARENT_STRUCTURE);

    }

    /**
     * Is only added if not already available.
     *
     * @param tag
     */
    public void addStructureHeader(String tag) {
        DWARHeaderTag header = new DWARHeaderTag(tag);
        add(header);

        addProperty(tag, DWARHeader.COLPROP_SPTYPE, DWARHeader.SPTYPE_IDCODE);

    }

    public void addHeaderNonWritable(String tag) {
        DWARHeaderTag header = new DWARHeaderTag(tag);
        add(header);

        addProperty(tag, COLPROP_WRITABLE, COL_PROP_DO_NOT_WRITE);

    }

    public void addCoordinates2DHeader(String tagStructure, String tagCoordinates) {
        DWARHeaderTag header = new DWARHeaderTag(tagCoordinates);
        add(header);

        addProperty(tagCoordinates, DWARHeader.COLPROP_SPTYPE, DWARHeader.SPTYPE_COORD2D);
        addProperty(tagCoordinates, DWARHeader.COLPROP_PARENT, tagStructure);
    }

    public void addCoordinates3DHeader(String tagStructure, String tagCoordinates) {
        DWARHeaderTag header = new DWARHeaderTag(tagCoordinates);
        add(header);

        addProperty(tagCoordinates, DWARHeader.COLPROP_SPTYPE, DWARHeader.SPTYPE_COORD3D);
        addProperty(tagCoordinates, DWARHeader.COLPROP_PARENT, tagStructure);
    }

    public void addSynonym(String tag, String synonym) {
        DWARHeaderTag header = hmSynonym_Header.get(tag);
        if (header != null && hmSynonym_Header.get(synonym) == null)
            hmSynonym_Header.put(synonym, header);
    }

    public void add(DWARHeaderTag header) {
        if (hmHeaderTag_Index.get(header.get()) != null) {
            return;
        }

        header.setIndex(indexCounter);

        hmHeaderTag_Index.put(header.get(), indexCounter);

        hmIndex_Header.put(indexCounter, header);

        hmSynonym_Header.put(header.get(), header);

        indexCounter++;
    }

    public String getTagCoordinates3D(String tagStructure) {

        List<DWARHeaderTag> liHeader = new ArrayList<DWARHeaderTag>(hmIndex_Header.values());

        String tag = "";

        for (DWARHeaderTag header : liHeader) {

            String value = header.getProperty(COLPROP_SPTYPE);

            if (value != null && value.equals(SPTYPE_COORD3D)) {
                tag = header.get();
            }
        }

        return tag;
    }

//	public String getTagCoordinates2D(String tagStructure){
//		
//		List<DWARHeaderTag> liHeader = new ArrayList<DWARHeaderTag>(hmIndex_Header.values());
//		
//		String tag= "";
//		
//		for (DWARHeaderTag header : liHeader) {
//			
//			String value = header.getProperty(COLPROP_SPTYPE);
//			
//			if(value!=null && value.equals(SPTYPE_COORD2D)) {
//				tag = header.get();
//			}
//		}
//		
//		return tag;
//	}

    /**
     * @return first tag with special property idcode.
     */
    public String getTagIdCode() {

        List<DWARHeaderTag> liHeader = new ArrayList<DWARHeaderTag>(hmIndex_Header.values());

        String tag = "";

        for (DWARHeaderTag header : liHeader) {

            String value = header.getProperty(COLPROP_SPTYPE);

            if (value != null && value.equals(SPTYPE_IDCODE)) {
                tag = header.get();
                break;
            }
        }

        return tag;
    }

    public List<String> getAllTagsIdCode() {

        List<String> liTagIdCode = new ArrayList<String>();

        List<DWARHeaderTag> liHeader = new ArrayList<DWARHeaderTag>(hmIndex_Header.values());

        for (DWARHeaderTag header : liHeader) {

            String value = header.getProperty(COLPROP_SPTYPE);

            if (value != null && value.equals(SPTYPE_IDCODE)) {
                liTagIdCode.add(header.get());
            }
        }

        return liTagIdCode;
    }

    /**
     * Do not remove a tag if the header is still pointing to a DWAR record.
     * This will end in a mess. Create a new header via the copy constructor and remove the tags there.
     *
     * @param tag
     */
    public void remove(String tag) {

        if (!contains(tag))
            return;

        int indexRemoved = hmHeaderTag_Index.get(tag);

        hmHeaderTag_Index.remove(tag);

        hmIndex_Header.remove(indexRemoved);

        List<String> liKeyString = new ArrayList<String>(hmHeaderTag_Index.keySet());
        for (String key : liKeyString) {
            int ind = hmHeaderTag_Index.get(key);
            if (ind > indexRemoved) {
                hmHeaderTag_Index.remove(key);
                ind--;
                hmHeaderTag_Index.put(key, ind);
            }
        }

        List<Integer> liKeyInteger = new ArrayList<Integer>(hmIndex_Header.keySet());
        Collections.sort(liKeyInteger);

        for (Integer key : liKeyInteger) {
            if (key > indexRemoved) {
                DWARHeaderTag h = hmIndex_Header.get(key);
                hmIndex_Header.remove(key);
                key--;
                hmIndex_Header.put(key, h);
            }
        }

        List<String> liKeys = new ArrayList<String>(hmSynonym_Header.keySet());
        for (String key : liKeys) {
            DWARHeaderTag h = hmSynonym_Header.get(key);
            if (h.get().equals(tag)) {
                hmSynonym_Header.remove(key);
            }

        }

        indexCounter--;
    }

    /**
     * @param tag      column header tag
     * @param property tag for the property
     * @param value    property attribute
     */
    public void addProperty(String tag, String property, String value) {
        if (!contains(tag)) {
            throw new RuntimeException("Header tag " + tag + " not included. Tag has to be added first");
        }

        DWARHeaderTag header = null;
        try {
            Integer index = hmHeaderTag_Index.get(tag);
            if (index != null)
                header = hmIndex_Header.get(hmHeaderTag_Index.get(tag));

        } catch (Exception ex) {
            String e = "Error for tag " + tag + " property " + property + " value " + value + ".";
            throw new RuntimeException(e);
        }
        header.addProperty(property, value);

    }

    /**
     * Facultative add.
     *
     * @param liHeader
     */
    public void add(List<String> liHeader) {
        for (String tag : liHeader) {
            if (!isSynonym((tag)))
                add(tag);
        }
    }

    public boolean contains(String header) {
        if (get(header) == -1)
            return false;
        else
            return true;
    }

    public void copy(DWARHeader h) {

        hmSynonym_Header.clear();
        List<String> liHeaderSyn = new ArrayList<String>(h.hmSynonym_Header.keySet());
        for (String key : liHeaderSyn) {
            DWARHeaderTag header = new DWARHeaderTag();
            header.copy(h.hmSynonym_Header.get(key));
            hmSynonym_Header.put(key, header);
        }

        hmHeaderTag_Index.clear();
        List<String> liHeader = new ArrayList<String>(h.hmHeaderTag_Index.keySet());
        for (String key : liHeader) {
            hmHeaderTag_Index.put(key, h.hmHeaderTag_Index.get(key));
        }


        hmIndex_Header.clear();
        List<Integer> liIndex = new ArrayList<Integer>(h.hmIndex_Header.keySet());
        for (Integer key : liIndex) {
            DWARHeaderTag header = new DWARHeaderTag();
            header.copy(h.hmIndex_Header.get(key));
            hmIndex_Header.put(key, header);
        }

        indexCounter = h.indexCounter;

        dwFileInfo.copy(h.dwFileInfo);

    }

    public void add(DWARHeader h) {

        List<DWARHeaderTag> li = new ArrayList<DWARHeaderTag>(h.hmIndex_Header.values());

        DWARHeaderTag.sortWithIndex(li);

        for (DWARHeaderTag header : li) {
            add(header);
        }
    }

    private void init() {

        hmHeaderTag_Index = new ConcurrentHashMap<String, Integer>();

        hmIndex_Header = new ConcurrentHashMap<Integer, DWARHeaderTag>();

        hmSynonym_Header = new ConcurrentHashMap<String, DWARHeaderTag>();

        indexCounter = 0;

        dwFileInfo = new DataWarriorFileInfo();

        mTagCompare = null;

        parser = new IDCodeParser(true);
    }

    public boolean isStructure(String tag) {
        boolean b = false;

        DWARHeaderTag header = hmSynonym_Header.get(tag);

        if (header.getProperty(COLPROP_SPTYPE) == null)
            return false;

        if (header.getProperty(COLPROP_SPTYPE).equals(SPTYPE_IDCODE)) {
            b = true;
        }

        return b;
    }

    public boolean isSynonym(String synonym) {
        boolean b = false;

        DWARHeaderTag header = hmSynonym_Header.get(synonym);

        if (header != null)
            b = true;


        return b;
    }

    /**
     * Adds cols for idcode, coordinates and FragFp.
     *
     * @return
     */
    public static final DWARHeader getStructureDWARHeader() {
        DWARHeader header = new DWARHeader();

        header.add(ConstantsDWAR.TAG_IDCODE2);

        header.add(ConstantsDWAR.TAG_COOR2);

        header.add(SPTYPE_FRAGFP);

        try {
            header.addProperty(ConstantsDWAR.TAG_IDCODE2, COLPROP_SPTYPE, SPTYPE_IDCODE);

            header.addSynonym(ConstantsDWAR.TAG_IDCODE2, ConstantsDWAR.TAG_IDCODE);

            header.addProperty(ConstantsDWAR.TAG_COOR2, COLPROP_SPTYPE, SPTYPE_COORD2D);

            header.addSynonym(ConstantsDWAR.TAG_COOR2, ConstantsDWAR.TAG_COOR);

            header.addSynonym(ConstantsDWAR.TAG_COOR2, DWARFileHandler.HEADER_COOR_ORACLE);

            header.addProperty(SPTYPE_FRAGFP, COLPROP_SPTYPE, SPTYPE_FRAGFP);

            header.addSynonym(SPTYPE_FRAGFP, DWARFileHandler.HEADER_FP_OLD);

            header.addProperty(SPTYPE_FRAGFP, COLPROP_VERSION, DescriptorHandlerFFP512.VERSION);

            header.addProperty(SPTYPE_FRAGFP, COLPROP_PARENT, PARENT_STRUCTURE);

            // odeHeader.addProperty(TAG_MOLECULE, COLPROP_WRITABLE, COL_PROP_DO_NOT_WRITE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return header;
    }

    public String get(int index) {

        if (hmIndex_Header.get(index) != null)
            return hmIndex_Header.get(index).get();
        else
            return null;
    }

    public IDCodeParser getParser() {
        return parser;
    }

    // public getProperties

    public boolean isWritable(int index) {
        boolean writable = true;

        if (hmIndex_Header.get(index) != null) {
            writable = hmIndex_Header.get(index).isWritable();
        }

        return writable;
    }

    public int get(String tag) throws NullPointerException {

        if (tag == null) {
            throw new RuntimeException("Header tag is null!");
        }

        DWARHeaderTag header = hmSynonym_Header.get(tag);

        if (header == null)
            return -1;

        return hmHeaderTag_Index.get(header.get());
    }

    public List<String> get() {
        List<String> li = new ArrayList<String>();
        for (int i = 0; i < indexCounter; i++) {
            li.add(get(i));
        }

        return li;
    }

    /**
     * @return number of columns.
     */
    public int size() {
        return indexCounter;
    }

    public String toStringSynonyms() {
        StringBuilder sb = new StringBuilder();

        System.out.println("key tag");
        List<String> liKey = new ArrayList<String>(hmSynonym_Header.keySet());
        for (String key : liKey) {
            String tag = hmSynonym_Header.get(key).get();
            sb.append(key + " " + tag + "\n");
        }

        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        List<Integer> liIndex = new ArrayList<Integer>(hmIndex_Header.keySet());

        for (int i = 0; i < liIndex.size(); i++) {
            int index = liIndex.get(i);

            sb.append(index + ": ");

            sb.append(hmIndex_Header.get(index));

            if (i < liIndex.size() - 1)
                sb.append(", ");
            else
                sb.append(".");
        }

        return sb.toString();
    }

    public String toStringDWAR() {
        StringBuilder sb = new StringBuilder();

        if (dwFileInfo != null)
            sb.append(dwFileInfo.toString());
        else
            sb.append(DataWarriorFileInfo.toStringDefault());

        sb.append("\n");

        if (hasProperties()) {
            sb.append(toStringColProperties());

            sb.append("\n");
        }

        for (int i = 0; i < indexCounter; i++) {

            if (get(i) != null && isWritable(i)) {

                sb.append(get(i));
                if (i < indexCounter - 1)
                    sb.append(DWARFileHandler.SEP_TABLE_HEADER);
            }
        }

        return sb.toString();
    }

    /**
     * @param tagStructure
     * @return null if not found
     */
    public String getTagCoordinates2D(String tagStructure) {

        List<DWARHeaderTag> liHeaderTag = new ArrayList<DWARHeaderTag>(hmIndex_Header.values());

        String tagCoord = null;

        boolean hasStructureParent = false;

        for (DWARHeaderTag dwarHeaderTag : liHeaderTag) {

            String value = dwarHeaderTag.getProperty(COLPROP_PARENT);

            if (tagStructure.equals(value)) {
                hasStructureParent = true;
                break;
            }

        }

        if (hasStructureParent) {

            for (DWARHeaderTag dwarHeaderTag : liHeaderTag) {

                String value = dwarHeaderTag.getProperty(COLPROP_PARENT);

                if (SPTYPE_COORD2D.equals(value)) {
                    tagCoord = dwarHeaderTag.get();
                    break;
                }

            }

        }

        return tagCoord;
    }

    public String getTagCoordinates() {
        DWARHeaderTag header = hmSynonym_Header.get(SPTYPE_COORD2D);
        String tag = null;
        if (header != null)
            tag = header.get();
        return tag;
    }

    public String getTagFingerprint() {
        DWARHeaderTag header = hmSynonym_Header.get(SPTYPE_FRAGFP);
        String tag = null;
        if (header != null)
            tag = header.get();
        return tag;
    }

//	public String getTagIdCode() {
//		Header header = mHtHeaderSynonym.get(ODEFileHandler.HEADER_IDCODE2);
//		String tag=null;
//		if(header != null) {
//			tag = header.get();
//			return tag;
//		}
//		
//		Header header2 = mHtHeaderSynonym.get(ODEHeader.SPTYPE_IDCODE);
//		if(header2 != null)
//			tag = header2.get();
//		
//		
//		
//		return tag;
//	}

    /**
     * @param header
     * @param channel input stream must stand after start of column properties.
     * @throws IOException
     * @throws Exception
     */
    public static void parseColProperties(DWARHeader header, StringReadChannel channel) throws IOException {

        String line = channel.readLine().trim();

        String sColName = "";

        while (!line.matches(DWARFileHandler.TAG_DW_COLUMN_PROPERTIES_END)) {

            String varName = readVarName(line);

            if (varName.equals(COL_PROP_ORIG_COL_NAME)) {

                sColName = readContent(line);

                if (!header.contains(sColName)) {
                    header.add(sColName);
                }

            } else if (varName.equals(COL_PROP_VAR_PROP)) {
                String varProperty = readContent(line);
                StringTokenizer st = new StringTokenizer(varProperty, "\t");
                String tag = st.nextToken();
                String cont = st.nextToken();
                header.addProperty(sColName, tag, cont);
            }
            line = channel.readLine().trim();
        }
    }

    public static List<String> parseTableHeader(String line) {
        StringTokenizer st = new StringTokenizer(line, DWARFileHandler.SEP_TABLE_HEADER);
        List<String> li = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            li.add(st.nextToken().trim());
        }

        return li;
    }

    public boolean hasProperties() {
        boolean bHasProp = false;
        for (int i = 0; i < indexCounter; i++)
            if (hmIndex_Header.get(i).hasProperties())
                bHasProp = true;
        return bHasProp;
    }

    public static String readVarName(String line) {
        String v = "";
        int iStart = line.indexOf('<');
        int iEnd = line.lastIndexOf('=');
        v = line.substring(iStart + 1, iEnd).trim();
        return v;
    }

    public static String readContent(String line) {
        String v = "";
        int iStart = line.indexOf('"');
        int iEnd = line.lastIndexOf('"');
        v = line.substring(iStart + 1, iEnd);
        return v;
    }


    public String toStringColProperties() {

        StringBuilder sb = new StringBuilder();

        if (hasProperties()) {

            sb.append(DWARFileHandler.TAG_DW_COLUMN_PROPERTIES_START + "\n");

            for (int i = 0; i < indexCounter; i++) {

                if ((hmIndex_Header.get(i) != null) && (hmIndex_Header.get(i).hasProperties())) {

                    DWARHeaderTag header = hmIndex_Header.get(i);

                    if (header.isWritable()) {
                        sb.append(header.toString());
                    }
                }
            }

            sb.append(DWARFileHandler.TAG_DW_COLUMN_PROPERTIES_END);
        }
        return sb.toString();
    }

    public DataWarriorFileInfo getDWFileInfo() {
        return dwFileInfo;
    }

    public String[] getHeaderIDCodes() {
        List<String> li = new ArrayList<String>();
        return li.toArray(new String[li.size()]);
    }

    public void setDWFileInfo(DataWarriorFileInfo fileInfo) {
        dwFileInfo = fileInfo;
    }

    public String getTagCompare() {
        return mTagCompare;
    }

    public void setTagCompare(String tagCompare) {
        mTagCompare = tagCompare;
    }

//	public String getTagIdCode() {
//		Header header = mHtHeaderSynonym.get(ODEFileHandler.HEADER_IDCODE2);
//		String tag=null;
//		if(header != null) {
//			tag = header.get();
//			return tag;
//		}
//		
//		Header header2 = mHtHeaderSynonym.get(ODEHeader.SPTYPE_IDCODE);
//		if(header2 != null)
//			tag = header2.get();
//		
//		
//		
//		return tag;
//	}


    public static void replaceHeaderTag(File fiDWAR, String tagOld, String tagNew) throws Exception {
        DWARFileHandler fh = new DWARFileHandler(fiDWAR);

        DWARHeader header = fh.getDWARHeader();

        DWARHeader headerNew = new DWARHeader(header);

        if (headerNew.isSynonym(tagOld))
            headerNew.remove(tagOld);
        else
            throw new RuntimeException("No tag " + tagOld + " in file " + fiDWAR.getAbsolutePath() + ".");

        headerNew.add(tagNew);

        File fiTmp = File.createTempFile("replaceHeaderTag", ConstantsDWAR.DWAR_EXTENSION);

        DWARFileWriter fw = new DWARFileWriter(fiTmp, headerNew);

        List<String> liHeader = header.get();
        while (fh.hasMore()) {
            DWARRecord rec = fh.next();

            DWARRecord recNew = new DWARRecord(headerNew);

            for (String tag : liHeader) {
                String v = rec.getAsString(tag);

                if (tag.equals(tagOld))
                    tag = tagNew;

                recNew.addOrReplaceField(tag, v);
            }

            fw.write(recNew);
        }

        fw.close();
        fh.close();

        //FileUtils.copy(fiTmp, fiDWAR);
        Files.copy(fiTmp.toPath(), fiDWAR.toPath());

        fiTmp.delete();
    }


}
