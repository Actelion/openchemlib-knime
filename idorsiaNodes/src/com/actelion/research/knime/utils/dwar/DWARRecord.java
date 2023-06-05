package com.actelion.research.knime.utils.dwar;

import com.actelion.research.calc.Matrix;
import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.chem.descriptor.ISimilarityCalculator;
import com.actelion.research.util.ConstantsDWAR;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;

/**
 * <p>Title: ODERecord </p>
 * <p></p>
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * 07.12.2004 MvK: Start implementation
 */

public class DWARRecord{

    public static final int FINGERPRINT_LENGTH_STRING = 128;
    
    public static final String NEW_LINE = "<NL>";
    
    public static final String HEX_STRING_SEP = "X";
    
    public static final String HEX_STRING_EMPTY = "n";
    
    public static final String NULL = "null";

    
    public static final boolean DEBUG=false;
    
    private long id;
    
    private HashMap<Integer, FieldDWAR> hmFields;
    
    protected DWARHeader dwarHeader; 
    
    private double score;

    public DWARRecord() {
    	init();
    	dwarHeader = DWARHeader.getStructureDWARHeader();
    }

    public DWARRecord(DWARHeader header) {
    	init();
    	this.dwarHeader = header;
    }
    public DWARRecord(String idcode) {
    	init();
    	dwarHeader = DWARHeader.getStructureDWARHeader();
    	setIdCode(idcode);
    }

    /**
     * Be careful with adding new fields, the header is a flat copy!
     * @param rec
     */
    public DWARRecord(DWARRecord rec) {
    	rec.copy(this);
    }
    
	/**
	 * 
	 * @param copy This is written into copy.
	 */
    public void copy(DWARRecord copy){
    	copy.init();
    	copy.id = getID();
    	copy.dwarHeader = dwarHeader;
        List<String> header = getHeaderList();
        for (String tag : header) {
			try {
				copy.addField(tag, get(tag));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
    
    private void init() {
    	
        hmFields = new HashMap<Integer, FieldDWAR>();
        
        dwarHeader = new DWARHeader();
    }
    
    /**
     * If the field is already there a RuntimeException is thrown.
     * @param header
     * @param content
     */
    public void addField(String header, Object content) {
    	
    	if(!isHeader(header)){
    		dwarHeader.add(header);
    	}
    	
        if(isField(header)) {
            String err = "Field for header '" + header + "' is alreadey present.";
            throw new RuntimeException(err);
        }
        
    	int index = dwarHeader.get(header);
    	
        FieldDWAR f = new FieldDWAR(content);
        
        hmFields.put(index, f);
    }

    /**
     * 
     * @param header
     * @param content if null nothing will be added.
     */
    public void addOrReplaceField(String header, Object content) {
    	
    	if(content == null)
    		return;
    	
    	if(isField(header)) {
        	int index = dwarHeader.get(header);
            FieldDWAR f = hmFields.get(index);
            f.setContent(content);
        } else {
        	try {
				addField(header, content);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Adds the string content to the specified field. If the field does not exists it is created.
     * @param header
     * @param s
     */
    public void addContent(String header, String s) {
    	
    	if(s == null)
    		return;
    	
    	if(isField(header)) {
    		
        	int index = dwarHeader.get(header);
        	
            FieldDWAR f = hmFields.get(index);
            
            if((f.getContent()!=null) && (f.getContent() instanceof String)) {
            	
            	String str = (String)f.getContent();
            	
            	StringBuilder sb = new StringBuilder(str);
            	
            	if(str.length()>0){
            		sb.append(ConstantsDWAR.SEP_VALUE);
            	}
            	
            	sb.append(s);
            	
            	f.setContent(sb.toString());
            }
            
            
        } else {
        	try {
				addField(header, s);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    

    /**
     * Content of rec is added to this.
     * @param rec
     */
    public void add2Record(DWARRecord rec){
    	this.id = rec.id;
    	
        List<String> liHeader = rec.getHeaderList();
        for (String tag : liHeader) {
			addOrReplaceField(tag, rec.getAsString(tag));
		}
    }
    
    public int hashCode(){
    	return getIdCode().hashCode();
    }
    
    public boolean equals(Object o){
    	boolean bEq=true;
    	
    	DWARRecord rec = (DWARRecord)o;
    	
    	if(dwarHeader.size() != rec.getHeaderList().size()){
    		return false;
    	} 
    	
    	
    	for (String header : dwarHeader.get()) {
			String cont1 = getAsString(header);
			String cont2 = rec.getAsString(header);
			
			if(cont1==null && cont2==null){
				continue;
			} else if(cont1==null && cont2!=null)
				return false;
			else if(cont1!=null && cont2==null)
				return false;
			else if(!cont1.equals(cont2)){
				return false;
			}
		}
    	
    	
    	return bEq;
    }
    
    public Object getField(int index) {
    	
    	if(index == -1){
    		throw new RuntimeException("Index is negative.");
    	}
    	
    	FieldDWAR field = hmFields.get(index);
    	
    	Object o = null;
    	
        if(field != null)
            o = field.getContent();
    	
    	return o;
    }
    
    /**
     * 
     * @param tag
     * @return null if tag not found in record header.
     */
    public String getAsString(String tag) {
    	
    	if(tag==null)
    		return null;
    	
    	String str = null;
    	
        FieldDWAR field=null;
		try {
			Integer integerKey = dwarHeader.get(tag.trim()); 
			if(hmFields.containsKey(integerKey))
				field = hmFields.get(dwarHeader.get(tag.trim()));
		} catch (NullPointerException ex) {
			if(DEBUG) {
				StringBuilder sb = new StringBuilder(); 
				sb.append("Null pointer exception in record with tag '" + tag + "'.\n");
				sb.append(dwarHeader.toString() + "\n");
				System.err.println(sb.toString());
				ex.printStackTrace();
			}
		}
        if(field != null) {
        	
        	if(field.getContent() instanceof StereoMolecule){
        		StereoMolecule mol = (StereoMolecule)field.getContent();
        		
        		Canonizer can = new Canonizer(mol);
        		
        		str = can.getIDCode();
        		
        	} else if(field.getContent() instanceof String){
        		str = (String)field.getContent();
        		
        	}  else if(field.getContent() instanceof byte[]){
        		str =  new String((byte[])field.getContent());
        		
        	} else if(field.getContent() != null){
        		str = field.getContent().toString();
        		
        	}
        }
        
        return str;
    }
    
    /**
     * 
     * @param tag
     * @return null if nothing is in the field or if the the field does not exists.
     */
    public Object get(String tag) {
    	
    	if(tag==null)
    		return null;
    	
    	Object obj = null;
    	
        FieldDWAR field=null;
		try {
			Integer integerKey = dwarHeader.get(tag.trim()); 
			if(hmFields.containsKey(integerKey))
				field = hmFields.get(dwarHeader.get(tag.trim()));
		} catch (NullPointerException ex) {
			if(DEBUG) {
				StringBuilder sb = new StringBuilder(); 
				sb.append("Null pointer exception in record with tag '" + tag + "'.\n");
				sb.append(dwarHeader.toString() + "\n");
				System.err.println(sb.toString());
				ex.printStackTrace();
			}
		}
        if(field != null) {
        	obj = field.getContent();
        }

        return obj;
    }
    
    /**
     * 
     * @param tagRegEx Regular expression for header tag.
     * @return List with the contents from fileds matching the regular 
     * expression tag. If the content of a field is null nothing is added to the list. 
     */
    public List<String> getFromRegEx(String tagRegEx) {
    	List<String> li = new ArrayList<String>();
    	
    	for (String tag : dwarHeader.get()) {
			if(tag.matches(tagRegEx)) {
				String content = getAsString(tag);
				if(content!=null)
					li.add(content);
			}
		}
    	
        return li;
    }

    public String getODEFormat(String tag) {
    	String str = null;
    	if (DescriptorConstants.DESCRIPTOR_FFP512.shortName.equals(tag)
    	 || DescriptorConstants.DESCRIPTOR_FFP512.name.equals(tag)) {
    		str = getFingerprint();
    	} 
	        
    	// We try the other fields.
    	if(str == null)
    		str = getAsString(tag.trim());
    	
    	
    	
    	if(str != null) {
	    	str = str.replaceAll("\\n", NEW_LINE);
	    	str = str.replaceAll("\\r", "");
    	} else {
    		str = "";
    	}
    	
        return str;
    }
    
	/**
	 * The header keys are in upper case 
	 * @param regex
	 * @return
	 */
    public String getRegEx(String regex) {
        String str = null;

        List<String> li = dwarHeader.get();
        for (String key : li) 
          if(key.matches(regex)) {
              str = getAsString(key);
          }
        
        return str;
    }

    public String getCoordinates() {
        return getAsString(dwarHeader.getTagCoordinates());
    }

    public String getFingerprint() {
        return getAsString(dwarHeader.getTagFingerprint());
    }

    public List<String> getHeaderList() {
        return dwarHeader.get();
    }

    public DWARHeader getHeader() {
        return dwarHeader;
    }

    public long getID() {
        return id;
    }

    public String getIdCode() {
    	
    	String idcode = getAsString(ConstantsDWAR.TAG_IDCODE2);
    	
    	if(idcode == null)
    		idcode = getAsString(dwarHeader.getTagIdCode());
    	
        return idcode;
    }

    /**
     * @return
     */
    public StereoMolecule getMolecule() {
    	
    	StereoMolecule mol = (StereoMolecule)get(DWARHeader.TAG_MOLECULE);
    	
    	if(mol==null){
    		mol = dwarHeader.getParser().getCompactMolecule(getIdCode(), getCoordinates());
    	}
    	
        return mol;
    }
    
    public void setMolecule(StereoMolecule mol) {
    	
        Canonizer can = new Canonizer(mol);
        
        setIdCode(can.getIDCode());
        
        setCoordinates(can.getEncodedCoordinates());
        
        addOrReplaceField(DWARHeader.TAG_MOLECULE, mol);
        
    }

    /**
     * 
     * @param header
     * @return true If isHeader is true and if the corresponding field is not null. Otherwise false!
     */
    public boolean isField(String header) {
    	
    	FieldDWAR f=null;
		
		int index = dwarHeader.get(header);
		if(index>-1)
			f = hmFields.get(index);
    	
    	if(f==null)
    		return false;
    	else
    		return true;
    }
    
    public boolean isHeader(String header) {
        return dwarHeader.contains(header);
    }
    
	public static final String getStrippedIDCode(String idcode){
		
		IDCodeParser parser = new IDCodeParser(false);
		
		StereoMolecule mol = new StereoMolecule();
		
		mol = parser.getCompactMolecule(idcode);
		
		mol.ensureHelperArrays(StereoMolecule.cHelperCIP);
		
		mol.stripStereoInformation();
		
		Canonizer can = null;
    	can = new Canonizer(mol);
    	return can.getIDCode();
	}
	/**
	 * Gets a String array with the id code for each fragment, 
	 * in pos 0 the idcode of the complete molecule is stored. 
	 * @param idcode
	 * @return
	 */
	public static final String [] getFragIDCodes(String idcode){
		
		IDCodeParser parser = new IDCodeParser(false);
		
		StereoMolecule mol = new StereoMolecule();
		
		mol = parser.getCompactMolecule(idcode);
		
		mol.ensureHelperArrays(StereoMolecule.cHelperCIP);
		
		mol.stripStereoInformation();
		
		StereoMolecule [] arrFrags = mol.getFragments();
		String [] arrIdCode = new String [arrFrags.length+1];
		
		arrIdCode[0]=getStrippedIDCode(idcode);
		for (int i = 0; i < arrFrags.length; i++) {
			Canonizer can = new Canonizer(arrFrags[i]);
	    	arrIdCode[i+1]=getStrippedIDCode(can.getIDCode());
		}
		
		
    	return arrIdCode;
	}

    
    /**
     * Use this function with extreme care!!
     * The keys for the following fields are decreased by one, 
     * so the record is not any more in sync with the ODEHeader!
     * @param tag
     */
    public void removeField(String tag){
    	
    	int key = dwarHeader.get(tag);
    	
    	hmFields.remove(key);
    	
    	List<Integer> liKey = new ArrayList<Integer>(hmFields.keySet());
    	Collections.sort(liKey);
    	
    	for (Integer i : liKey) {
    		if(i>key){
	    		FieldDWAR f = hmFields.remove(i);
	    		if(f!=null){
	    			hmFields.put(i-1, f);
	    		}
    		}
		}
    }
    
    public void setCoordinates(String coordinates) {
    	if(dwarHeader.getTagCoordinates()==null){
    		dwarHeader.add(DWARHeader.SPTYPE_COORD2D);
    		try {
				dwarHeader.addProperty(DWARHeader.SPTYPE_COORD2D, DWARHeader.COLPROP_SPTYPE, DWARHeader.SPTYPE_COORD2D);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	addOrReplaceField(dwarHeader.getTagCoordinates(), coordinates.trim());
    }

    /**
     * The method <code>addField(...)</code> may be used before to initialize the field.
     * The field must be already there, otherwise nothing will be set and a runtime exceptin is thrown. 
     * @param header
     * @param content
     * @return
     */
    public boolean setField(String header, Object content) {
    	    	
        boolean bOK = false;
        
        if(isField(header)) {
            FieldDWAR f = hmFields.get(dwarHeader.get(header));
            f.setContent(content);
            bOK = true;
        } else {
        	String m = "No field or not a field with a not null entry " + header + ".";
        	throw new RuntimeException(m);
        }
        return bOK;
    }

    public void setFields(List<String> liHeader) {
    	for (String tag : liHeader) {
			setField(tag, "");
		}
    }

	public void setFingerprint(String fp) throws DataFormatException {
		
    	if(dwarHeader.getTagFingerprint()==null){
    		dwarHeader.add(DWARHeader.SPTYPE_FRAGFP);
    		try {
				dwarHeader.addProperty(DWARHeader.SPTYPE_FRAGFP, DWARHeader.COLPROP_SPTYPE, DWARHeader.SPTYPE_FRAGFP);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	addOrReplaceField(dwarHeader.getTagFingerprint(), fp.trim());
    }

    public void setIdCode(String idCode) {
    	
    	if(dwarHeader.getTagIdCode()==null){
    		dwarHeader.add(ConstantsDWAR.TAG_IDCODE2);
    		try {
				dwarHeader.addProperty(ConstantsDWAR.TAG_IDCODE2, DWARHeader.COLPROP_SPTYPE, DWARHeader.SPTYPE_IDCODE);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	addOrReplaceField(dwarHeader.getTagIdCode(), idCode);
    	
    }
    
    public void setID(long i) {
        id = i;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();

        List<String> liHeader = dwarHeader.get();
        
        for (int i = 0; i < liHeader.size(); i++) {
            String header = liHeader.get(i);
            String cont = getAsString(header);
            if(cont != null)
            	sb.append(cont);
            else
            	sb.append("");
            
            if(i < liHeader.size() - 1) {
            	sb.append(DWARFileHandler.SEP_TABLE_HEADER);
            }
        }

        return sb.toString();
    }
    
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	
	public static List<Double> parseAsDoubles(String sValues){
		
		List<Double> li = new ArrayList<Double>();
		
		StringTokenizer st = new StringTokenizer(sValues, ConstantsDWAR.SEP_VALUE.trim());
		
		while(st.hasMoreTokens()){
			String sVal = st.nextToken().trim();
			try {
				
				double val = DWARRecord.parseAsOneDouble(sVal);

				li.add(val);
			} catch (Exception e) {
				
			}
		}
		
		
		return li;
	}
	
	public static int [] parseAsIntegers(String sValues){
				
		StringTokenizer st = new StringTokenizer(sValues, ConstantsDWAR.SEP_VALUE.trim());
		
		int [] a = new int [st.countTokens()];
		
		int cc=0;
		
		while(st.hasMoreTokens()){
			
			String sVal = st.nextToken().trim();
			
			int val = Integer.parseInt(sVal);
		
			a[cc++]=val;
		}
				
		return a;
	}

	public static double parseAsOneDouble(String sVal){
		
		String s = sVal.replace('>', ' ');
		
		s = s.replace('<', ' ');
		
		s = s.trim();
			
		return Double.parseDouble(s);
	}
	
	public static double getAverage(String sValues){
		
		List<Double> li = parseAsDoubles(sValues);
		
		Matrix ma = new Matrix(true, li);
		
		double val = ma.getMean();
		
		return val;
	}
	
	/**
	 * 
	 * @param rec
	 * @param isc
	 * @return maximum value in the corresponding result column of the given similarity calculator interface. 
	 */
	public static double getMaxValue(DWARRecord rec, ISimilarityCalculator isc){
		
		double max = Double.NaN;
		
		String tag = DescriptorHelper.getTagDescriptorSimilarity(isc);

		String sVals = rec.getAsString(tag);
		
		if((sVals != null) && (sVals.length() > 0)) {
		
			List<Double> liSim = DWARRecord.parseAsDoubles(sVals);
			
			Collections.sort(liSim);
			
			max = liSim.get(liSim.size()-1);
		
		}
		
		return max;
	}
	
	/**
	 * Is also able to read null.
	 * @param rec
	 * @param tag
	 * @return
	 */
	public static List<Long> getLongList(DWARRecord rec, String tag){
		
		List<String> liStrTime = DWARRecord.getStringList(rec, tag);
		
		List<Long> liTime = new ArrayList<Long>(liStrTime.size());
		
		for (String s : liStrTime) {
			
			if(NULL.equals(s)){
				liTime.add(null);
			} else {
				liTime.add(Long.parseLong(s));	
			}
		}
		
		return liTime;
	}
	
	public static List<String> getStringList(DWARRecord rec, String tag){
		
		String text = rec.getAsString(tag);
				
		return parseAsStrings(text);
	}
	
	/**
	 * Parses the values and tokenizes them with the dwar field tokenizer.
	 * @param text
	 * @return
	 */
	public static List<String> parseAsStrings(String text){
		
		List<String> li = new ArrayList<String>();
				
		if((text != null) && (text.length() > 0)) {
		
			StringTokenizer st = new StringTokenizer(text, ConstantsDWAR.SEP_VALUE.trim());
			
			while(st.hasMoreTokens()){
				li.add(st.nextToken().trim());
			}
		}
		
		return li;
	}
	
	public static int [] getIntegerValues(DWARRecord rec, String tag){
		
		String sVals = rec.getAsString(tag);
		
		return parseAsIntegers(sVals) ;
	}
	
	public static double [] getValues(DWARRecord rec, DescriptorInfo di){
		
		double [] arr = null;
		
		String tagSimFlexophore = DescriptorHelper.getTagDescriptorSimilarity(di);

		String sVals = rec.getAsString(tagSimFlexophore);
		
		if((sVals != null) && (sVals.length() > 0)) {
		
			List<Double> liSim = DWARRecord.parseAsDoubles(sVals);
			
			arr = new double [liSim.size()];
			
			for (int i = 0; i < liSim.size(); i++) {
				arr[i]=liSim.get(i);
			}
		}
		
		return arr;
	}

	public static void merge(DWARRecord rec, DWARRecord source, String tag){
		
		String contentRecord = rec.getAsString(tag);
		
		if(contentRecord == null){
			contentRecord = "";
		}
		
		if(source.getAsString(tag) != null){
		
			if(contentRecord.length()>0) {
				contentRecord += ConstantsDWAR.SEP_VALUE + source.getAsString(tag);
			} else {
				contentRecord += source.getAsString(tag);
			}
			
			rec.addOrReplaceField(tag, contentRecord);
		}
	}

	
	/**
	 * Tries several fields.
	 * @return
	 */
	public static String getName(DWARRecord rec){
		
		String name = "";
		
		for (int i = 0; i < ConstantsDWAR.TAG_NAMES.length; i++) {
			name = rec.getAsString(ConstantsDWAR.TAG_NAMES[i]);
			
			if(name != null && name.length() > 0){
				break;
			}
		}
		
		return name;
		
	}
	
	public static String toString(int [] a) {
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < a.length; i++) {
			sb.append(a[i]);
			if(i < a.length-1){
				sb.append(ConstantsDWAR.SEP_VALUE);
			}
		}
		
		return sb.toString();
	}
	
	public static String toString(double [] a, NumberFormat nf) {
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < a.length; i++) {
			sb.append(nf.format(a[i]));
			if(i < a.length-1){
				sb.append(ConstantsDWAR.SEP_VALUE);
			}
		}
		
		return sb.toString();
	}

	/**
	 * 
	 * @param li
	 * @return String formatted for dwar.
	 */
	public static String toString(List<String> li) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < li.size(); i++) {
			sb.append(li.get(i));
			
			if(i < li.size()-1){
				sb.append(ConstantsDWAR.SEP_VALUE);
			}
		}
		
		return sb.toString();
	}
	
	public static String toStringInteger(List<Integer> li) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < li.size(); i++) {
			sb.append(li.get(i));
			
			if(i < li.size()-1){
				sb.append(ConstantsDWAR.SEP_VALUE);
			}
		}
		
		return sb.toString();
	}
	
	public static String toStringLong(List<Long> li) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < li.size(); i++) {
			sb.append(li.get(i));
			
			if(i < li.size()-1){
				sb.append(ConstantsDWAR.SEP_VALUE);
			}
		}
		
		return sb.toString();
	}
	

}

