package com.actelion.research.knime.data;

import java.util.concurrent.atomic.AtomicInteger;

public class FrequencyItem implements Comparable<FrequencyItem> {
    private AtomicInteger count = new AtomicInteger(1);
    private String idCode = "";

    //~--- constructors ---------------------------------------------------

    public FrequencyItem(String idCode) {
        this.idCode = idCode;
    }

    //~--- methods --------------------------------------------------------

    @Override
    public int compareTo(FrequencyItem o) {
        return Integer.compare(count.get(), o.count.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FrequencyItem)) {
            return false;
        }

        FrequencyItem that = (FrequencyItem) o;

        return !((idCode != null)
                ? !idCode.equals(that.idCode)
                : that.idCode != null);
    }

    @Override
    public int hashCode() {
        return (idCode != null)
                ? idCode.hashCode()
                : 0;
    }

    public void incrementCount() {
        count.getAndIncrement();
    }

    public int getCount() {
        return count.get();
    }


    public String getIdCode() {
        return idCode;
    }


}
