package ru.mobnius.localdb.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlInsertFromString {
    private final String mTableName;
    private String[] mColumnPattern;
    private String[] mValues;
    private String params;
    private int mInsertsLength;

    public SqlInsertFromString(String zipString, String tableName) {
        mTableName = tableName;
        String[] dirtyArray = zipString.split("\n");
        mColumnPattern = dirtyArray[0].split("\\|");
        mValues = getPureArray(dirtyArray);
        mInsertsLength = mValues.length;
        StringBuilder paramsBuilder = new StringBuilder();
        for (int i = 0; i < mColumnPattern.length; i++) {
            paramsBuilder.append("?,");
        }
        params = paramsBuilder.substring(0, paramsBuilder.length() - 1);

    }

    public String convertToSqlQuery(int max) {
        StringBuilder builder = new StringBuilder();
        for (String field : mColumnPattern) {
            builder.append(field).append(",");
        }

        StringBuilder paramsBuilder = new StringBuilder();
        for (int j = 0; j < max; j++) {
            paramsBuilder.append("(").append(params).append("),");
        }

        return "INSERT INTO " + mTableName + "(" + builder.substring(0, builder.length() - 1) + ")" + " VALUES " + paramsBuilder.substring(0, paramsBuilder.length() - 1) + ";";
    }

    public List<Object> getValues() {
        List<Object> readyValues = new ArrayList<>(mInsertsLength);
        for (String field : mValues) {

            if (field.endsWith("|")) {
                field += " ";
            }
            String[] cellString = field.split("\\|");
            readyValues.addAll(Arrays.asList(cellString));
        }
        return readyValues;
    }

    public String[] getPureArray(String[] dirtyArray) {
        List<String> list = new ArrayList<>(Arrays.asList(dirtyArray));
        list.remove(0);
        return list.toArray(new String[0]);
    }
}
