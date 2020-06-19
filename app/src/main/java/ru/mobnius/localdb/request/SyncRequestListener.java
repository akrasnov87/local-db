package ru.mobnius.localdb.request;

import android.util.Log;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.rpc.RpcMeta;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * запуск синхронизации
 */
public class SyncRequestListener extends AuthFilterRequestListener
        implements LoadAsyncTask.OnLoadListener {

    private final App mApp;
    private UrlReader mUrlReader;

    public SyncRequestListener(App app) {
        mApp = app;
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
       if(response != null) {
           return response;
       }

        mUrlReader = urlReader;
        // TODO: 17.06.2020 нужно достать из запроса логин и пароль
        String tableName = urlReader.getParam("table");
        if(tableName != null) {
            new LoadAsyncTask(tableName, this).execute(PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());

            response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
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
