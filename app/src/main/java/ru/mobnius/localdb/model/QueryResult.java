package ru.mobnius.localdb.model;

public class QueryResult {

    private String mValues;
    private byte[] mBytes;

    public QueryResult(String values, byte[] bytes) {
        mValues = values;
        mBytes = bytes;
    }

    public String getValues() {
        return mValues;
    }

    public byte[] getBytes() {
        return mBytes;
    }
}
