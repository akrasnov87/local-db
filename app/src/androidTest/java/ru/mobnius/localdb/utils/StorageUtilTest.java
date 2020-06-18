package ru.mobnius.localdb.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StorageUtilTest {

    private DaoSession mDaoSession;
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mDaoSession = new DaoMaster(new DbOpenHelper(mContext, "local-db.db").getWritableDb()).newSession();
    }
    @Test
    public void getStorageNames() {
        StorageName[] names = StorageUtil.getStorage(mContext, "ru.mobnius.localdb.storage");
        assertTrue(names.length > 0);
    }
}