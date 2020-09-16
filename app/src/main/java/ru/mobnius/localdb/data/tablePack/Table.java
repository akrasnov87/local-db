package ru.mobnius.localdb.data.tablePack;

public class Table {
    private String[] mHeaders;
    private String[][] mValues;
    private int mIdx = 0;

    public Table() {}

    public Table(String[] headers, int count) {
        super();
        mHeaders = headers;
        mValues = new String[count][headers.length];
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
