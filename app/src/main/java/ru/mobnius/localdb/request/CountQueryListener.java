package ru.mobnius.localdb.request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public class CountQueryListener extends AuthFilterRequestListener implements OnRequestListener  {
    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/count", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        super.getResponse(urlReader);
        String d = urlReader.getParam("tables");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("result", d);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Response response =  Response.getInstance(urlReader, jsonObject.toString());
        response.setContentType(Response.APPLICATION_JSON);
        return response;
    }
}
