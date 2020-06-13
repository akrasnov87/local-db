package ru.mobnius.localdb.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import ru.mobnius.localdb.model.fias.FiasResult;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.storage.Fias;

import static org.junit.Assert.*;

public class LoaderTest {

    private DaoSession mDaoSession;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mDaoSession = new DaoMaster(new DbOpenHelper(context, "loader-test.db").getWritableDb()).newSession();
        mDaoSession.getFiasDao().deleteAll();
    }

    @Test
    public void authTest() {
        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        assertTrue(loader.isAuthorized());
    }

    @Test
    public void versionTest() throws IOException {
        Loader loader = Loader.getInstance();
        String version = loader.version();
        assertNotEquals(version, "0.0.0.0");
    }

    @Test
    public void rpc() {
        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        FiasResult[] results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{}]", FiasResult[].class);
        assertNotNull(results);
        assertTrue(results[0].result.total > 0);
        results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[]", FiasResult[].class);
        assertNotNull(results);
        assertFalse(results[0].meta.success);

        results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": 0, \"limit\": 100, \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
        Fias first = results[0].result.records[0];
        results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": 100, \"limit\": 100, \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
        Fias two = results[0].result.records[0];
        assertNotEquals(first.LINK, two.LINK);
    }

    /*@Test
    public void loadAll() {
        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        int size = 100000;
        FiasResult[] results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": 0, \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
        int total = results[0].result.total;
        mDaoSession.getFiasDao().insertOrReplaceInTx(results[0].result.records);
        for(int i = size; i < total; i += size) {
            results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": " + i + ", \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
            mDaoSession.getFiasDao().insertOrReplaceInTx(results[0].result.records);
        }
        assertEquals(mDaoSession.getFiasDao().count(), total);
    }*/
}