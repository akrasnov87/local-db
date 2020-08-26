package ru.mobnius.localdb.request;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.greendao.database.Database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.data.ConnectionChecker;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * запуск синхронизации
 */
public class SyncRequestListener extends AuthFilterRequestListener
        implements LoadAsyncTask.OnLoadListener, ConnectionChecker.CheckConnection {

    private final App mApp;
    private UrlReader mUrlReader;
    private String mTableName = "";
    private boolean isCanceled = false;

    public SyncRequestListener(App app) {
        mApp = app;
        mApp.getConnectionReceiver().setListener(this);
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
        String tableName = urlReader.getParam("table");
        mTableName = tableName;
        if (tableName != null) {
            if (NetworkUtil.isNetworkAvailable(mApp)) {
                if (urlReader.getParam("restore") == null) {
                    PreferencesManager.getInstance().setProgress(null);
                }
                Intent cancelPreviousTask = new Intent(Tags.CANCEL_TASK_TAG);
                LocalBroadcastManager.getInstance(mApp).sendBroadcast(cancelPreviousTask);
                new LoadAsyncTask(tableName, this, mApp).execute(PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
                isCanceled = false;
            } else {
                response = Response.getErrorInstance(urlReader, "Не подключения к сети интернет", Response.RESULT_FAIL);
            }
        } else {
            response = Response.getErrorInstance(urlReader, "Не все параметры запроса указаны", Response.RESULT_FAIL);
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
        mTableName = "";
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        if (!isConnected) {
            Intent intent1 = new Intent(Tags.CANCEL_TASK_TAG);
            LocalBroadcastManager.getInstance(mApp).sendBroadcast(intent1);
            isCanceled = true;
        } else {
            if (!mTableName.isEmpty() && isCanceled) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new LoadAsyncTask(mTableName, SyncRequestListener.this, mApp).
                                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                        isCanceled = false;
                    }
                }, 7000);
            }
        }
    }

    private double getPercent(int progress, int total) {
        double result = (double) (progress * 100) / total;
        if(result > 100) {
            result = 100;
        }
        return result;
    }
}
