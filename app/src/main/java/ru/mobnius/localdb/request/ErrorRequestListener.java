package ru.mobnius.localdb.request;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.storage.ClientErrorsDao;
import ru.mobnius.localdb.utils.StorageUtil;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * Проверка выполнения синхронизации
 */
public class ErrorRequestListener extends AuthFilterRequestListener
        implements OnRequestListener {

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/error", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = super.getResponse(urlReader);
        if(response != null) {
            return response;
        }

        String top = urlReader.getParam("top");
        if(top != null) {
            try {
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                JSONObject meta = new JSONObject();
                meta.put("success", true);
                meta.put("message", ClientErrorsDao.TABLENAME);
                object.put("meta", meta);

                JSONObject result = new JSONObject();
                JSONArray jsonArray = StorageUtil.getResults(HttpService.getDaoSession().getDatabase(), Uri.decode("select * from " + ClientErrorsDao.TABLENAME + " ORDER BY " + ClientErrorsDao.Properties.Date.columnName + " DESC LIMIT " + top));
                result.put("records", jsonArray);
                result.put("total", jsonArray.length());

                object.put("result", result);
                array.put(object);

                response = Response.getInstance(urlReader, array.toString(4));
            } catch (Exception e) {
                response = Response.getErrorInstance(urlReader, e.getMessage(), Response.RESULT_FAIL);
            }
        } else {
            response = Response.getErrorInstance(urlReader, "количество не указано", Response.RESULT_FAIL);
        }

        return response;
    }
}
