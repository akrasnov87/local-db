package ru.mobnius.localdb.data;

import android.os.AsyncTask;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Progress, Void> {
    private final OnLoadListener mListener;
    private final String mTableName;

    public LoadAsyncTask(String tableName, OnLoadListener listener) {
        mListener = listener;
        mTableName = tableName;
    }

    @Override
    protected Void doInBackground(String... strings) {
        Loader loader = Loader.getInstance();
        loader.auth(strings[0], strings[1]);
        // нужно ли просто добавлять или удалить все, а затем заливать
        boolean isAppend = PreferencesManager.getInstance().getProgress() != null;

        int size = PreferencesManager.getInstance().getSize();
        Progress progress = PreferencesManager.getInstance().getProgress() != null ? PreferencesManager.getInstance().getProgress() : new Progress(0, 1, mTableName);
        publishProgress(progress);

        DaoSession daoSession = HttpService.getDaoSession();

        RPCResult[] results = loader.rpc("Domain." + mTableName, "Query", "[{ \"start\": " + progress.current + ", \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]");
        RPCResult result = results[0];
        int total = result.result.total;
        publishProgress(new Progress(progress.current, total, mTableName));
        StorageUtil.processing(daoSession, result, mTableName, isAppend);

        for(int i = (progress.current + size); i < total; i += size) {
            if(PreferencesManager.getInstance().getProgress() == null) {
                // значит принудительно все было остановлено
                break;
            }
            results = loader.rpc("Domain." + mTableName, "Query", "[{ \"start\": " + i + ", \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]");
            result = results[0];
            StorageUtil.processing(daoSession, result, mTableName, isAppend);
            publishProgress(new Progress(i, total, mTableName));
        }
        return null;
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

        PreferencesManager.getInstance().setProgress(null);
        mListener.onLoadFinish(mTableName);
    }

    public interface OnLoadListener {
        /**
         * Прогресс выполнения
         * @param tableName имя таблицы
         * @param progress процент
         */
        void onLoadProgress(String tableName, Progress progress);

        /**
         * Результат загрузки данных
         * @param tableName имя таблицы
         */
        void onLoadFinish(String tableName);
    }
}
