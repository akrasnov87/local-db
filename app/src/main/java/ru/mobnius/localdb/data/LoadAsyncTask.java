package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteFullException;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.data.tablePack.CsvUtil;
import ru.mobnius.localdb.data.tablePack.Table;
import ru.mobnius.localdb.model.KeyValue;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.FileUtil;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.StorageUtil;
import ru.mobnius.localdb.utils.UnzipUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Integer, ArrayList<String>> implements StorageUtil.OnProgressUpdate, EventListener {

    private final OnLoadListener mListener;
    @SuppressLint("StaticFieldLeak")
    private Context mApp;
    private final String[] mTableName;
    private String mCurrentTableName;
    private int mTotal;
    private final String INSERT_HANDLER_NAME = "insert_handler_name";

    public LoadAsyncTask(String[] tableName, OnLoadListener listener, App app) {
        mListener = listener;
        mTableName = tableName;
        mApp = app;
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        ArrayList<String> message = new ArrayList<>();
        if (!isCancelled()) {
            for (int j = 0; j < mTableName.length; j++) {
                mCurrentTableName = mTableName[j];
                Loader loader = Loader.getInstance();
                boolean isAuthorized = loader.auth(strings[0], strings[1]);
                if (!isAuthorized) {
                    message.add(Tags.AUTH_ERROR);
                    message.add("Не удалось получить ответ от сервера. Возможно пароль введен неверно, попробуйте авторизоваться повторно");
                    return message;
                }
                String tablesInfo = getTablesInfo();
                String version;
                int totalCount;
                int fileCount;
                int singlePart;
                if (tablesInfo == null) {
                    message.add(Tags.ZIP_ERROR);
                    message.add("Не удалось необходимую для скачивания информацию");
                    return message;
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(tablesInfo);
                        JSONArray array = jsonObject.getJSONArray(mCurrentTableName);
                        JSONObject object = (JSONObject) array.get(0);
                        if (!object.has("VERSION")){
                            object = (JSONObject) array.get(1);
                        }
                        version = object.getString("VERSION");
                        totalCount = Integer.parseInt(object.getString("TOTAL_COUNT"));
                        fileCount = Integer.parseInt(object.getString("FILE_COUNT"));
                        singlePart = Integer.parseInt(object.getString("PART"));
                    } catch (JSONException e) {
                        message.add(Tags.ZIP_ERROR);
                        message.add("Не удалось прочитать необходимую для скачивания информацию");
                        return message;
                    }
                }
                InsertHandler<String> insertHandler = new InsertHandler<>();
                int currentRowsCount = 0;

                for (int i = 0; i < 1; i++) {

                    long x =  System.currentTimeMillis();
                    Log.e("hak", "time start: " + x);
                    UnzipUtil unzipUtil = new UnzipUtil(getFile(mApp, mCurrentTableName, version, currentRowsCount, singlePart).getAbsolutePath(),
                            FileUtil.getRoot(mApp, Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
                    String unzipped = unzipUtil.unzip();
                    StorageUtil.processings(HttpService.getDaoSession(), unzipped, mCurrentTableName, false);
                    long y = (System.currentTimeMillis() - x);
                    Log.e("hak", "time finish: " + y);
                    long z =  System.currentTimeMillis();
                    Table table = CsvUtil.convert(unzipped);
                    long t = (System.currentTimeMillis() - z);
                    Log.e("hak", "time alex: " + t);
                    //insertHandler.insert(unzipped);
                    currentRowsCount += singlePart;

                    return message;

                }
                //UnzipUtil unzipUtil = new UnzipUtil(get.getAbsolutePath(), FileUtil.getRoot(mApp, Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
               // String s = unzipUtil.unzip();
                int last = singlePart;
                //InsertHandler<String> insertHandler = new InsertHandler<>(0 + "-" + last);
                //insertHandler.insert(s);
                boolean removeBeforeInsert = false;
                Progress progress;
                if (PreferencesManager.getInstance().getProgress() == null) {
                    //удаляем прежде чем заливать
                    removeBeforeInsert = true;
                    progress = new Progress(0, 1, mTableName[j]);
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
                mTotal = result.result.total;
                publishProgress(0);
                File cacheDir = mApp.getCacheDir();
                int spaceNeeded = getRequiredSpaceSize(result, mTotal, size);
                if (cacheDir.getFreeSpace() / 1000000 < spaceNeeded) {
                    message.add(Tags.STORAGE_ERROR);
                    message.add("Ошибка: в хранилище телефона недостаточно свободного места для загрузки таблицы(" +
                            mTableName[j] + ", необходимо около " + spaceNeeded + " МБ). Очистите место и повторите загрузку");
                    return message;
                }
                PreferencesManager.getInstance().setRemoteRowCount(String.valueOf(mTotal), mTableName[j]);
                PreferencesManager.getInstance().setProgress(new Progress(progress.current, mTotal, mTableName[j]));

                try {
                    StorageUtil.processing(daoSession, result, mTableName[j], removeBeforeInsert, this);
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
                        results = rpc(mTableName[j], i, size);
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
                        StorageUtil.processing(daoSession, result, mTableName[j], false, this);
                    } catch (SQLiteFullException | SQLiteConstraintException e) {
                        Logger.error(e);
                        message.add(Tags.SQL_ERROR);
                        message.add(e.getMessage());
                        return message;

                    }
                    if (!isCancelled()) {
                        PreferencesManager.getInstance().setProgress(new Progress(i, mTotal, mTableName[j]));
                    }
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
        if (tableName.toLowerCase().equals("ed_device_billing") || tableName.toLowerCase().equals("ed_registr_pts")) {
            bDisabled = " \"filter\":[{\"property\":\"B_Disable\", \"value\":\"0\", \"operator\":\"=\" }]";
        }
        RPCResult[] results = Loader.getInstance().rpc("Domain." + tableName, "Query", "[{ \"forceLimit\": true, \"start\": " +
                start + ", \"limit\": " + limit + ", \"sort\": [{ \"property\": " + sortColumn + ", \"direction\": \"ASC\" }]," + bDisabled + "}]");
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
        PreferencesManager.getInstance().setProgress(null);
        if (message != null && !message.isEmpty()) {
            String[] errorData = {message.get(0), message.get(1)};
            mListener.onLoadError(errorData);
        } else {
            mListener.onLoadFinish(mCurrentTableName);
        }
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

    private File getFile(Context context, String tableName, String version, int start, int limit) {
        File file = null;
        HttpURLConnection conn;
        try {
            int nextStep = start + limit;
            URL url = new URL(PreferencesManager.getInstance().getZipUrl() + "/csv-zip/" + tableName + "/" + version + "/" + start + "-" + nextStep + ".zip");
            conn = (HttpURLConnection) url.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(url.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();
            file = new File(FileUtil.getRoot(context, Environment.DIRECTORY_DOCUMENTS), tableName + ":" + start + "-" + nextStep);
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private String getTablesInfo() {
        String version = null;
        HttpURLConnection conn;
        try {
            URL url = new URL(PreferencesManager.getInstance().getZipUrl() + "/csv-zip/");
            conn = (HttpURLConnection) url.openConnection();
            int contentLength = conn.getContentLength();
            //OutputStream outputStream = conn.getOutputStream();
            // outputStream.write(postData);
            int status = conn.getResponseCode();
            if (status != 200) {
                return null;
            }
            InputStream stream = conn.getInputStream();
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");

            }
            br.close();
            version = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }


    private int getRequiredSpaceSize(RPCResult rpcResult, int totalSize, int size) {
        float oneResultSize = (float) (Arrays.toString(rpcResult.result.records).length() / 1024) / 1024;
        float totalResultsCount = (float) totalSize / size;
        float spaceNeeded = totalResultsCount * oneResultSize;
        return (int) spaceNeeded + 100;
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.STOP)) {
            this.cancel(false);
            if (PreferencesManager.getInstance().getProgress() != null) {
                PreferencesManager.getInstance().setProgress(null);
            }
        }
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

        void onLoadError(String... errorData);
    }


}
