
package ru.mobnius.localdb.request;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;
import ru.mobnius.localdb.utils.VersionUtil;

public class InfoRequestListener extends AuthFilterRequestListener implements OnRequestListener {
    private Context mContext;

    public InfoRequestListener(Context context) {
        mContext = context;
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
            for (AbstractDao dao:HttpService.getDaoSession().getAllDaos()) {
                long count = dao.count();
                data.put(dao.getTablename(), dao.count());
            }
            array.put(data);
            JSONObject result = new JSONObject();
            result.put("records", array);
            object.put("result", result);
            response = Response.getInstance(urlReader, object.toString());
        } catch (Exception e) {
            response = Response.getErrorInstance(urlReader, e.getMessage(), Response.RESULT_FAIL);
        }
        return response;
    }
}