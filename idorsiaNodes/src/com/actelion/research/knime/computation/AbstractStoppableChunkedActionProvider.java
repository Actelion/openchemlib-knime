package com.actelion.research.knime.computation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.actelion.research.knime.data.MoleculeRow;

// Author: tl

public abstract class AbstractStoppableChunkedActionProvider {

	private boolean stopRequested = false;
	private int totalRows;
	private int chunkSize;
	List<int[]> chunks;	
	//private final MoleculeRow[] records;
	
	public AbstractStoppableChunkedActionProvider(int total_rows, int chunk_size) {
		//this.records = records;
		this.totalRows = total_rows;
		this.chunkSize = chunk_size;
		this.createChunks();
	}
	
	private void createChunks() {
		this.chunks = new ArrayList<>();
		int start = 0;
		int end   = 0;
		do {
			end = Math.min(totalRows,start+this.chunkSize);
			this.chunks.add(new int[] {start,end});
			start = end;			
		}
		while( end < totalRows);			
	}
	
	protected abstract boolean processChunk(int start, int end) throws Exception;
	
    public void requestStop() {
        this.stopRequested = true;
    }

    boolean isStopRequested() {        
        return stopRequested;
    }
	
	public List<Callable<Boolean>> getTasks() {
		List<Callable<Boolean>> tasks = new ArrayList<>();			
		for(int[] ti : this.chunks) {
			tasks.add(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return processChunk(ti[0],ti[1]);
				}
			});
		}
		return tasks;		
	}
	
	
}
