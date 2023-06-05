package com.actelion.research.knime.utils.dwar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/*
 * A header has a name and can have several properties.
 * A property consists of a label i.e. specialType and a name i.e. idcode.
 * 
<column properties>
<columnName="Structure">
<columnProperty="specialType	idcode">
<columnName="idcoordinates2D">
<columnProperty="specialType	idcoordinates2D">
<columnProperty="parent	Structure">
*/
public class DWARHeaderTag {
	
	private int index;
	
	private String tag;
	
	private HashMap<String, String> hmProperty; 
	
	public DWARHeaderTag(){
		hmProperty = new HashMap<String, String>();
	}
	
	public DWARHeaderTag(String tag){
		hmProperty = new HashMap<String, String>();
		
		this.tag = tag;
	}
	public void addProperty(String property, String value) {
		if(property.equals(DWARHeader.COL_PROP_ORIG_COL_NAME)) {
			String e = "Not possible to add " + DWARHeader.COL_PROP_ORIG_COL_NAME + " as a property.";
			throw new RuntimeException(e);
		}
		
		hmProperty.put(property, value);
	}
	
	public void copy(DWARHeaderTag h){
		
		hmProperty.clear();
		List<String> li = new ArrayList<String>(h.hmProperty.keySet());
		for (String key : li) {
			hmProperty.put(key, h.hmProperty.get(key));
		}
		
		tag = h.tag;
		
	}
	
	public boolean isWritable(){
		boolean writable=true;
		
		String attr = hmProperty.get(DWARHeader.COLPROP_WRITABLE);
		
		if(attr != null && DWARHeader.COLPROP_WRITABLE.equals(attr)){
			writable=false;
		}
		
		return writable;
	}
	
	public void set(String tag){
		this.tag = tag;
	}
	
	/*
	 * returns header tag
	 */
	public String get(){
		return tag;
	}
	
	public boolean hasProperties(){
		if(hmProperty.size()==0)
			return false;
		else
			return true;
	}
	
	public String getProperty(String property){
		return hmProperty.get(property);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("<" + DWARHeader.COL_PROP_ORIG_COL_NAME + "=");
		sb.append("\"" + tag + "\">\n");
		List<String> liKey = new ArrayList<String>(hmProperty.keySet());
		for (String key : liKey) {
			String v = hmProperty.get(key);
			sb.append("<" + DWARHeader.COL_PROP_VAR_PROP + "=");
			sb.append("\"" + key + "\t" + v + "\">\n");
		}
		
		return sb.toString();
	}

	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	
	
	public static void sortWithIndex(List<DWARHeaderTag> liHeader){
		
		Collections.sort(liHeader, new Comparator<DWARHeaderTag>() {

			public int compare(DWARHeaderTag o1, DWARHeaderTag o2) {
				
				if(o1.index > o2.index){
					return 1;
				} else if(o1.index < o2.index){
					return -1;
				}
				
				return 0;
			}
		});
		
	}

	
}