package ru.mobnius.localdb.data;

import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public interface OnHttpListener {
    void onHttpRequest(UrlReader reader);
    void onHttpResponse(Response response);
    void onDownLoadProgress(UrlReader reader, int progress, int total);
    void onDownLoadFinish(String tableName, UrlReader reader);
}
