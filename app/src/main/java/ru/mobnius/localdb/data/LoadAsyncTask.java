package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteFullException;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileNotFoundException;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Progress, String> {

    private final OnLoadListener mListener;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private final String mTableName;

    public LoadAsyncTask(String tableName, OnLoadListener listener, Context context) {
        mListener = listener;
        mTableName = tableName;
        mContext = context;
        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Tags.CANCEL_TASK_TAG)) {
                    LoadAsyncTask.this.cancel(true);
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(
                mMessageReceiver, new IntentFilter(Tags.CANCEL_TASK_TAG));
    }

    @Override
    protected String doInBackground(String... strings) {
        String message = "";
            Loader loader = Loader.getInstance();
            boolean isAuthorized  = loader.auth(strings[0], strings[1]);
            if (!isAuthorized){
                message = "Неудалось получить ответ от сервера. Возможно пароль введен неверно, попробуйте авторизоваться повторно";
                return message;
            }
            // нужно ли просто добавлять или удалить все, а затем заливать
            boolean removeBeforeInsert = PreferencesManager.getInstance().getProgress() == null;

            int size = PreferencesManager.getInstance().getSize();
            Progress progress = PreferencesManager.getInstance().getProgress() != null ? PreferencesManager.getInstance().getProgress() : new Progress(0, 1, mTableName);
            if (!removeBeforeInsert) {
                progress.current = progress.current + size;
            }
            publishProgress(progress);

            DaoSession daoSession = HttpService.getDaoSession();
            RPCResult[] results;
            try {
                results = rpc(mTableName, progress.current, size);
            } catch (FileNotFoundException e) {
                Logger.error(e);
                message = e.getMessage();
                return message;
            }

            if (results == null) {
                message = "Не удалось установить соединение с сервером при старте загрузки";
                return message;
            }
            RPCResult result = results[0];
            int total = result.result.total;
            publishProgress(new Progress(progress.current, total, mTableName));
            File cacheDir = mContext.getCacheDir();
            if (cacheDir.getUsableSpace() * 100 / cacheDir.getTotalSpace() <= 6) {
                message = "Ошибка: в хранилище телефона осталось свободно менее 5%. Очистите место и повторите загрузку";
                return message;
            } else {
                try {
                    StorageUtil.processing(daoSession, result, mTableName, removeBeforeInsert);
                } catch (SQLiteFullException | SQLiteConstraintException e) {
                    message = e.getMessage();
                    Logger.error(e);
                    return message;
                }
            }
            for (int i = (progress.current + size); i < total; i += size) {
                if (PreferencesManager.getInstance().getProgress() == null) {
                    // значит принудительно все было остановлено
                    Intent intent = new Intent(Tags.CANCEL_TASK_TAG);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    break;
                }
                try {
                    results = rpc(mTableName, i, size);
                } catch (FileNotFoundException e) {
                    message = e.getMessage();
                    return message;
                }
                if (results == null) {
                    message = "Не удалось установить соединение с сервером в процессе загрузки";
                    return message;
                }
                result = results[0];


                if (cacheDir.getUsableSpace() * 100 / cacheDir.getTotalSpace() <= 6) {
                    message = "Ошибка: в хранилище телефона осталось свободно менее 5%. Удалось загрузить: " + i + " записей (очистите место и повторите загрузку)";
                    return message;
                } else {
                    try {
                        StorageUtil.processing(daoSession, result, mTableName, false);
                    } catch (SQLiteFullException | SQLiteConstraintException e) {
                        message = "Ошибка: недостаточно места в хранилище телефона. Удалось загрузить: " + i + " записей (" + e.getMessage() + ")";
                        return message;
                    }
                }
                publishProgress(new Progress(i, total, mTableName));
            }
            return message;
    }

    private RPCResult[] rpc(String tableName, int start, int limit) throws FileNotFoundException {
        RPCResult[] results = Loader.getInstance().rpc("Domain." + tableName, "Query", "[{ \"forceLimit\": true, \"start\": " + start + ", \"limit\": " + limit + ", \"sort\": [{ \"property\": \"LINK\", \"direction\": \"ASC\" }] }]");
        return results;
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
        Progress progress = values[0];
        PreferencesManager.getInstance().setProgress(progress);
        mListener.onLoadProgress(mTableName, progress);
    }

    @Override
    protected void onPostExecute(String message) {
        super.onPostExecute(message);
        if (message != null && !message.isEmpty()) {
            Intent intent = new Intent(Tags.ERROR_TAG);
            if (message.length() > 2000) {
                message = message.substring(0, 1000) + "\n...\n" + message.substring(message.length() - 1000, message.length() - 1);
            }
            intent.putExtra(Tags.ERROR_TEXT, message);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
