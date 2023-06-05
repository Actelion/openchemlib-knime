package com.actelion.research.knime.utils.dwar;


import com.actelion.research.io.StringReadChannel;
import com.actelion.research.util.ErrorHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * DWARReaderThread
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.1
 * 8 Mar 2010 MvK: updates
 * 9 Mar 2012 MvK: ConcurrentLinkedQueue<DWARRecord> etc. introduced.
 */
public class DWARReaderThread implements Runnable {
	
    public static boolean VERBOSE = false;
    
    public static int CPACITY_RECORD_LIST = 10000;

    private static final long TIME_SLEEP = 100;
    
    public static final String SEP = "\t";

    private URL url;
    
    private String version;
	
    private ConcurrentLinkedQueue<DWARRecord> liRecord;
    
    private ErrorHashMap errorHashMap;
    
    private AtomicBoolean finalize;
    
    private AtomicBoolean finished;
    
    private AtomicBoolean allRecordsInBuffer;
      
    private DWARHeader header;

    public DWARReaderThread(URL url, DWARHeader header, String version){
    	
    	this.url = url;
    	
    	this.version = version;
    	
    	this.header = header;
    	
    	init();
    }
    
    
    private void init(){
    	
    	liRecord = new ConcurrentLinkedQueue<DWARRecord>();
    	
    	errorHashMap = new ErrorHashMap();
    	    	    	
    	finished = new AtomicBoolean(false);
    	
    	finalize = new AtomicBoolean(false);
    
    	// Has to be true or hasMore() will be true without any record.
    	allRecordsInBuffer = new AtomicBoolean(false);
    }
    
    
    private void add2BufferList(DWARRecord rec) throws Exception{
    	liRecord.add(rec);
    }

    public static DWARRecord extractRecord(String line, DWARHeader header) throws Exception{

        DWARRecord rec = new DWARRecord(header);

        int pos = 0;
        
        for (int i = 0; i < header.size(); i++) {
        	
            int posEnd = line.indexOf(SEP, pos);
            
            if(i>0)
          	  posEnd = line.indexOf(SEP, pos + 1);
            
            String tag = header.get(i);
            
            if(tag==null){
            	throw new RuntimeException("Header tag is null!");
            }
            
            String cont = null;
            
            if (posEnd > -1)
                cont = line.substring(pos, posEnd);
            else {
                cont = line.substring(pos);
            }
            
            cont = cont.trim();
            
            if (cont.length() > 0)
         		  rec.addOrReplaceField(tag, cont);
            else {
                rec.addOrReplaceField(tag, null);
            }
            pos = posEnd;

            if (posEnd == -1) {
                break;
            }
            
        }

        return rec;

    }
    
    public DWARRecord extractRecord(String line) throws Exception{

        DWARRecord rec = new DWARRecord(header);

        int pos = 0;
        
        for (int ii = 0; ii < header.size(); ii++) {
            int posEnd = line.indexOf(SEP, pos);
            if(ii>0)
          	  posEnd = line.indexOf(SEP, pos + 1);
            
            String tag = header.get(ii);
            String cont = null;
            if (posEnd > -1)
                cont = line.substring(pos, posEnd);
            else {
                cont = line.substring(pos);
            }
            cont = cont.trim();
            if (cont.length() > 0)
         		  rec.addOrReplaceField(tag, cont);
            else {
                rec.addOrReplaceField(tag, null);
            }
            pos = posEnd;

            if (posEnd == -1) {
                break;
            }
        }
        

        return rec;

    }

    public boolean hasMore() {
    	
    	if(allRecordsInBuffer.get() && liRecord.size()==0) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    public DWARRecord next() {

    	int maxCycles=250;
    	
    	DWARRecord rec = liRecord.poll();
    	
    	int ccBlock=0;
    	while(rec==null){
    		try {Thread.sleep(TIME_SLEEP);} catch (InterruptedException e) {e.printStackTrace();};
    		
    		if(!hasMore()){
    			throw new RuntimeException("No more records!");
    		}
    		
    		rec = liRecord.poll();
    		ccBlock++;
    		
    		if(ccBlock == maxCycles) {
    			throw new RuntimeException("Not possible to retrieve a record!");
    			
        	}
    	}

    	return rec;

    }

    public void run() {
    	
    	if(VERBOSE)
    		System.out.println("Start DWARReaderThread run()");

		try {
			
	    	StringReadChannel channel = DWARFileHandler.skipLinesUntilHeader(url, version);
			
	    	channel.readLine();
	    	
			String line = "";
			
			int ccLines=0;
			
			while(!finished.get()){
				
				if(liRecord.size() <= CPACITY_RECORD_LIST) {
					
					// Check for finished input stream.
//					int cc=0;
					
					line = channel.readLine();
					
					if(!channel.hasMoreLines()){
						allRecordsInBuffer.set(true);
						
						finished.set(true);
					}
					
//					while((line = channel.readLine()) == null){
//						if(channel.isEndOfFile()){
//							
//							allRecordsInBuffer.set(true);
//							
//							finished.set(true);
//							break;
//						}
//						
//						try {
//							Thread.sleep(TIME_SLEEP);
//						} catch (InterruptedException e) {
//							System.out.println("DWARReaderThread run(): InterruptedException.");
//							finished.set(true);
//							errorHashMap.add(e);
//							break;
//						}
//						if(cc==10){
//							System.out.println("DWARReaderThread run(): Not possible to get next line.");
//							finished.set(true);
//							break;
//						}
//						
//						cc++;
//					}
						
					if(finished.get()) {
						break;
					}
					
					// Properties started? -> End of structure data.
					if(DWARFileHandler.isEndOfTableData(line)) {
						
						finished.set(true);
						
						allRecordsInBuffer.set(true);
						
					} else {
						try {
							
							DWARRecord rec = extractRecord(line, header);
							
							add2BufferList(rec);
							
							ccLines++;
							
						} catch (Exception ex) {
							
							String e = "Error in '" + url.toString() + "'.";
							
							errorHashMap.add(new RuntimeException(e));
							
							errorHashMap.add(ex);
						}
					}
					
				} else {
					try {
						Thread.sleep(TIME_SLEEP);
					} catch (InterruptedException e) {
						System.out.println("DWARReaderThread run(): InterruptedException.");
						finished.set(true);
						e.printStackTrace();
						errorHashMap.add(e);
						break;
					}
				}
				
				
				if(finalize.get()){
					if(VERBOSE)
						System.out.println("DWARReaderThread run(): Forced break.");
					finished.set(true);
				}
			}
		
			channel.close();
			
			if(VERBOSE) {
				System.out.println("DWARReaderThread run(): Channel and stream closed");
				System.out.println("Red: " + ccLines + ".");
			}
				
			if(errorHashMap.hasErrors())
				System.err.println(errorHashMap.toString());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(VERBOSE)
			System.out.println("DWARReaderThread run() end.");
		
    }

	public void setFinalize(boolean finalize) {
		this.finalize.set(finalize);
	}
	
	public ErrorHashMap getErrors(){
		return errorHashMap;
	}	
}
