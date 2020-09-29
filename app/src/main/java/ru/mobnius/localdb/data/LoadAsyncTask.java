package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteFullException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.greenrobot.greendao.database.Database;
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
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.data.tablePack.CsvUtil;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.FileUtil;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Загрузка данных с сервера
 */
public class LoadAsyncTask extends AsyncTask<String, Integer, ArrayList<String>> implements StorageUtil.OnProgressUpdate, EventListener {


    @SuppressLint("StaticFieldLeak")
    private Context mApp;

    private final String[] mTableName;
    private String mCurrentTableName;
    private int mTotal;

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
        JSONObject infoTables = CsvUtil.getInfo(PreferencesManager.getInstance().getRepoUrl());
        ArrayList<String> message = new ArrayList<>();
        if (!isCancelled() && mTableName != null) {
            for (int j = 0; j < mTableName.length; j++) {
                //количество записей с сервера за 1 rpc запрос
                int size;
                int fileCount;
                int hddSize;
                mCurrentTableName = mTableName[j];
                String mVersion;
                JSONObject tableInfo;

                try {
                    tableInfo = infoTables.getJSONArray(mCurrentTableName).getJSONObject(0);
                    mVersion = tableInfo.getString("VERSION");
                    size = Integer.parseInt(tableInfo.getString("PART"));
                    fileCount = Integer.parseInt(tableInfo.getString("FILE_COUNT"));
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
                Progress downloadProgress;
                if (PreferencesManager.getInstance().getProgress() == null) {
                    //удаляем прежде чем заливать
                    removeBeforeInsert = true;
                    Progress progress = new Progress(0, mTotal, mTableName[j], mVersion);
                    PreferencesManager.getInstance().setProgress(progress);
                    downloadProgress =new Progress(0, mTotal, mTableName[j], mVersion);
                } else {
                    //восстанавливаем загрузку
                    downloadProgress = PreferencesManager.getInstance().getDownloadProgress();
                }

                PreferencesManager.getInstance().setDownloadProgress(downloadProgress);

                DaoSession daoSession = HttpService.getDaoSession();

                File cacheDir = mApp.getCacheDir();
                if (cacheDir.getFreeSpace() / 1000000 < hddSize + 100) {
                    message.add(Tags.STORAGE_ERROR);
                    message.add("Ошибка: в хранилище телефона недостаточно свободного места для загрузки таблицы(" +
                            mTableName + ", необходимо около " + hddSize + 100 + " МБ). Очистите место и повторите загрузку");
                    return message;
                }
                PreferencesManager.getInstance().setRemoteRowCount(String.valueOf(mTotal), mTableName[j]);

                try {
                    Database db = daoSession.getDatabase();
                    if (removeBeforeInsert) {
                        db.execSQL("delete from " + mTableName[j]);
                        PreferencesManager.getInstance().setLocalRowCount("0", mTableName[j]);
                    }
                } catch (SQLiteFullException | SQLiteConstraintException e) {
                    message.add(Tags.SQL_ERROR);
                    message.add(e.getMessage());
                    Logger.error(e);
                    return message;
                }
                int currentRowsCount = 0;
                Log.e("hak", "Начато скачивание" + System.currentTimeMillis());
                int x = 0;
                if (PreferencesManager.getInstance().getDownloadProgress() != null) {
                    x = PreferencesManager.getInstance().getDownloadProgress().getFilesCount();
                }
                for (int i = x; i < fileCount; i++) {
                    if (!isCancelled()) {
                        File file = getZipFile(mApp, mTableName[j], mVersion, currentRowsCount, size);
                        currentRowsCount += size;
                        if (file == null) {
                            message.add(Tags.RPC_ERROR);
                            message.add("Не удалось получить файл с сервера");
                            return message;
                        }
                        mZipDownloadListener.onZipDownloaded(mTableName[j], file.getAbsolutePath());
                        Progress currentDownloadProgress = new Progress(i, mTotal, mTableName[j], mVersion);
                        currentDownloadProgress.setFileName(file.getName());
                        currentDownloadProgress.setFilesCount(i);
                        publishProgress(i);
                        PreferencesManager.getInstance().setDownloadProgress(currentDownloadProgress);
                    }
                }
                PreferencesManager.getInstance().setDownloadProgress(null);

/*
                for (int i = progress.current; i < mTotal; i += size) {
                    if (isCancelled()) {
                        return message;
                    }
                    if (PreferencesManager.getInstance().getProgress() == null) {
                        // значит принудительно все было остановлено
                        break;
                    }

                    try {
                        byte[] buffer = CsvUtil.getFile(PreferencesManager.getInstance().getRepoUrl(), mCurrentTableName, mVersion, i, size);
                        byte[] result = ZipManager.decompress(buffer);
                        String txt = new String(result, StandardCharsets.UTF_8);
                        Table table = CsvUtil.convert(txt);
                        table.updateHeaders("F_Registr_Pts___LINK", "F_Registr_Pts");
                        CsvUtil.insertToTable(table, mCurrentTableName, daoSession);

                    } catch (IOException | DataFormatException e) {
                        Logger.error(e);
                        message.add(Tags.RPC_ERROR);
                        message.add(e.getMessage());
                        return message;
                    }
                    StorageUtil.processing(size, mTableName[j], this);
                    PreferencesManager.getInstance().setProgress(new Progress(i, mTotal, mTableName[j], mVersion));
                }*/
                createIndexes(mTableName[j], HttpService.getDaoSession().getDatabase());
                ArrayList<String> list = new ArrayList<>(Arrays.asList(mTableName));
                list.remove(mCurrentTableName);
                String[] allTablesArray = list.toArray(new String[0]);
                PreferencesManager.getInstance().setAllTablesArray(allTablesArray);
            }
        }
        return message;
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
        if (message != null && !message.isEmpty()) {
            String[] errorData = {message.get(0), message.get(1)};
            mListener.onLoadError(errorData);
        } else {
            mListener.onDownLoadFinish(mCurrentTableName);
        }
    }

    @Override
    public void onUpdateProgress(int progress) {
        publishProgress(progress);
    }

    private File getZipFile(Context context, String tableName, String version, int start, int limit) {
        File file = null;
        HttpURLConnection conn;
        try {
            int nextStep = start + limit;
            String repoURL = PreferencesManager.getInstance().getRepoUrl();
            if (repoURL == null){
                return null;
            }
            URL url = new URL(repoURL + "/csv-zip/" + tableName + "/" + version + "/" + start + "-" + nextStep + ".zip");
            conn = (HttpURLConnection) url.openConnection();
            int contentLength = conn.getContentLength();
            if (contentLength <= 0) {
                return null;
            }
            DataInputStream stream = new DataInputStream(url.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();
            file = new File(FileUtil.getRoot(context, Environment.DIRECTORY_DOCUMENTS), tableName + ":" + start + "-" + nextStep);
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch (IOException | RuntimeException e) {
            Logger.error(e);
        }
        return file;
    }

    private void
    createIndexes(String tableName, Database db) {
        if ("ui_sv_fias".equals(tableName.toLowerCase())) {
            db.execSQL("CREATE INDEX IF NOT EXISTS " + "IDX_UI_SV_FIAS_C_Full_Address ON \"UI_SV_FIAS\"" +
                    " (\"C_Full_Address\" ASC);");
        }
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.STOP_ASYNC_TASK)) {
            this.cancel(false);
            if (PreferencesManager.getInstance().getDownloadProgress() != null) {
                PreferencesManager.getInstance().setDownloadProgress(null);
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
        void onInsertFinish(String tableName);

        void onInsertProgress(String tableName, int progress, int total);


        void onDownLoadFinish(String tableName);


        void onLoadError(String[] message);
    }


}
