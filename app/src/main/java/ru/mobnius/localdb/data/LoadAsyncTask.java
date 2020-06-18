package ru.mobnius.localdb.data;

import android.os.AsyncTask;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.fias.FiasResult;
import ru.mobnius.localdb.utils.Loader;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Progress, Void> {
    private OnLoadListener mListener;
    private String mTableName;

    public LoadAsyncTask(String tableName, OnLoadListener listener) {
        mListener = listener;
        mTableName = tableName;
    }

    @Override
    protected Void doInBackground(String... strings) {
        int size = PreferencesManager.getInstance().getSize();
        Progress progress = PreferencesManager.getInstance().getProgress() != null ? PreferencesManager.getInstance().getProgress() : new Progress(0, 1, mTableName);
        publishProgress(progress);

        if(PreferencesManager.getInstance().getProgress() == null) {
            HttpService.getDaoSession().getFiasDao().deleteAll();
        }

        Loader loader = Loader.getInstance();
        loader.auth(strings[0], strings[1]);

        FiasResult[] results = loader.rpc("Domain." + mTableName, "Query", "[{ \"start\": " + progress.current + ", \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
        int total = results[0].result.total;
        publishProgress(new Progress(progress.current, total, mTableName));
        HttpService.getDaoSession().getFiasDao().insertOrReplaceInTx(results[0].result.records);

        for(int i = (progress.current + size); i < total; i += size) {
            if(PreferencesManager.getInstance().getProgress() == null) {
                // значит принудительно все было остановлено
                break;
            }
            results = loader.rpc("Domain." + mTableName, "Query", "[{ \"start\": " + i + ", \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
            HttpService.getDaoSession().getFiasDao().insertOrReplaceInTx(results[0].result.records);
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
