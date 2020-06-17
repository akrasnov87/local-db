package ru.mobnius.localdb.model.fias;

import android.os.AsyncTask;

import ru.mobnius.localdb.AutoRunReceiver;
import ru.mobnius.localdb.model.Progress;
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
        publishProgress(new Progress(0, 1));
        AutoRunReceiver.getDaoSession().getFiasDao().deleteAll();

        Loader loader = Loader.getInstance();

        loader.auth(strings[0], strings[1]);

        int size = 10;
        FiasResult[] results = loader.rpc("Domain." + mTableName, "Query", "[{ \"start\": 0, \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
        int total = results[0].result.total;
        publishProgress(new Progress(0, total));
        AutoRunReceiver.getDaoSession().getFiasDao().insertOrReplaceInTx(results[0].result.records);
        for(int i = size; i < total; i += size) {
            results = loader.rpc("Domain." + mTableName, "Query", "[{ \"start\": " + i + ", \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
            AutoRunReceiver.getDaoSession().getFiasDao().insertOrReplaceInTx(results[0].result.records);
            publishProgress(new Progress(i, total));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);

        mListener.onLoadProgress(mTableName, values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

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
