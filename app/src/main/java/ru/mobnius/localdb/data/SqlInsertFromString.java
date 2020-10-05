package ru.mobnius.localdb.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlInsertFromString {
    private final String mTableName;
    private final String[] mColumnPattern;
    private final String[] mValues;
    private final String params;
    private final int mInsertsLength;

    public SqlInsertFromString(String zipString, String tableName) {
        mTableName = tableName;
        String[] dirtyArray = zipString.split("\n");
        mColumnPattern = dirtyArray[0].split("\\|");
        for (int i = 0; i < mColumnPattern.length; i++) {
            if (mColumnPattern[i].toLowerCase().equals("f_registr_pts___link")){
                mColumnPattern[i] = "F_Registr_Pts";
            }
        }
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

    public Object [] getValues() {
        List<Object> readyValues = new ArrayList<>(mInsertsLength);
        for (String field : mValues) {

            if (field.endsWith("|")) {
                field += " ";
            }
            String[] cellString = field.split("\\|");
            readyValues.addAll(Arrays.asList(cellString));
        }
        return readyValues.toArray();
    }

    public String[] getPureArray(String[] dirtyArray) {
        List<String> list = new ArrayList<>(Arrays.asList(dirtyArray));
        list.remove(0);
        return list.toArray(new String[0]);
    }
}
