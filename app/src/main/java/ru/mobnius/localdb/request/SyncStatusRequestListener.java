package ru.mobnius.localdb.request;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.RpcMeta;
import ru.mobnius.localdb.model.progress.ProgressRecords;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * Проверка выполнения синхронизации
 */
public class SyncStatusRequestListener
        implements OnRequestListener {

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/sync/status", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response;
        if(SyncRequestListener.sDownloadProgress != null) {
            response = new Response(Response.RESULT_OK, urlReader.getParts()[2]);
            response.setContentType(Response.APPLICATION_JSON);
            ProgressResult progressResult = new ProgressResult();
            progressResult.result = new ProgressRecords();
            progressResult.result.records = new Progress[1];
            progressResult.result.records[0] = SyncRequestListener.sDownloadProgress;
            progressResult.result.total = 1;
            progressResult.meta = new RpcMeta();
            progressResult.meta.success = true;
            progressResult.meta.msg = SyncRequestListener.sTableName;

            response.setContent(new Gson().toJson(progressResult));
        } else {
            response = new Response(Response.RESULT_FAIL, urlReader.getParts()[2]);
            response.setContentType(Response.APPLICATION_JSON);
            ProgressResult progressResult = new ProgressResult();
            progressResult.meta = new RpcMeta();
            progressResult.meta.success = false;
            progressResult.meta.msg = "Информация о выполнении загрузки не найдена. Возможно она была завершена ранее.";

            response.setContent(new Gson().toJson(progressResult));
        }
        return response;
    }
}
