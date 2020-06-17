package ru.mobnius.localdb.request;

import android.util.Log;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.RpcMeta;
import ru.mobnius.localdb.model.fias.LoadAsyncTask;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.storage.FiasDao;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * запуск синхронизации
 */
public class SyncRequestListener
        implements OnRequestListener,
        LoadAsyncTask.OnLoadListener {

    public static Progress sDownloadProgress;
    public static LoadAsyncTask sLoadAsyncTask;
    public static String sTableName;

    private App mApp;
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
        mUrlReader = urlReader;
        // TODO: 17.06.2020 нужно достать из запроса логин и пароль
        if(sLoadAsyncTask != null) {
            sLoadAsyncTask.cancel(true);
        }
        sTableName = urlReader.getParam("table");
        sLoadAsyncTask = new LoadAsyncTask(sTableName, this);

        Response response;
        if(sTableName != null) {
            sLoadAsyncTask.execute(PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());

            response = new Response(Response.RESULT_OK, urlReader.getParts()[2]);
            response.setContentType(Response.APPLICATION_JSON);
            ProgressResult progressResult = new ProgressResult();
            progressResult.meta = new RpcMeta();
            progressResult.meta.success = true;
            response.setContent(new Gson().toJson(progressResult));
        } else {
            response = new Response(Response.RESULT_FAIL, urlReader.getParts()[2]);
            response.setContentType(Response.APPLICATION_JSON);
            ProgressResult progressResult = new ProgressResult();
            progressResult.meta = new RpcMeta();
            progressResult.meta.success = false;
            progressResult.meta.msg = "Не все параметры запроса указаны";
            response.setContent(new Gson().toJson(progressResult));
        }

        return response;
    }

    @Override
    public void onLoadProgress(String tableName, Progress progress) {
        mApp.onDownLoadProgress(mUrlReader, progress);
        sDownloadProgress = progress;
        Log.d(Names.TAG, String.valueOf(progress.getPercent()));
    }

    @Override
    public void onLoadFinish(String tableName) {
        mApp.onDownLoadFinish(mUrlReader);
    }
}
