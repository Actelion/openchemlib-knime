package com.actelion.research.knime.utils.dwar;

/**
 * 
 * IDWARFileWriter
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * 2 Mar 2010 MvK: Start implementation
 */
public interface IDWARWriter {
	
	public void write(DWARRecord rec);
	
	public int getAdded();
	
	public void close();
	
	public boolean isTag(String tag);
	
	public DWARHeader getHeader();
	
}
