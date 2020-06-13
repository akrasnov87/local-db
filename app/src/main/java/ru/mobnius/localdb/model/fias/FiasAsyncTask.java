package ru.mobnius.localdb.model.fias;

import android.os.AsyncTask;

import ru.mobnius.localdb.AutoRunReceiver;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.utils.Loader;

public class FiasAsyncTask extends AsyncTask<String, Progress, Void> {
    private OnFiasListener mListener;

    public FiasAsyncTask(OnFiasListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... strings) {
        AutoRunReceiver.getDaoSession().getFiasDao().deleteAll();
        Loader loader = Loader.getInstance();
        loader.auth(strings[0], strings[1]);
        String userId = loader.getUser().user.userId; // потом для фильтра нужно

        int size = 100000;
        FiasResult[] results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": 0, \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
        int total = results[0].result.total;
        publishProgress(new Progress(size, total));
        AutoRunReceiver.getDaoSession().getFiasDao().insertOrReplaceInTx(results[0].result.records);
        for(int i = size; i < total; i += size) {
            results = loader.rpc("Domain.UI_SV_FIAS", "Query", "[{ \"start\": " + i + ", \"limit\": " + size + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]", FiasResult[].class);
            AutoRunReceiver.getDaoSession().getFiasDao().insertOrReplaceInTx(results[0].result.records);
            publishProgress(new Progress(i, total));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);

        mListener.onFiasProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        mListener.onFiasLoaded();
    }

    public interface OnFiasListener {
        /**
         * Прогресс выполнения
         * @param progress процент
         */
        void onFiasProgress(Progress progress);

        /**
         * Результат загрузки данных
         */
        void onFiasLoaded();
    }
}
