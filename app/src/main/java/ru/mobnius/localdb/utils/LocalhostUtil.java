package ru.mobnius.localdb.utils;
import ru.mobnius.localdb.App;

import ru.mobnius.localdb.request.SyncRequestListener;

public class LocalhostUtil {

    public static void sync(App app, String tableName) {
        new SyncRequestListener(app).getResponse(new UrlReader("GET /sync?table=" + tableName + " HTTP/1.1"));
    }
}
