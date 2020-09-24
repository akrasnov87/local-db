package ru.mobnius.localdb.data.tablePack;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.FiasDao;

public class RunnableLoad implements Runnable {
    private boolean isStopped = false;

    private String mBaseUrl;
    private int mTotal;
    private int mStart;
    private int mLimit;

    public static int CACHE_SIZE = 2;

    private List<Table> mCaches = new ArrayList<>(CACHE_SIZE);

    private OnRunnableLoadListeners mListeners;
    private LoadAsyncTask mLoadAsyncTask;
    private DaoSession mSQLiteDatabase;
    private DbAsyncTask mDbAsyncTask;
    private int mDbCount = 0;
    private String mTableName;

    public RunnableLoad(OnRunnableLoadListeners listener, DaoSession sqLiteDatabase, String baseUrl, String tableName, int start) {
        mListeners = listener;

        mTableName = tableName;
        mSQLiteDatabase = sqLiteDatabase;
        mBaseUrl = baseUrl;
        mStart = start;
    }

    @Override
    public void run() {
        try {
            String s = CsvUtil.getReadme(mBaseUrl);
            Readme readme = CsvUtil.convertReadme(s);

            mTotal = Integer.parseInt(readme.getValue(0)[1]);
            mLimit = Integer.parseInt(readme.getValue(0)[5]);
            Log.d(PackManager.TAG, "readme.txt загружен");

            load();

            mDbAsyncTask = new DbAsyncTask();
            mDbAsyncTask.execute(getNext());
        } catch (IOException e) {
            mListeners.onError("Ошибка чтения информации о архиве.");
        }
    }

    public void load() {
        if(isStopped) {
            return;
        }
        if(mTotal > mStart && mCaches.size() <= CACHE_SIZE) {
            mListeners.onProgress(mStart, mTotal);

            String baseUrl = mBaseUrl + "/" + mStart + "-" + (mStart + mLimit) + ".zip";
            mStart += mLimit;
            mLoadAsyncTask = new LoadAsyncTask();
            mLoadAsyncTask.execute(baseUrl);
        }
    }

    public Table getNext() {
        if(mCaches.size() > 0) {
            Table txt = mCaches.get(0);
            mCaches.remove(0);

            if(mCaches.size() == 0) {
                mListeners.onBufferEmpty();
            }
            load();
            return txt;
        }

        return null;
    }

    private void writeToBuffer(byte[] bytes) {
        try {
            byte[] result = ZipManager.decompress(bytes);
            String txt = new String(result, StandardCharsets.UTF_8);
            Table table = CsvUtil.convert(txt);
            mCaches.add(table);
            mListeners.onBufferInsert(mCaches.size());

            if (mCaches.size() == CACHE_SIZE) {
                mListeners.onBufferSuccess(CACHE_SIZE);
            } else {
                load();
            }
        }catch (Exception e) {
            mListeners.onError(e.getMessage());
        }
    }

    public void stop() {
        isStopped = true;

        if(mLoadAsyncTask != null) {
            mLoadAsyncTask.cancel(true);
            mLoadAsyncTask = null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadAsyncTask extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... strings) {
            return CsvUtil.getFile(strings[0]);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);

            writeToBuffer(bytes);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DbAsyncTask extends AsyncTask<Table, Void, Void> {

        @Override
        protected Void doInBackground(Table... txt) {
            Table table = txt[0];
            if(table != null) {
                try {
                    long dStart = new Date().getTime();
                    boolean insertedResult = CsvUtil.insertToTable(table, mTableName, mSQLiteDatabase);
                    long dEnd = new Date().getTime();
                    mDbCount += table.count();
                    Log.d(PackManager.TAG, "inserted: " + mDbCount + "("+insertedResult+")" + " за " + (dEnd - dStart));
                } catch (Exception e) {
                    mListeners.onError(e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(mDbCount >= mTotal) {
                mListeners.onLoaded();
            } else {
                mDbAsyncTask = new DbAsyncTask();
                mDbAsyncTask.execute(getNext());
            }
        }
    }
}
