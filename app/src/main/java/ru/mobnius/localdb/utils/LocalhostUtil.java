package ru.mobnius.localdb.utils;
import android.content.Context;
import android.content.Intent;

import ru.mobnius.localdb.App;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.request.SyncRequestListener;

public class LocalhostUtil {

    public static void sync(Context context, String tableName) {


        //new SyncRequestListener(app).getResponse(new UrlReader("GET /sync?table=" + tableName + " HTTP/1.1"));
    }
}
