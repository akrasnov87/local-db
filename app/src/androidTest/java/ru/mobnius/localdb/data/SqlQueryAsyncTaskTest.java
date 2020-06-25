package ru.mobnius.localdb.data;

import android.content.Context;
import android.content.Intent;

import org.greenrobot.greendao.database.Database;
import org.junit.Before;
import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;

import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DbOpenHelper;

import static org.junit.Assert.*;

public class SqlQueryAsyncTaskTest {
    private final static String TABLE_NAME = "NewTestTable";
    private final static String COLUMN_1 = "column1";
    private final static String COLUMN_2 = "column2";
    private final static String COLUMN_3 = "column3";
    private final static String VALUE_1 = "'value1'";
    private final static String VALUE_2 = "'value2'";
    private final static String VALUE_3 = "'value3'";

    private Database db;
    private String dbName;
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbName = getClass().getName();
        mContext.deleteDatabase(dbName);
        db = new DaoMaster(new DbOpenHelper(mContext, dbName).getWritableDb()).newSession().getDatabase();
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_1 + " text not null, " + COLUMN_2 + " text not null, " + COLUMN_3 + " text not null);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_1 + ", " + COLUMN_2 + ", " + COLUMN_3 + ") VALUES (" + VALUE_1 + ", " + VALUE_2 + ", " + VALUE_3 + ");");
    }

    @Test
    public void doInBackground() {
        SqlQueryAsyncTask task = new SqlQueryAsyncTask(db, null);
        String s = task.doInBackground("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_1 + " = " + VALUE_1);
        String c = "{\n    \"column1\": \"value1\",\n    \"column2\": \"value2\",\n    \"column3\": \"value3\"\n}";
        assertTrue(s.contentEquals(c));
        String e = task.doInBackground("SELECT column4 FROM " + TABLE_NAME);
        assertTrue(e.contentEquals("android.database.sqlite.SQLiteException: no such column: column4 (code 1): , while compiling: SELECT column4 FROM NewTestTable"));
        mContext.deleteDatabase(dbName);
    }
}