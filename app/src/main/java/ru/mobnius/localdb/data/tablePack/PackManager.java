package ru.mobnius.localdb.data.tablePack;

import ru.mobnius.localdb.storage.DaoSession;

/**
 * Упраление загрузкой архивами
 */
public class PackManager {

    public final static String TAG = "PACK";

    private final DaoSession mSQLiteDatabase;
    private final String mBaseUrl;
    private final String mTableName;
    private final String mVersion;

    private RunnableLoad mRunnableLoad;

    private Thread mLoadThread;

    public PackManager(DaoSession sqLiteDatabase, String baseUrl, String tableName, String version) {
        mSQLiteDatabase = sqLiteDatabase;
        mBaseUrl = baseUrl;
        mTableName = tableName;
        mVersion = version;
    }

    public void start(OnRunnableLoadListeners listener, int start) {

        mRunnableLoad = new RunnableLoad(listener, mSQLiteDatabase, mBaseUrl + "/csv-zip/" + mTableName + "/" + mVersion, mTableName, start);

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
