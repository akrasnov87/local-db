package ru.mobnius.localdb.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.utils.NetworkUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * Проверка выполнения синхронизации
 */
public class SyncStatusRequestListener extends AuthFilterRequestListener
        implements OnRequestListener {

    private final App mApp;

    public SyncStatusRequestListener(App app) {
        mApp = app;
    }

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/sync/status", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = super.getResponse(urlReader);
        if(response != null) {
            return response;
        }

        if(PreferencesManager.getInstance().getProgress() != null) {
            if(NetworkUtil.isNetworkAvailable(mApp)) {
                ProgressResult progressResult = ProgressResult.getInstance(PreferencesManager.getInstance().getProgress());
                response = Response.getInstance(urlReader, progressResult.toJsonString());
            } else {
                response = Response.getErrorInstance(urlReader, "Не подключения к сети интернет", Response.RESULT_FAIL);
            }
        } else {
            response = Response.getErrorInstance(urlReader, "Информация о выполнении загрузки не найдена. Возможно она была завершена ранее.", Response.RESULT_FAIL);
        }
        return response;
    }
}
