package ru.mobnius.localdb.utils;

import android.database.Cursor;

public class SQLFieldTypeChecker {
    private static final int FIELD_TYPE_NULL = 0;
    private static final int FIELD_TYPE_INTEGER = 1;
    private static final int FIELD_TYPE_FLOAT = 2;
    private static final int FIELD_TYPE_STRING = 3;
    private static final int FIELD_TYPE_BLOB = 4;

    public static String getType(Cursor cursor, int columnIndex) {
        switch (cursor.getType(columnIndex)) {
            case FIELD_TYPE_NULL:
                return "null";
            case FIELD_TYPE_INTEGER:
                int x = cursor.getInt(columnIndex);
                return String.valueOf(x);
            case FIELD_TYPE_FLOAT:
                float y = cursor.getFloat(columnIndex);
                return String.valueOf(y);
            case FIELD_TYPE_STRING:
                return cursor.getString(columnIndex);
            case FIELD_TYPE_BLOB:
                return "blob";
        }
        return "";
    }
}
