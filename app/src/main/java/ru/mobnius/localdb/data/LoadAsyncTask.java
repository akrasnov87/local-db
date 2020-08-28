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

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

import ru.mobnius.localdb.App;
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
public class LoadAsyncTask extends AsyncTask<String, Integer, ArrayList<String>> implements StorageUtil.OnProgressUpdate{

    private final OnLoadListener mListener;
    @SuppressLint("StaticFieldLeak")
    private Context mApp;
    private final String mTableName;
    private int mTotal;
    private BroadcastReceiver mMessageReceiver;

    public LoadAsyncTask(String tableName, OnLoadListener listener, App app) {
        mListener = listener;
        mTableName = tableName;
        mApp = app;
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Tags.CANCEL_TASK_TAG)) {
                    LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mMessageReceiver);
                    LoadAsyncTask.this.cancel(false);
                }
            }
        };
        LocalBroadcastManager.getInstance(mApp).registerReceiver(
                mMessageReceiver, new IntentFilter(Tags.CANCEL_TASK_TAG));
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        ArrayList<String> message = new ArrayList<>();
        if (!isCancelled()) {


            Loader loader = Loader.getInstance();
            boolean isAuthorized = loader.auth(strings[0], strings[1]);
            if (!isAuthorized) {
                message.add(Tags.AUTH_ERROR);
                message.add("Не удалось получить ответ от сервера. Возможно пароль введен неверно, попробуйте авторизоваться повторно");
                return message;
            }

            boolean removeBeforeInsert = false;
            Progress progress;
            if (PreferencesManager.getInstance().getProgress() == null) {
                //удаляем прежде чем заливать
                removeBeforeInsert = true;
                progress = new Progress(0, 1, mTableName);
            } else {
                //восстанавливаем загрузку
                progress = PreferencesManager.getInstance().getProgress();
            }

            //количество записей с сервера за 1 rpc запрос
            int size = PreferencesManager.getInstance().getSize();

            PreferencesManager.getInstance().setProgress(progress);

            DaoSession daoSession = HttpService.getDaoSession();
            RPCResult[] results;
            try {
                results = rpc(mTableName, progress.current, size);
            } catch (FileNotFoundException e) {
                Logger.error(e);
                message.add(Tags.RPC_ERROR);
                message.add(e.getMessage());
                return message;
            }
            if (results == null) {
                message.add(Tags.RPC_ERROR);
                message.add("Не удалось установить соединение с сервером при старте загрузки");
                return message;
            }
            RPCResult result = results[0];
            mTotal = result.result.total;
            publishProgress(0);
            File cacheDir = mApp.getCacheDir();
            int spaceNeeded = isNotEnoughSpace(result, mTotal, size);
            if (cacheDir.getFreeSpace() / 1000000 < spaceNeeded) {
                message.add(Tags.STORAGE_ERROR);
                message.add("Ошибка: в хранилище телефона недостаточно свободного места для загрузки таблицы(" +
                        mTableName + ", необходимо около " + spaceNeeded + " МБ). Очистите место и повторите загрузку");
                return message;
            }
            PreferencesManager.getInstance().setRemoteRowCount(String.valueOf(mTotal), mTableName);
            PreferencesManager.getInstance().setProgress(new Progress(progress.current, mTotal, mTableName));

            try {
                StorageUtil.processing(daoSession, result, mTableName, removeBeforeInsert, this);
            } catch (SQLiteFullException | SQLiteConstraintException e) {
                message.add(Tags.SQL_ERROR);
                message.add(e.getMessage());
                Logger.error(e);
                return message;
            }

            for (int i = (progress.current + size); i < mTotal; i += size) {
                if (isCancelled()) {
                    return message;
                }
                if (PreferencesManager.getInstance().getProgress() == null) {
                    // значит принудительно все было остановлено
                    Intent intent = new Intent(Tags.CANCEL_TASK_TAG);
                    LocalBroadcastManager.getInstance(mApp).sendBroadcast(intent);
                    break;
                }

                try {
                    results = rpc(mTableName, i, size);
                } catch (FileNotFoundException e) {
                    Logger.error(e);
                    message.add(Tags.RPC_ERROR);
                    message.add(e.getMessage());
                    return message;
                }
                if (results == null) {
                    message.add(Tags.RPC_ERROR);
                    message.add("Не удалось установить соединение с сервером в процессе загрузки");
                    return message;
                }

                result = results[0];
                try {
                    StorageUtil.processing(daoSession, result, mTableName, false, this);
                } catch (SQLiteFullException | SQLiteConstraintException e) {
                    Logger.error(e);
                    message.add(Tags.SQL_ERROR);
                    message.add(e.getMessage());
                    return message;

                }
                PreferencesManager.getInstance().setProgress(new Progress(i, mTotal, mTableName));
            }
            createIndexes(mTableName, HttpService.getDaoSession().getDatabase());
        }
        return message;
    }

    private RPCResult[] rpc(String tableName, int start, int limit) throws FileNotFoundException {
        String sortColumn = "\"LINK\"";
        if (tableName.toLowerCase().equals("ui_sv_fias")) {
            sortColumn = "\"C_Full_Address\"";
        }
        RPCResult[] results = Loader.getInstance().rpc("Domain." + tableName, "Query", "[{ \"forceLimit\": true, \"start\": " + start + ", \"limit\": " + limit + ", \"sort\": [{ \"property\": " + sortColumn + ", \"direction\": \"ASC\" }] }]");
        return results;
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        mListener.onLoadProgress(mTableName, progress, mTotal);
    }

    @Override
    protected void onPostExecute(ArrayList<String> message) {
        super.onPostExecute(message);
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mMessageReceiver);
        if (message != null && !message.isEmpty()) {
            Intent intent = new Intent(Tags.ERROR_TAG);
            intent.putExtra(Tags.ERROR_TYPE, message.get(0));
            intent.putExtra(Tags.ERROR_TEXT, message.get(1));
            LocalBroadcastManager.getInstance(mApp).sendBroadcast(intent);
        }
        PreferencesManager.getInstance().setProgress(null);
        mListener.onLoadFinish(mTableName);
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onUpdateProgress(int progress) {
        publishProgress(progress);
    }

    private void createIndexes(String tableName, Database db) {
        if ("ui_sv_fias".equals(tableName.toLowerCase())) {
            db.execSQL("CREATE INDEX " + "IDX_UI_SV_FIAS_C_Full_Address ON \"UI_SV_FIAS\"" +
                    " (\"C_Full_Address\" ASC);");
            /*
            db.execSQL("CREATE INDEX " + "EXISTS IDX_UI_SV_FIAS_F_Structure ON \"UI_SV_FIAS\"" +
                    " (\"F_Structure\" ASC);");
            db.execSQL("CREATE INDEX  " + "IDX_UI_SV_FIAS_F_Municipality ON \"UI_SV_FIAS\"" +
                    " (\"F_Municipality\" ASC);");
            db.execSQL("CREATE INDEX " + "IDX_UI_SV_FIAS_F_Town ON \"UI_SV_FIAS\"" +
                    " (\"F_Town\" ASC);");
             */
        }
    }

    private int isNotEnoughSpace(RPCResult rpcResult, int totalSize, int size) {
        float oneResultSize = (float) (Arrays.toString(rpcResult.result.records).length() / 1024) / 1024;
        float totalResultsCount = (float) totalSize / size;
        float spaceNeeded = totalResultsCount * oneResultSize;
        return (int) spaceNeeded + 100;
    }


    public interface OnLoadListener {
        /**
         * Прогресс выполнения
         *
         * @param tableName имя таблицы
         * @param progress  процент
         */
        void onLoadProgress(String tableName, int progress, int total);

        /**
         * Результат загрузки данных
         *
         * @param tableName имя таблицы
         */
        void onLoadFinish(String tableName);
    }


}
