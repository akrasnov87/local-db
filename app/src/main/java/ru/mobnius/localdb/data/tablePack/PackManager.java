package ru.mobnius.localdb.data.tablePack;

import android.util.Log;

import java.util.Date;

import ru.mobnius.localdb.storage.DaoSession;

/**
 * Упраление загрузкой архивами
 */
public class PackManager {

    public static String TAG = "PACK";

    private DaoSession mDaoSession;
    private String mBaseUrl;
    private String mTableName;
    private String mVersion;

    private RunnableLoad mRunnableLoad;

    private Thread mLoadThread;

    public PackManager(DaoSession daoSession, String baseUrl, String tableName, String version) {
        mDaoSession = daoSession;
        mBaseUrl = baseUrl;
        mTableName = tableName;
        mVersion = version;
    }

    public void start(OnRunnableLoadListeners listener, int start) {

        mRunnableLoad = new RunnableLoad(listener, mDaoSession, mBaseUrl + "/csv-zip/" + mTableName + "/" + mVersion, mTableName, start);

        mLoadThread = new Thread(mRunnableLoad);
        mLoadThread.start();
    }

    public void destroy() {
        if(mRunnableLoad != null) {
            mRunnableLoad.stop();
        }

        if(mLoadThread != null) {
            mLoadThread.interrupt();
        }
    }
}
