package ru.mobnius.localdb.request;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.data.ConnectionChecker;
import ru.mobnius.localdb.data.InsertHandler;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * запуск синхронизации
 */
public class SyncRequestListener extends AuthFilterRequestListener
        implements LoadAsyncTask.OnLoadListener, ConnectionChecker.CheckConnection,
        InsertHandler.ZipDownloadListener, EventListener {

    private final App mApp;
    private UrlReader mUrlReader;
    private boolean isCanceled = false;
    private InsertHandler mInsertHandler;
    private String [] mTableName;

    public SyncRequestListener(App app, SyncStatusRequestListener statusRequestListener) {
        mApp = app;
        mApp.getConnectionReceiver().setListener(this);
        mApp.getObserver().subscribe(Observer.ERROR, statusRequestListener);
        mApp.getObserver().subscribe(Observer.STOP_THREAD, this);

    }

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/sync\\?table=", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = super.getResponse(urlReader);
        if (response != null) {
            return response;
        }

        mUrlReader = urlReader;
        // TODO: 17.06.2020 нужно достать из запроса логин и пароль
        ArrayList<String> tableName = new ArrayList<>();
        String table = urlReader.getParam("table");
        if (table.equals("allTables")) {
            for (int i = 0; i < HttpService.getDaoSession().getAllDaos().size(); i++) {
                AbstractDao dao = (AbstractDao) HttpService.getDaoSession().getAllDaos().toArray()[i];
                if (!dao.getTablename().equals("sd_client_errors")) {
                    tableName.add(dao.getTablename());
                    PreferencesManager.getInstance().setIsAllTables(true);
                    PreferencesManager.getInstance().setAllTablesArray(tableName.toArray(new String[0]));
                }
            }
        } else {
            tableName.add(table);
            PreferencesManager.getInstance().setIsAllTables(false);
            PreferencesManager.getInstance().setAllTablesArray(null);
        }
        mTableName = tableName.toArray(new String[0]);
        if (NetworkUtil.isNetworkAvailable(mApp)) {
            if (urlReader.getParam("restore") != null) {
                LoadAsyncTask task = new LoadAsyncTask(mTableName, SyncRequestListener.this, mApp);
                mApp.getObserver().subscribe(Observer.STOP_ASYNC_TASK, task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
            } else {
                PreferencesManager.getInstance().setProgress(null);
                if (mInsertHandler != null) {
                    mInsertHandler.quit();
                    mInsertHandler = null;
                }
                mInsertHandler = new InsertHandler(mApp, this, new Handler(Looper.getMainLooper()));
                mInsertHandler.start();
                mInsertHandler.getLooper();
                LoadAsyncTask task = new LoadAsyncTask(tableName.toArray(new String[0]), this, mApp);
                mApp.getObserver().subscribe(Observer.STOP_ASYNC_TASK, task);
                mApp.getObserver().subscribe(Observer.STOP_THREAD, mInsertHandler);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
                isCanceled = false;
            }
        } else {
            response = Response.getErrorInstance(urlReader, "Не подключения к сети интернет", Response.RESULT_FAIL);
        }
        return response;
    }

    @Override
    public void onLoadProgress(String tableName, int progress, int total) {

        Log.d(Names.TAG, tableName + ": " + getPercent(progress, total));
    }

    @Override
    public void onInsertProgress(String tableName, int progress, int total) {
        mApp.onDownLoadProgress(mUrlReader, progress, total);
    }

    @Override
    public void onInsertFinish(String tableName) {
        mApp.onDownLoadFinish(tableName, mUrlReader);
    }

    @Override
    public void onDownLoadFinish(String tableName) {
        if (mTableName!=null){
            mTableName = null;
        }
        PreferencesManager.getInstance().setDownloadProgress(null);
    }

    @Override
    public void onLoadError(String[] message) {
        mApp.getObserver().notify(Observer.ERROR, message);
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        if (!isConnected) {
            mApp.getObserver().notify(Observer.STOP_ASYNC_TASK, "stopping async task");
            isCanceled = true;
        } else {
            if (PreferencesManager.getInstance().getDownloadProgress()!= null && isCanceled) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoadAsyncTask task = new LoadAsyncTask(mTableName, SyncRequestListener.this, mApp);
                        mApp.getObserver().subscribe(Observer.STOP_ASYNC_TASK, task);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                        isCanceled = false;
                    }
                }, 5000);
            }
        }
    }

    @Override
    public void onZipDownloaded(String tableName, String zipFilePath) {
        mInsertHandler.insert(tableName, zipFilePath);
    }

    private double getPercent(int progress, int total) {
        double result = (double) (progress * 100) / total;
        if (result > 100) {
            result = 100;
        }
        return result;
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.STOP_THREAD)) {
            if (mInsertHandler != null) {
                mInsertHandler.quit();
                mInsertHandler = null;
            }
            if (PreferencesManager.getInstance().getProgress() != null) {
                PreferencesManager.getInstance().setProgress(null);
            }
        }
    }
}