package com.actelion.research.knime.utils.dwar;

/**
 * 
 * IDWARFilter
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * 2007 MvK: Start implementation
 * 10.11.2011 MvK: IODEFilter-->IDWARFilter
 */
public interface IDWARFilter extends IFilterInfo {

	/**
	 * Separates two filter. 
	 */
	public static final String SEP_FILTER = ",";
	
	/**
	 * Separates the filter name from the parameter.
	 */
	public static final String SEP_START_PARAMETER = ":";
	
	/**
	 * Separates the parameter.
	 */
	public static final String SEP_PARAMETER = " ";
	
	
    public boolean accept(DWARRecord rec) throws Exception;
    
    public String getSummary();
    
	public int getParsed();

	public void setParsed(int parsed);

	public int getAccepted();

	public void setAccepted(int accepted);
    

}
