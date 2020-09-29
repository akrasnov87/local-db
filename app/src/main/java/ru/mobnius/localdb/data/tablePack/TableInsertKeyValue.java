package ru.mobnius.localdb.data.tablePack;

public class TableInsertKeyValue {
    public String getTableName() {
        return mTableName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    private final String mTableName;
    private final String mFilePath;

    public TableInsertKeyValue(String tableName, String filePath) {
        mTableName = tableName;
        mFilePath = filePath;
    }
}
