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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.DataFormatException;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.data.tablePack.CsvUtil;
import ru.mobnius.localdb.data.tablePack.Table;
import ru.mobnius.localdb.data.tablePack.ZipManager;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.FiasDao;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Integer, ArrayList<String>> implements StorageUtil.OnProgressUpdate {

    private final OnLoadListener mListener;
    @SuppressLint("StaticFieldLeak")
    private Context mApp;
    private final String[] mTableName;
    private String mCurrentTableName;
    private int mTotal;
    private BroadcastReceiver mMessageReceiver;

    public LoadAsyncTask(String[] tableName, OnLoadListener listener, App app) {
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
        JSONObject infoTables = CsvUtil.getInfo(PreferencesManager.getInstance().getRepoUrl());
        ArrayList<String> message = new ArrayList<>();
        if (!isCancelled() && mTableName != null) {
            for (int j = 0; j < mTableName.length; j++) {
                //количество записей с сервера за 1 rpc запрос
                int size;
                int hddSize;
                mCurrentTableName = mTableName[j];
                String mVersion;
                JSONObject tableInfo;

                try {
                    tableInfo = infoTables.getJSONArray(mCurrentTableName).getJSONObject(0);
                    mVersion = tableInfo.getString("VERSION");
                    size = Integer.parseInt(tableInfo.getString("PART"));
                    mTotal = Integer.parseInt(tableInfo.getString("TOTAL_COUNT"));
                    hddSize = Integer.parseInt(tableInfo.getString("SIZE")) / 1024 / 1024;
                } catch (JSONException e) {
                    message.add(Tags.CRITICAL_ERROR);
                    message.add("Ошибка чтения информации о таблице " + e.toString());
                    return message;
                }
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
                    progress = new Progress(0, mTotal, mTableName[j], mVersion);
                } else {
                    //восстанавливаем загрузку
                    progress = PreferencesManager.getInstance().getProgress();
                }

                PreferencesManager.getInstance().setProgress(progress);

                DaoSession daoSession = HttpService.getDaoSession();
                /*RPCResult[] results;
                try {
                    results = rpc(mTableName[j], progress.current, size);
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
                mTotal = result.result.total;*/
                publishProgress(0);

                File cacheDir = mApp.getCacheDir();
                int spaceNeeded = hddSize; // getRequiredSpaceSize(result, mTotal, size);
                if (cacheDir.getFreeSpace() / 1000000 < hddSize) {
                    message.add(Tags.STORAGE_ERROR);
                    message.add("Ошибка: в хранилище телефона недостаточно свободного места для загрузки таблицы(" +
                            mTableName + ", необходимо около " + spaceNeeded + " МБ). Очистите место и повторите загрузку");
                    return message;
                }
                PreferencesManager.getInstance().setRemoteRowCount(String.valueOf(mTotal), mTableName[j]);
                PreferencesManager.getInstance().setProgress(new Progress(progress.current, mTotal, mTableName[j], mVersion));

                try {

                    Database db = daoSession.getDatabase();
                    if (removeBeforeInsert) {
                        db.execSQL("delete from " + mTableName[j]);
                        PreferencesManager.getInstance().setLocalRowCount("0", mTableName[j]);
                    }

                    //StorageUtil.processing(size, mTableName[j], this);
                } catch (SQLiteFullException | SQLiteConstraintException e) {
                    message.add(Tags.SQL_ERROR);
                    message.add(e.getMessage());
                    Logger.error(e);
                    return message;
                }

                for (int i = progress.current; i < mTotal; i += size) {
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
                        byte[] buffer = CsvUtil.getFile(PreferencesManager.getInstance().getRepoUrl(), mCurrentTableName, mVersion, i, size);
                        byte[] result = ZipManager.decompress(buffer);
                        String txt = new String(result, StandardCharsets.UTF_8);
                        Table table = CsvUtil.convert(txt);
                        table.updateHeaders("F_Registr_Pts___LINK", "F_Registr_Pts");
                        CsvUtil.insertToTable(table, mCurrentTableName, daoSession);
                        //results = rpc(mTableName[j], i, size);
                    } catch (IOException | DataFormatException e) {
                        Logger.error(e);
                        message.add(Tags.RPC_ERROR);
                        message.add(e.getMessage());
                        return message;
                    }
                    /*if (results == null) {
                        message.add(Tags.RPC_ERROR);
                        message.add("Не удалось установить соединение с сервером в процессе загрузки");
                        return message;
                    }

                    result = results[0];
                    try {
                        StorageUtil.processing(daoSession, result, mTableName[j], false, this);
                    } catch (SQLiteFullException | SQLiteConstraintException e) {
                        Logger.error(e);
                        message.add(Tags.SQL_ERROR);
                        message.add(e.getMessage());
                        return message;

                    }*/
                    StorageUtil.processing(size, mTableName[j], this);
                    PreferencesManager.getInstance().setProgress(new Progress(i, mTotal, mTableName[j], mVersion));
                }
                createIndexes(mTableName[j], HttpService.getDaoSession().getDatabase());
                ArrayList<String> list = new ArrayList<>(Arrays.asList(mTableName));
                list.remove(mCurrentTableName);
                String[] allTablesArray = list.toArray(new String[0]);
                PreferencesManager.getInstance().setAllTablesArray(allTablesArray);
                PreferencesManager.getInstance().setProgress(null);
            }
        }
        return message;
    }

    private RPCResult[] rpc(String tableName, int start, int limit) throws FileNotFoundException {
        String sortColumn = "\"LINK\"";
        if (tableName.toLowerCase().equals("ui_sv_fias")) {
            sortColumn = "\"C_Full_Address\"";
        }
        String bDisabled = "";
        if (tableName.toLowerCase().equals("ed_device_billing")||tableName.toLowerCase().equals("ed_registr_pts")){
            bDisabled =  " \"filter\":[{\"property\":\"B_Disable\", \"value\":\"0\", \"operator\":\"=\" }]";
        }
        RPCResult[] results = Loader.getInstance().rpc("Domain." + tableName, "Query", "[{ \"forceLimit\": true, \"start\": "+
                start + ", \"limit\": " + limit + ", \"sort\": [{ \"property\": " + sortColumn + ", \"direction\": \"ASC\" }]," + bDisabled +"}]");
        return results;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        mListener.onLoadProgress(mCurrentTableName, progress, mTotal);
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
        if(mTableName != null) {
            PreferencesManager.getInstance().setProgress(null);
            mListener.onLoadFinish(mCurrentTableName);
        }
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onUpdateProgress(int progress) {
        publishProgress(progress);
    }

    private void
    createIndexes(String tableName, Database db) {
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

    private int getRequiredSpaceSize(RPCResult rpcResult, int totalSize, int size) {
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
