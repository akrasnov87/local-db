package ru.mobnius.localdb.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.storage.FiasDao;

import static org.junit.Assert.*;

public class LoaderTest {

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DaoSession daoSession = new DaoMaster(new DbOpenHelper(context, "loader-test.db").getWritableDb()).newSession();
        daoSession.getFiasDao().deleteAll();
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
    public void rpc() throws JSONException {
        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        RPCResult[] results = loader.rpc("Domain." + FiasDao.TABLENAME, "Query", "[{}]");
        assertNotNull(results);
        assertTrue(results[0].result.total > 0);
        results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[]");
        assertNotNull(results);
        assertFalse(results[0].meta.success);

        results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": 0, \"limit\": 100, \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]");
        JSONObject first = results[0].result.records[0];
        results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": 100, \"limit\": 100, \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]");
        JSONObject two = results[0].result.records[0];
        assertNotEquals(first.getString("LINK"), two.getString("LINK"));
    }
}