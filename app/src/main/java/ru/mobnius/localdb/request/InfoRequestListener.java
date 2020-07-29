
package ru.mobnius.localdb.request;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.greendao.AbstractDao;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;
import ru.mobnius.localdb.utils.VersionUtil;


public class InfoRequestListener extends AuthFilterRequestListener implements OnRequestListener {
    private Context mContext;
    private HttpService service;

    public InfoRequestListener(Context context) {
        mContext = context;
        if (context instanceof HttpService) {
            service = (HttpService) context;
        }
    }

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/info", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = super.getResponse(urlReader);
        if (response != null) {
            return response;
        }
        try {
            JSONObject meta = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject object = new JSONObject();

            meta.put("success", true);

            object.put("meta", meta);
            String version = VersionUtil.getVersionName(mContext);
            File f = mContext.getDatabasePath("local-db.db");
            long dbSize = f.length();
            String dbSizeInMB = dbSize / 1048576 + " MB";
            JSONArray array = new JSONArray();
            data.put("db_size", dbSizeInMB);
            data.put("version", version);
            array.put(data);
            new CountQueryAsyncTask().execute();
            JSONObject result = new JSONObject();
            result.put("records", array);
            object.put("result", result);
            response = Response.getInstance(urlReader, object.toString());
        } catch (Exception e) {
            response = Response.getErrorInstance(urlReader, e.getMessage(), Response.RESULT_FAIL);
        }
        return response;
    }


    public class CountQueryAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... avoids) {
            JSONObject data;
            try {
                data = new JSONObject();
                for (AbstractDao dao : HttpService.getDaoSession().getAllDaos()) {
                    data.put(dao.getTablename(), dao.count());
                }
            } catch (SQLException | JSONException e) {
                return null;
            }
            return data;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            super.onPostExecute(data);
            Intent intent = new Intent(Tags.COUNT_TAG);
            intent.putExtra(Tags.COUNT_TEXT, data.toString());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
}