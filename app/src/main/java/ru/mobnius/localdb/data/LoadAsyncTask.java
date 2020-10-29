package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteFullException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.data.dowloadInfo.TableInfo;
import ru.mobnius.localdb.data.tablePack.CsvUtil;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.FileUtil;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.VersionUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Integer, ArrayList<String>> implements EventListener {


    @SuppressLint("StaticFieldLeak")
    private final App mApp;

    private final String[] mTableName;
    private final OnLoadListener mListener;
    private InsertHandler.ZipDownloadListener mZipDownloadListener;

    public LoadAsyncTask(String[] tableName, OnLoadListener listener, App app) {
        mListener = listener;
        mTableName = tableName;
        mApp = app;
        if (listener instanceof InsertHandler.ZipDownloadListener) {
            mZipDownloadListener = (InsertHandler.ZipDownloadListener) listener;
        }
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        ArrayList<String> message = new ArrayList<>();
        if (PreferencesManager.getInstance().getRepoUrl() == null) {
            message.add(Tags.AUTH_ERROR);
            message.add("Ошибка аторизации, не известно значение url для скачивания");
            return message;
        }

        if (!isCancelled() && mTableName != null) {
            for (String tableName : mTableName) {
                Progress startProgress = new Progress(0, 0, tableName);
                PreferencesManager.getInstance().setProgress(startProgress);
                JSONObject versions = CsvUtil.newGetInfo(PreferencesManager.getInstance().getRpcUrl() + "/localdb/versions/1.139.0.513", tableName); //+ VersionUtil.getVersionName(mApp));
                JSONObject infoTables = CsvUtil.getInfo(PreferencesManager.getInstance().getRepoUrl());
                if (infoTables == null || versions == null) {
                    message.add(Tags.RPC_ERROR);
                    message.add("Не удалось получить необходимую информацию о загрузке. Попробуйте повторить загрузку снова");
                    return message;
                }
                //количество записей с сервера за 1 rpc запрос
                int size;
                int fileCount;
                int hddSize;
                String mVersion;
                JSONObject resource = null;
                int mTotal;
                try {
                    if (infoTables.getJSONArray(tableName).length() > 0) {
                        TableInfo tableInfo;
                        try {
                            tableInfo = new TableInfo(infoTables, versions, tableName);
                        } catch (JSONException | NumberFormatException e) {
                            message.add(Tags.RPC_ERROR);
                            message.add("Не удалось получить необходимую для синхронизации информацию. Нужен стабильный источник интернет сигнала. Попробуйте повторить загрузку позднее");
                            return message;
                        }
                        if (tableInfo != null && tableInfo.isError) {
                            message.add(Tags.RPC_ERROR);
                            message.add("Не удалось прочитать информацию о синхронизации. Попробуйте повторить загрузку позднее");
                            return message;
                        }

                        String actualVersion = versions.getString("version_Pack_Max");
                        if (actualVersion.equals(JUST_LAST_VERSION)) {
                            resource = infoTables.getJSONArray(tableName).getJSONObject(0);
                        } else {
                            JSONArray array = infoTables.getJSONArray(tableName);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject temp = array.getJSONObject(i);
                                if (temp.getString("VERSION").equals(actualVersion)) {
                                    resource = array.getJSONObject(i);
                                    break;
                                }
                            }
                        }
                        if (resource != null) {
                            mVersion = resource.getString("VERSION");
                            size = Integer.parseInt(resource.getString("PART"));
                            fileCount = Integer.parseInt(resource.getString("FILE_COUNT"));
                            mTotal = Integer.parseInt(resource.getString("TOTAL_COUNT"));
                            hddSize = Integer.parseInt(resource.getString("SIZE")) / 1024 / 1024;
                        } else {
                            message.add(Tags.RPC_ERROR);
                            message.add("Отсутствует подходящая версия таблицы, попробуйте повторить загрузку позднее");
                            return message;
                        }
                    } else {
                        message.add(Tags.RPC_ERROR);
                        message.add("Ошибка загрузки данных о таблице");
                        return message;
                    }
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
                Progress downloadProgress;
                if (PreferencesManager.getInstance().getProgress() == null) {
                    //удаляем прежде чем заливать
                    removeBeforeInsert = true;
                    Progress progress = new Progress(0, mTotal, tableName);
                    PreferencesManager.getInstance().setProgress(progress);
                    downloadProgress = new Progress(0, mTotal, tableName);
                } else {
                    //восстанавливаем загрузку
                    if (!PreferencesManager.getInstance().getProgress().tableName.toLowerCase().equals(tableName.toLowerCase())) {
                        //так как многопоточная загрузка при загрузке всех таблиц
                        // Progress может не быть null, так как вставляется другая таблица
                        removeBeforeInsert = true;
                    }
                    downloadProgress = PreferencesManager.getInstance().getDownloadProgress();
                }

                PreferencesManager.getInstance().setDownloadProgress(downloadProgress);

                DaoSession daoSession = HttpService.getDaoSession();

                File cacheDir = mApp.getCacheDir();
                if (cacheDir.getFreeSpace() / 1000000 < hddSize + 100) {
                    message.add(Tags.STORAGE_ERROR);
                    message.add("Ошибка: в хранилище телефона недостаточно свободного места для загрузки таблицы(" +
                            tableName + ", необходимо около " + hddSize + 100 + " МБ). Очистите место и повторите загрузку");
                    return message;
                }
                PreferencesManager.getInstance().setRemoteRowCount(String.valueOf(mTotal), tableName);

                try {
                    Database db = daoSession.getDatabase();
                    if (removeBeforeInsert) {
                        db.execSQL("delete from " + tableName);
                        PreferencesManager.getInstance().setLocalRowCount("0", tableName);
                    }
                } catch (SQLiteFullException | SQLiteConstraintException e) {
                    message.add(Tags.SQL_ERROR);
                    message.add(e.getMessage());
                    e.printStackTrace();
                    return message;
                }
                int currentRowsCount = 0;
                int currentFile = 0;
                if (PreferencesManager.getInstance().getDownloadProgress() != null) {
                    currentFile = PreferencesManager.getInstance().getDownloadProgress().getFilesCount();
                    currentRowsCount = PreferencesManager.getInstance().getDownloadProgress().getDownloadRowsCount();
                }
                for (int i = currentFile; i < fileCount; i++) {
                    if (!isCancelled()) {
                        File file = getZipFile(mApp, tableName, mVersion, currentRowsCount, size);
                        if (file == null && !isCancelled()) {
                            file = tryToGetProblemFile(mApp, tableName, mVersion, currentRowsCount, size);
                            if (file == null) {
                                message.add(Tags.RPC_ERROR);
                                message.add("Не удалось получить файл с сервера");
                                return message;
                            }
                        }
                        currentRowsCount += size;
                        if (file != null) {
                            mZipDownloadListener.onZipDownloaded(tableName, file.getAbsolutePath());
                        }
                        Progress currentDownloadProgress = new Progress(currentRowsCount, mTotal, tableName);
                        currentDownloadProgress.setFilesCount(i + 1);
                        currentDownloadProgress.setDownloadRowsCount(currentRowsCount);
                        PreferencesManager.getInstance().setDownloadProgress(currentDownloadProgress);
                        Log.d(Names.TAG, tableName + ": " + getPercent(currentRowsCount, mTotal));
                    }
                }
                PreferencesManager.getInstance().setDownloadProgress(null);
                createIndexes(tableName, HttpService.getDaoSession().getDatabase());
                ArrayList<String> list = new ArrayList<>(Arrays.asList(mTableName));
                list.remove(tableName);
                String[] allTablesArray = list.toArray(new String[0]);
                PreferencesManager.getInstance().setAllTablesArray(allTablesArray);
            }
        }
        return message;
    }

    @Override
    protected void onPostExecute(ArrayList<String> message) {
        super.onPostExecute(message);
        if (message != null && !message.isEmpty()) {
            String[] errorData = {message.get(0), message.get(1)};
            mListener.onLoadError(errorData);
        }
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.STOP_ASYNC_TASK)) {
            this.cancel(false);
            mApp.getObserver().unsubscribe(Observer.STOP_ASYNC_TASK, this);
        }
    }

    private double getPercent(int progress, int total) {
        double result = (double) (progress * 100) / total;
        if (result > 100) {
            result = 100;
        }
        return result;
    }

    private void
    createIndexes(String tableName, Database db) {
        if ("ui_sv_fias".equals(tableName.toLowerCase())) {
            db.execSQL("CREATE INDEX IF NOT EXISTS " + "IDX_UI_SV_FIAS_C_Full_Address ON \"UI_SV_FIAS\"" +
                    " (\"C_Full_Address\" ASC);");
        }
    }

    private File getZipFile(Context context, String tableName, String version, int start, int limit) {
        File file = null;
        HttpURLConnection conn;
        try {
            int nextStep = start + limit;
            String repoURL = PreferencesManager.getInstance().getRepoUrl();
            if (repoURL == null) {
                return null;
            }
            URL url = new URL(repoURL + "/csv-zip/" + tableName + "/" + version + "/" + start + "-" + nextStep + ".zip");


            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            int contentLength = conn.getContentLength();
            if (contentLength <= 0) {
                return null;
            }
            if (!isCancelled()) {
                DataInputStream stream = new DataInputStream(url.openStream());
                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();
                file = new File(FileUtil.getRoot(context, Environment.DIRECTORY_DOCUMENTS), tableName + ":" + start + "-" + nextStep);
                DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                fos.write(buffer);
                fos.flush();
                fos.close();
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        return file;
    }

    private File tryToGetProblemFile(Context context, String tableName, String version, int start, int limit) {
        File file = null;
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(3000);
                if (!isCancelled()) {
                    file = getZipFile(context, tableName, version, start, limit);
                }
                if (file != null) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public interface OnLoadListener {

        /**
         * Результат загрузки данных
         *
         * @param tableName имя таблицы
         */
        void onInsertFinish(String tableName);

        void onInsertProgress(int progress, int total);

        void onLoadError(String[] message);
    }
}
