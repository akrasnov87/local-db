package ru.mobnius.localdb.request;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.data.ConnectionChecker;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.RowCountAsyncTask;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * запуск синхронизации
 */
public class SyncRequestListener extends AuthFilterRequestListener
        implements LoadAsyncTask.OnLoadListener, ConnectionChecker.CheckConnection {

    private final App mApp;
    private UrlReader mUrlReader;
    private boolean isCanceled = false;

    public SyncRequestListener(App app, SyncStatusRequestListener statusRequestListener) {
        mApp = app;
        mApp.getConnectionReceiver().setListener(this);
        mApp.getObserver().subscribe(Observer.ERROR, statusRequestListener);
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
        if (NetworkUtil.isNetworkAvailable(mApp)) {
            if (urlReader.getParam("restore") != null) {
                RowCountAsyncTask task = new RowCountAsyncTask(mApp, SyncRequestListener.this);
               mApp.getObserver().subscribe(Observer.STOP, task);
                task.execute(PreferencesManager.getInstance().getProgress().tableName);
            } else {
                PreferencesManager.getInstance().setProgress(null);
                Intent cancelPreviousTask = new Intent(Tags.CANCEL_TASK_TAG);
                LocalBroadcastManager.getInstance(mApp).sendBroadcast(cancelPreviousTask);
                LoadAsyncTask task = new LoadAsyncTask(tableName.toArray(new String[0]), this, mApp);
                mApp.getObserver().subscribe(Observer.STOP, task);
                task.execute(PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
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
        mApp.onDownLoadProgress(mUrlReader, progress, total);
        Log.d(Names.TAG, tableName + ": " + getPercent(progress, total));
    }

    @Override
    public void onLoadFinish(String tableName) {
        mApp.onDownLoadFinish(tableName, mUrlReader);
        PreferencesManager.getInstance().setProgress(null);
    }

    @Override
    public void onLoadError(String [] message) {
        mApp.getObserver().notify(Observer.ERROR, message);
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        if (!isConnected) {
            Intent intent = new Intent(Tags.CANCEL_TASK_TAG);
            LocalBroadcastManager.getInstance(mApp).sendBroadcast(intent);
            mApp.getObserver().notify(Observer.STOP, "stopping async task");
            isCanceled = true;
        } else {
            if (PreferencesManager.getInstance().getProgress() != null && isCanceled) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RowCountAsyncTask task = new RowCountAsyncTask(mApp, SyncRequestListener.this);
                        mApp.getObserver().subscribe(Observer.STOP, task);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PreferencesManager.getInstance().getProgress().tableName);
                        isCanceled = false;
                    }
                }, 5000);
            }
        }
    }

    private double getPercent(int progress, int total) {
        double result = (double) (progress * 100) / total;
        if (result > 100) {
            result = 100;
        }
        return result;
    }

    public void stopDownload() {
    }
}