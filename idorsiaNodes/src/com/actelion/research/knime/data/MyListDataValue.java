package com.actelion.research.knime.data;

import java.util.Iterator;

import org.knime.core.data.DataCell;

public interface MyListDataValue {
	public int size();
	public Iterator<DataCell> iterator();
}