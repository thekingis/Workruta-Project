package com.workruta.android.Utils;

import java.util.Date;

public class CollectionLists implements Comparable<CollectionLists> {

    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDateTime(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(CollectionLists lists) {
        if (getDate() == null || lists.getDate() == null)
            return 0;
        return getDate().compareTo(lists.getDate());
    }

}
