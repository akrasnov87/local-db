package ru.mobnius.localdb.request;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.data.OnResponseListener;
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
        implements LoadAsyncTask.OnLoadListener {

    private final App mApp;
    private UrlReader mUrlReader;
    private LoadAsyncTask mLoadAsyncTask;

    public SyncRequestListener(App app) {
        mApp = app;
        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Names.CANCEL_TASK_TAG) && mLoadAsyncTask != null) {
                    mLoadAsyncTask.cancel(false);
                }
            }
        };
        LocalBroadcastManager.getInstance(mApp).registerReceiver(
                mMessageReceiver, new IntentFilter(Names.CANCEL_TASK_TAG));
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
        if (tableName != null) {
            if (NetworkUtil.isNetworkAvailable(mApp)) {
                if (urlReader.getParam("restore") == null) {
                    PreferencesManager.getInstance().setProgress(null);
                }
                if (mLoadAsyncTask == null) {
                    mLoadAsyncTask = new LoadAsyncTask(tableName, this, mApp);
                    mLoadAsyncTask.execute(PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                    response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
                } else {
                    if (mLoadAsyncTask.isCancelled()) {
                        mLoadAsyncTask = new LoadAsyncTask(tableName, this, mApp);
                        mLoadAsyncTask.execute(PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
                        response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
                    } else {
                        String attention = "Подождите, завершается предыдущая синхронизация";
                        mLoadAsyncTask.cancel(false);
                        Intent intent = new Intent(Names.ASYNC_NOT_CANCELLED_TAG);
                        intent.putExtra(Names.ASYNC_NOT_CANCELLED_TEXT, attention);
                        LocalBroadcastManager.getInstance(mApp).sendBroadcast(intent);
                        response = Response.getErrorInstance(urlReader, attention, Response.RESULT_FAIL);
                    }
                }
            } else {
                response = Response.getErrorInstance(urlReader, "Не подключения к сети интернет", Response.RESULT_FAIL);
            }
        } else {
            response = Response.getErrorInstance(urlReader, "Не все параметры запроса указаны", Response.RESULT_FAIL);
        }

        return response;
    }

    @Override
    public void onLoadProgress(String tableName, Progress progress) {
        mApp.onDownLoadProgress(mUrlReader, progress);
        Log.d(Names.TAG, tableName + ": " + progress.getPercent());
    }

    @Override
    public void onLoadFinish(String tableName) {
        mApp.onDownLoadFinish(tableName, mUrlReader);
    }
}
