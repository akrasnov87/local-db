package ru.mobnius.localdb.data.tablePack;

import java.lang.reflect.Type;

public class Readme {
    private final String[] mHeaders;
    private final String[][] mValues;
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
        System.arraycopy(values, 0, mValues[mIdx], 0, values.length);
        mIdx++;
    }

    public int count() {
        return mIdx;
    }
}
