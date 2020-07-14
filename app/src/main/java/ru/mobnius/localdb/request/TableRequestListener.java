package ru.mobnius.localdb.request;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.model.Response;
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

                response = Response.getInstance(urlReader, object.toString());
            } catch (Exception e) {
                response = Response.getErrorInstance(urlReader, e.getMessage(), Response.RESULT_FAIL);
            }
        } else {
            response = Response.getErrorInstance(urlReader, "Информация о выполнении загрузки не найдена. Возможно она была завершена ранее.",Response.RESULT_FAIL);
        }

        return response;
    }
}
