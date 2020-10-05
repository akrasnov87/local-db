package ru.mobnius.localdb.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.storage.FiasDao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StorageUtilTest {

    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
    @Test
    public void getStorageNames() {
        StorageName[] names = StorageUtil.getStorage(mContext, "ru.mobnius.localdb.storage");
        assertTrue(names.length > 0);
    }
    @Test
    public void getResults() throws JSONException {
        DaoSession daoSession =  new DaoMaster(new DbOpenHelper(mContext, "local-db.db").getWritableDb()).newSession();
        Database database = daoSession.getDatabase();
        database.execSQL("delete from " + FiasDao.TABLENAME);

        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        RPCResult[] results = loader.rpc("Domain." + FiasDao.TABLENAME, "Query", "[{ \"start\": 0, \"limit\": " + 1000 + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]");

        JSONArray arrays = StorageUtil.getResults(database, "select count(*) from " + FiasDao.TABLENAME);
        String result = arrays.toString(0);
        assertEquals(result, "[\n" +
                "{\n" +
                "\"count(*)\": \"1000\"\n" +
                "}\n" +
                "]");
    }
}