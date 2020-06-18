package ru.mobnius.localdb.utils;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.JsonArray;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.model.fias.FiasResult;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.storage.FiasDao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StorageUtilTest {

    private Context mContext;
    private Database mDatabase;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DaoSession daoSession =  new DaoMaster(new DbOpenHelper(mContext, "local-db.db").getWritableDb()).newSession();
        mDatabase = daoSession.getDatabase();
        mDatabase.execSQL("delete from " + FiasDao.TABLENAME);

        Loader loader = Loader.getInstance();
        loader.auth("iserv", "iserv");
        FiasResult[] results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": 0, \"limit\": " + 1000 + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
        daoSession.getFiasDao().insertOrReplaceInTx(results[0].result.records);
    }
    @Test
    public void getStorageNames() {
        StorageName[] names = StorageUtil.getStorage(mContext, "ru.mobnius.localdb.storage");
        assertTrue(names.length > 0);
    }
    @Test
    public void getResults() throws JSONException {
        JSONArray arrays = StorageUtil.getResults(mDatabase, "select count(*) from " + FiasDao.TABLENAME);
        String result = arrays.toString(0);
        assertEquals(result, "[\n" +
                "{\n" +
                "\"count(*)\": \"1000\"\n" +
                "}\n" +
                "]");
    }
}