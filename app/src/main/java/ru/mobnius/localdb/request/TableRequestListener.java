package ru.mobnius.localdb.request;

import android.net.Uri;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.AuthResult;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.RpcMeta;
import ru.mobnius.localdb.model.progress.ProgressRecords;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.utils.StorageUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * Проверка выполнения синхронизации
 */
public class TableRequestListener extends AuthFilterRequestListener
        implements OnRequestListener {

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/table", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = super.getResponse(urlReader);
        if(response != null) {
            return response;
        }

        String tableName = urlReader.getParam("name");
        String query = urlReader.getParam("query");

        if(tableName != null && query != null) {
            try {
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                JSONObject meta = new JSONObject();
                meta.put("success", true);
                meta.put("message", tableName);
                object.put("meta", meta);

                JSONObject result = new JSONObject();
                JSONArray jsonArray = StorageUtil.getResults(HttpService.getDaoSession().getDatabase(), Uri.decode(query));
                result.put("records", jsonArray);
                result.put("total", jsonArray.length());

                object.put("result", result);
                array.put(object);

                response = new Response(Response.RESULT_OK, urlReader.getParts()[2]);
                response.setContentType(Response.APPLICATION_JSON);
                response.setContent(array.toString(4));

            }catch (Exception e) {
                response = new Response(Response.RESULT_FAIL, urlReader.getParts()[2]);
                response.setContentType(Response.APPLICATION_JSON);
                ProgressResult progressResult = new ProgressResult();
                progressResult.meta = new RpcMeta();
                progressResult.meta.success = false;
                progressResult.meta.msg = e.getMessage();

                response.setContent(new Gson().toJson(progressResult));
            }
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
