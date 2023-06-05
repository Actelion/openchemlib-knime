/*
 * @(#)ListBasedListModel.java   16/01/04
 *
 * Copyright (c) 2010-2011 Actelion Pharmaceuticals Ltd.
 *
 *  Gewerbestrasse 16, CH-4123 Allschwil, Switzerland
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information
 *  of Actelion Pharmaceuticals Ltd. ("Confidential Information").  You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Actelion Pharmaceuticals Ltd.
 *
 *  Author: finkt
 */



package com.actelion.research.knime.ui;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: finkt
 * Date: Nov 22, 2010
 * Time: 6:27:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListBasedListModel<T> extends DefaultListModel {
    private List<T> valueList = new ArrayList<T>();

    //~--- constructors -------------------------------------------------------

    public ListBasedListModel(List<T> valueList) {
        this.valueList = valueList;
    }

    //~--- methods ------------------------------------------------------------

    public void addAll(List<T> toAdd) {
        valueList.addAll(toAdd);
        valuesChanged();
    }

    @Override
    public Object elementAt(int i) {
        return getElementAt(i);
    }

    @Override
    public void insertElementAt(Object o, int i) {
        int idx          = i;
        T   selectedMeta = (T) o;

        valueList.add(idx, selectedMeta);
    }

    @Override
    public Object remove(int i) {
        Object o = getElementAt(i);

        removeElementAt(i);

        return o;
    }

    public void removeAll() {
        valueList.clear();
        valuesChanged();
    }

    public void removeAll(List<T> toRemove) {
        valueList.removeAll(toRemove);
        valuesChanged();
    }

    @Override
    public boolean removeElement(Object o) {
        int i = valueList.indexOf(o);

        if (i > -1) {
            removeElementAt(i);

            return true;
        }

        return false;
    }

    @Override
    public void removeElementAt(int i) {
        int idx            = i;
        T   selectedObject = (T) getElementAt(idx);

        valueList.remove(selectedObject);
    }

    public void valuesChanged() {
        for (ListDataListener l : getListDataListeners()) {
            l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, valueList.size()));
        }
    }

    //~--- get methods --------------------------------------------------------

    public Object getElementAt(int index) {
        return valueList.get(index);
    }

    public int getSize() {
        if (valueList == null) {
            return 0;
        }

        return valueList.size();
    }

    public List<T> getValueList() {
        return Collections.unmodifiableList(valueList);
    }

    //~--- set methods --------------------------------------------------------

    public void setValues(List<T> values) {
        valueList.clear();
        valueList.addAll(values);
        valuesChanged();
    }
}
