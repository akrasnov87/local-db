package ru.mobnius.localdb.data.tablePack;

public class Table {
    private String[] mHeaders;
    private String[][] mValues;
    private int mIdx = 0;
    private String mHeadersLine;

    public Table(String headers, int count) {
        super();
        mHeadersLine = headers;
        mHeaders = headers.split("\\|");
        mValues = new String[count][mHeaders.length];
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

    public String getHeadersLineForSql() {
        return mHeadersLine.replaceAll("\\|", ",");
    }

    public void updateHeaders(String oldName, String newName) {
        mHeadersLine = mHeadersLine.replace(oldName, newName);
        mHeaders = mHeadersLine.split("\\|");
    }
}
