package ru.mobnius.localdb.data;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteFullException;
import android.os.AsyncTask;
import android.text.style.StyleSpan;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.request.SyncRequestListener;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Progress, Void> {
    private final OnLoadListener mListener;
    private final SyncRequestListener.OnSpaceOver mSpaceOverListener;
    private final String mTableName;
    private String isError = "";

    public LoadAsyncTask(String tableName, OnLoadListener listener, SyncRequestListener.OnSpaceOver spaceOverListener) {
        mListener = listener;
        mTableName = tableName;
        mSpaceOverListener = spaceOverListener;
    }

    @Override
    protected Void doInBackground(String... strings) {
        Loader loader = Loader.getInstance();
        loader.auth(strings[0], strings[1]);
        // нужно ли просто добавлять или удалить все, а затем заливать
        boolean removeBeforeInsert = PreferencesManager.getInstance().getProgress() == null;

        int size = PreferencesManager.getInstance().getSize();
        Progress progress = PreferencesManager.getInstance().getProgress() != null ? PreferencesManager.getInstance().getProgress() : new Progress(0, 1, mTableName);
        if (!removeBeforeInsert) {
            progress.current = progress.current + size;
        }
        publishProgress(progress);

        DaoSession daoSession = HttpService.getDaoSession();

        RPCResult[] results = rpc(mTableName, progress.current, size);
        RPCResult result = results[0];
        int total = result.result.total;
        publishProgress(new Progress(progress.current, total, mTableName));
        try {
            StorageUtil.processing(daoSession, result, mTableName, removeBeforeInsert);
        } catch (SQLiteFullException | SQLiteConstraintException e) {
            isError = e.getMessage();
            Logger.error(e);
            return null;
        }
        for (int i = (progress.current + size); i < total; i += size) {
            if (PreferencesManager.getInstance().getProgress() == null) {
                // значит принудительно все было остановлено
                break;
            }
            results = rpc(mTableName, i, size);
            if (results == null) {
                break;
            }
            result = results[0];
            try {
                StorageUtil.processing(daoSession, result, mTableName, false);
            } catch (SQLiteFullException | SQLiteConstraintException e) {
                isError = "Ошибка: недостаточно места в хранилище телефона. Удалось загрузить: " + i + " записей (" + e.getMessage() + ")";
                return null;
            }
            publishProgress(new Progress(i, total, mTableName));
        }
        return null;
    }

    private RPCResult[] rpc(String tableName, int start, int limit) {
        return Loader.getInstance().rpc("Domain." + tableName, "Query", "[{ \"forceLimit\": true, \"start\": " + start + ", \"limit\": " + limit + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]");
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
        Progress progress = values[0];
        PreferencesManager.getInstance().setProgress(progress);
        mListener.onLoadProgress(mTableName, progress);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!isError.isEmpty()) {
            mSpaceOverListener.onSpaceFinished(isError);
        }
        PreferencesManager.getInstance().setProgress(null);
        mListener.onLoadFinish(mTableName);
    }

    public interface OnLoadListener {
        /**
         * Прогресс выполнения
         *
         * @param tableName имя таблицы
         * @param progress  процент
         */
        void onLoadProgress(String tableName, Progress progress);

        /**
         * Результат загрузки данных
         *
         * @param tableName имя таблицы
         */
        void onLoadFinish(String tableName);
    }
}
