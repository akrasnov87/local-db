package ru.mobnius.localdb.utils;

import android.database.Cursor;

public class SQLFieldTypeChecker {
    static final int FIELD_TYPE_NULL = 0;
    static final int FIELD_TYPE_INTEGER = 1;
    static final int FIELD_TYPE_FLOAT = 2;
    static final int FIELD_TYPE_STRING = 3;
    static final int FIELD_TYPE_BLOB = 4;

    public static Object getType(Cursor cursor, int columnIndex) {
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
                return cursor.getBlob(columnIndex);
        }
        return null;
    }
}
