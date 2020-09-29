package ru.mobnius.localdb.request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * Проверка выполнения синхронизации
 */
public class SyncStatusRequestListener extends AuthFilterRequestListener
        implements OnRequestListener, EventListener {

    private final App mApp;
    private String errorMessage = "";
    private String errorType = "";

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
        if (response != null) {
            return response;
        }
        if (!errorMessage.isEmpty()) {

            try {
                JSONObject object = new JSONObject();
                JSONObject meta = new JSONObject();
                JSONObject error = new JSONObject();

                meta.put("success", false);
                error.put("ldberror", errorMessage);
                error.put("ldbstatus", errorType);
                object.put("meta", meta);
                object.put("result", error);
                response = Response.getInstance(urlReader, object.toString());
                errorMessage = "";
                return response;
            } catch (JSONException e) {
                Logger.error(e);
            }
        }
        if (PreferencesManager.getInstance().getProgress() != null) {
            ProgressResult progressResult = ProgressResult.getInstance(PreferencesManager.getInstance().getProgress());
            response = Response.getInstance(urlReader, progressResult.toJsonString());

        } else {
            response = Response.getErrorInstance(urlReader, "Информация о выполнении загрузки не найдена. Возможно она была завершена ранее.", Response.RESULT_FAIL);
        }
        return response;
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.ERROR)) {
            errorType = args[0];
            errorMessage = args[1];
            if (errorMessage != null && errorMessage.length() > 600) {
                errorMessage = errorMessage.substring(0, 600);
            }
            errorMessage = "В LocalDB произошла ошибка: " + errorMessage;
        }
    }
}
