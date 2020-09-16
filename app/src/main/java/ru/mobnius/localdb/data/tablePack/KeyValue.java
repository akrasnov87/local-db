package ru.mobnius.localdb.data.tablePack;

public class KeyValue {
    public KeyValue() {}
    public KeyValue(String key, String value) {
        super();
        mKey = key;
        mValue = value;
    }
    private String mKey;
    private String mValue;

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }
}
