package ru.mobnius.localdb.data.tablePack;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.zip.DataFormatException;

import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.storage.FiasDao;
import ru.mobnius.localdb.storage.RegistrPtsDao;

import static org.junit.Assert.*;

public class CsvUtilTest {

    private final String TAG = "CSV_TEST";

    @Test
    public void convert() {
        String input1 = "LINK|C_NAME\n123|Саша";
        Table table = CsvUtil.convert(input1);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 2);
        assertEquals(table.getValues().length, 1);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(0)[1], "Саша");

        String input2 = "LINK|C_NAME|C_NUMBER\n123||12";
        table = CsvUtil.convert(input2);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 1);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(0)[1], "");
        assertEquals(table.getValue(0)[2], "12");

        String input3 = "LINK|C_NAME|C_NUMBER\n|Саша|12";
        table = CsvUtil.convert(input3);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 1);
        assertEquals(table.getValue(0)[0], "");
        assertEquals(table.getValue(0)[1], "Саша");
        assertEquals(table.getValue(0)[2], "12");

        String input4 = "LINK|C_NAME|C_NUMBER\n123|Саша|";
        table = CsvUtil.convert(input4);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 1);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(0)[1], "Саша");
        assertNull(table.getValue(0)[2]);

        String input5 = "LINK|C_NAME|C_NUMBER\n123|Саша|\n12|Кирилл|1";
        table = CsvUtil.convert(input5);

        assertNull(CsvUtil.convert(null));
        assertNull(CsvUtil.convert(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 3);
        assertEquals(table.count(), 2);
        assertEquals(table.getValue(0)[0], "123");
        assertEquals(table.getValue(1)[0], "12");
    }

    @Test
    public void insert() throws IOException, DataFormatException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DaoSession mDaoSession = new DaoMaster(new DbOpenHelper(appContext, "csvDbName").getWritableDb()).newSession();
        mDaoSession.getFiasDao().deleteAll();
        mDaoSession.getFiasDao().detachAll();

        long start = new Date().getTime();
        byte[] buffer = CsvUtil.getFile("http://demo.it-serv.ru/repo", "UI_SV_FIAS", "1.2.641", 0, 10000);

        long unZipTime = new Date().getTime();
        Log.d(TAG, "loaded: " + (unZipTime - start));

        byte[] result = ZipManager.decompress(buffer);
        long convertTime = new Date().getTime();
        Log.d(TAG, "unzip: " + (convertTime - unZipTime));

        String txt = new String(result, StandardCharsets.UTF_8);
        long txtTime = new Date().getTime();
        Log.d(TAG, "to text: " + (txtTime - convertTime));

        Table table = CsvUtil.convert(txt);
        long insertTime = new Date().getTime();
        Log.d(TAG, "convert: " + (insertTime - txtTime));

        CsvUtil.insertToTable(table, FiasDao.TABLENAME, mDaoSession);
        long end = new Date().getTime();
        Log.d(TAG, "insert: " + (end - insertTime));

        Log.d(TAG, "all: " + (end - start));
    }

    @Test
    public void insert2() throws IOException, DataFormatException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DaoSession mDaoSession = new DaoMaster(new DbOpenHelper(appContext, "csvDbName").getWritableDb()).newSession();
        mDaoSession.getFiasDao().deleteAll();
        mDaoSession.getFiasDao().detachAll();

        long start = new Date().getTime();
        byte[] buffer = CsvUtil.getFile("http://demo.it-serv.ru/repo", "ED_Registr_Pts", "1.2.650", 1600000, 10000);

        long unZipTime = new Date().getTime();
        Log.d(TAG, "loaded: " + (unZipTime - start));

        byte[] result = ZipManager.decompress(buffer);
        long convertTime = new Date().getTime();
        Log.d(TAG, "unzip: " + (convertTime - unZipTime));

        String txt = new String(result, StandardCharsets.UTF_8);
        long txtTime = new Date().getTime();
        Log.d(TAG, "to text: " + (txtTime - convertTime));

        Table table = CsvUtil.convert(txt);
        long insertTime = new Date().getTime();
        Log.d(TAG, "convert: " + (insertTime - txtTime));

        CsvUtil.insertToTable(table, RegistrPtsDao.TABLENAME, mDaoSession);
        long end = new Date().getTime();
        Log.d(TAG, "insert: " + (end - insertTime));

        Log.d(TAG, "all: " + (end - start));
    }

    @Test
    public void readme() throws IOException {
        String input6 = "TABLE_NAME|TOTAL_COUNT|VERSION|DATE|FILE_COUNT|PART|SIZE\n" +
                "UI_SV_FIAS|2250414|1.2.641|2020-09-16T08:34:14.278Z|226|10000|66932823";
        Readme table = CsvUtil.convertReadme(input6);

        assertNull(CsvUtil.convertReadme(null));
        assertNull(CsvUtil.convertReadme(""));

        assertNotNull(table);
        assertEquals(table.getHeaders().length, 7);
        assertEquals(table.getValues().length, 1);
        assertEquals(table.getValue(0)[0], "UI_SV_FIAS");
        assertEquals(table.getValue(0)[1], "2250414");

        String result = CsvUtil.getReadme("http://demo.it-serv.ru/repo/csv-zip/UI_SV_FIAS/1.2.641");
        table = CsvUtil.convertReadme(result);
        assertNotNull(table);
        assertEquals(table.getHeaders().length, 7);
        assertEquals(table.getValues().length, 1);
        assertEquals(table.getValue(0)[0], "UI_SV_FIAS");
    }
}