package ru.mobnius.localdb.data.tablePack;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;

import java.lang.reflect.Type;
import java.util.Date;

public class Readme {
    private String[] mHeaders;
    private String[][] mValues;
    private int mIdx = 0;
    private String mHeadersLine;
    private Type[] mTypes;

    public Readme(String headers, int count) {
        super();
        mHeadersLine = headers;
        mHeaders = headers.split("\\|");
        mValues = new String[count][mHeaders.length];
        mTypes = new Type[mHeaders.length];
    }

    public String[] getHeaders() {
        return mHeaders;
    }

    public String[][] getValues() {
        return mValues;
    }

    public String[] getValue(int idx) {
        return mValues[idx];
    }

    public void addValues(String[] values) {
        for(int i = 0; i < values.length; i++) {
            mValues[mIdx][i] = values[i];
        }
        mIdx++;
    }

    public int count() {
        return mIdx;
    }
}
