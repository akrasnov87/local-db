package ru.mobnius.localdb.request;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * запуск синхронизации
 */
public class SyncRequestListener extends AuthFilterRequestListener
        implements LoadAsyncTask.OnLoadListener {

    private final App mApp;
    private UrlReader mUrlReader;
    private OnSpaceOverListener mSpaceOverListener;
    public SyncRequestListener(App app) {
        mApp = app;
    }

    public void addOnSpaceOverListener(OnSpaceOverListener spaceOverListener) {
        mSpaceOverListener = spaceOverListener;
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
                    new LoadAsyncTask(tableName, this, mSpaceOverListener).execute(PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());

                response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
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

    public interface OnSpaceOverListener {
        void onSpaceFinished(String message);

    }
}
