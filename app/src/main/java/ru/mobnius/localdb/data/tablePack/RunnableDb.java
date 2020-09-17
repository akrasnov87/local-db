package ru.mobnius.localdb.data.tablePack;

import android.os.AsyncTask;
import android.util.Log;

import java.nio.charset.StandardCharsets;

import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.FiasDao;

public class RunnableDb implements Runnable {

    private DaoSession mDaoSession;
    private OnRunnableDbListeners mListeners;
    private boolean isStopped = false;

    public RunnableDb(DaoSession daoSession, OnRunnableDbListeners listeners) {
        mListeners = listeners;
        mDaoSession = daoSession;
    }

    @Override
    public void run() {

    }

    public void insert(byte[] buffer) {
        if(buffer != null) {
            try {
                if(!isStopped) {
                    byte[] result = ZipManager.decompress(buffer);
                    String txt = new String(result, StandardCharsets.UTF_8);
                    Table table = CsvUtil.convert(txt);
                    CsvUtil.insertToTable(table, FiasDao.TABLENAME, mDaoSession);
                    Log.d(PackManager.TAG, "архив обработан");
                }
            } catch (Exception e) {
                mListeners.onError(e.getMessage());
            }
        }
    }

    public void stop() {
        isStopped = true;
    }

    public interface OnRunnableDbListeners {
        /**
         * Ошибка
         * @param message
         */
        void onError(String message);
    }
}
