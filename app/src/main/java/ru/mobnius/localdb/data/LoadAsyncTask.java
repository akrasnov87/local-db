package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteFullException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.greendao.database.Database;
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
        if (isAuthProblems(strings[0], strings[1])) {
            message.add(Tags.AUTH_ERROR);
            message.add("Не удалось получить ответ от сервера, проблема авторизации. Попробуйте авторизоваться повторно.");
            return message;
        }
        if (!isCancelled() && mTableName != null) {
            for (String tableName : mTableName) {

                JSONObject actualVersions = CsvUtil.getActualVersions(PreferencesManager.getInstance().getRpcUrl() + "/localdb/versions/1.140.0.623" , tableName);
                JSONObject completeInfo = CsvUtil.getInfo(PreferencesManager.getInstance().getRepoUrl(), tableName);
                if (completeInfo == null || actualVersions == null) {
                    message.add(Tags.RPC_ERROR);
                    message.add("Не удалось получить необходимую для синхронизации информацию. Нужен стабильный источник интернет сигнала. Попробуйте повторить загрузку позднее.");
                    return message;
                }

                TableInfo tableInfo = CsvUtil.getTableInfo(completeInfo, actualVersions, tableName);
                if (tableInfo == null) {
                    message.add(Tags.RPC_ERROR);
                    message.add("Не удалось получить валидные данные необходимые для начала загрузки. Попробуйте повторить загрузку позднее");
                    return message;
                }
                boolean removeBeforeInsert = false;
                Progress downloadProgress;
                if (PreferencesManager.getInstance().getProgress() == null) {
                    removeBeforeInsert = true;//удаляем прежде чем заливать
                    Progress progress = new Progress(0, tableInfo.totalRows, tableName);
                    PreferencesManager.getInstance().setProgress(progress);
                    downloadProgress = new Progress(0, tableInfo.totalRows, tableName);
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

                File cacheDir = mApp.getCacheDir();
                int wholeTableSize = ((tableInfo.hddSize*10) + 300);
                if (cacheDir.getFreeSpace() / 1000000 < wholeTableSize) {
                    message.add(Tags.STORAGE_ERROR);
                    message.add("Ошибка: в хранилище телефона недостаточно свободного места для загрузки таблицы(" +
                            tableName + ", необходимо около "+ wholeTableSize + "МБ). Очистите место и повторите загрузку");
                    return message;
                }
                PreferencesManager.getInstance().setRemoteRowCount(String.valueOf(tableInfo.totalRows), tableName);

                try {
                    Database db = HttpService.getDaoSession().getDatabase();
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
                for (int i = currentFile; i < tableInfo.fileCount; i++) {
                    if (!isCancelled()) {
                        File file = getZipFile(mApp, tableName, tableInfo.actualVersion, currentRowsCount, tableInfo.size);
                        if (file == null && !isCancelled()) {
                            file = tryToGetProblemFile(mApp, tableName, tableInfo.actualVersion, currentRowsCount, tableInfo.size);
                            if (file == null) {
                                message.add(Tags.RPC_ERROR);
                                message.add("Не удалось получить файл с сервера");
                                return message;
                            }
                        }
                        currentRowsCount += tableInfo.size;
                        if (file != null) {
                            mZipDownloadListener.onZipDownloaded(tableName, file.getAbsolutePath());
                        }
                        Progress currentDownloadProgress = new Progress(currentRowsCount, tableInfo.totalRows, tableName);
                        currentDownloadProgress.setFilesCount(i + 1);
                        currentDownloadProgress.setDownloadRowsCount(currentRowsCount);
                        PreferencesManager.getInstance().setDownloadProgress(currentDownloadProgress);
                        Log.d(Names.TAG, tableName + ": " + getPercent(currentRowsCount, tableInfo.totalRows));
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

    private boolean isAuthProblems(String login, String password) {
        boolean isProblem = false;
        if (PreferencesManager.getInstance().getRepoUrl() == null) {
            isProblem = true;
        } else {
            isProblem = !Loader.getInstance().auth(login, password);
        }
        return isProblem;
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
