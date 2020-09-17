package ru.mobnius.localdb.data.tablePack;

import android.annotation.SuppressLint;
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

    private List<byte[]> mCaches = new ArrayList<>(CACHE_SIZE);

    private OnRunnableLoadListeners mListeners;
    private LoadAsyncTask mLoadAsyncTask;
    private DaoSession mDaoSession;
    private DbAsyncTask mDbAsyncTask;
    private int mDbCount = 0;
    private String mTableName;

    public RunnableLoad(OnRunnableLoadListeners listener, DaoSession daoSession, String baseUrl, String tableName, int start) {
        mListeners = listener;

        mTableName = tableName;
        mDaoSession = daoSession;
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

    public byte[] getNext() {
        if(mCaches.size() > 0) {
            byte[] buffer = mCaches.get(0);
            byte[] copy = Arrays.copyOf(buffer, buffer.length);
            mCaches.remove(0);

            if(mCaches.size() == 0) {
                mListeners.onBufferEmpty();
            }
            load();
            return copy;
        }

        return null;
    }

    private void writeToBuffer(byte[] bytes) {
        mCaches.add(bytes);
        mListeners.onBufferInsert(mCaches.size());

        if(mCaches.size() == CACHE_SIZE) {
            mListeners.onBufferSuccess(CACHE_SIZE);
        } else {
            load();
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
    private class DbAsyncTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... bytes) {
            byte[] buffer = bytes[0];
            if(buffer != null) {
                try {
                    long dStart = new Date().getTime();
                    byte[] result = ZipManager.decompress(buffer);
                    String txt = new String(result, StandardCharsets.UTF_8);
                    Table table = CsvUtil.convert(txt);

                    boolean insertedResult = CsvUtil.insertToTable(table, mTableName, mDaoSession);
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
