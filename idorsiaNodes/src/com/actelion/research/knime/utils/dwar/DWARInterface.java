package com.actelion.research.knime.utils.dwar;

import java.util.List;

public interface DWARInterface {
	
	public void close();
	
	public String getSource();
	
	public List<String> getHeader();
	
	public DWARHeader getDWARHeader();
	
	public boolean hasMore();
	
	public DWARRecord next();
	
	public void reset(String headerTag);
	
	public void reset();
	
	public int size();
	
	
	
	
}
