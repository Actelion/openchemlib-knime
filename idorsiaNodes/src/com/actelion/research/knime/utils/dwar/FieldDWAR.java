package com.actelion.research.knime.utils.dwar;

/**
 * 
 * 
 * FieldDWAR
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * ca 2006 MvK: Start implementation
 */
public class FieldDWAR {

	private boolean isString;
	
	private Object data;

    public FieldDWAR() {
    	isString = false;
    }

    public FieldDWAR(Object content) {
    	setContent(content);
    }

    public boolean equals(Object o){
    	boolean bEq=true;
    	
    	FieldDWAR c = (FieldDWAR)o;
    	
    	if(size() != c.size()){
    		bEq = false;
    	} else if((data instanceof byte[])&&(c.data instanceof byte[])) {
    		byte[] s1 = (byte[])data;
    		byte[] s2 = (byte[])c.data;
    		
    		for (int i = 0; i < s1.length; i++) {
				if(s1[i]!=s2[i]) {
					bEq=false;
					break;
				}
			}
    	}
    	return bEq;
    }
    
    public Object getContent() {
    	
    	Object obj = data;
    	
    	if(data == null)
    		return null;
    	
    	if(isString){
    		String s = new String((byte[])data);
    		
    		obj = s;
    	}
    	
    	return obj;
    }

    public int hashCode(){
    	if(data instanceof byte[]) {
    		return new String(((byte[])data)).hashCode();
    	}
    	
    	return data.hashCode();
    }
    
    public void setContent(Object s) {
    	
    	if(s instanceof String) {
    		
    		isString = true;
    		
    		data = ((String)s).getBytes();
    		
    	} else {
    		isString = false;
    		
    		data = s;
    	}
    	
    	
    }
    
    public int size(){
    	if(data instanceof byte[]) {
    		return ((byte[])data).length;
    	}
    	
    	return 0;
    }

    
    
}