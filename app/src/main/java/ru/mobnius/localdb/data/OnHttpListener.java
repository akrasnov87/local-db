package ru.mobnius.localdb.data;

import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public interface OnHttpListener {
    void onHttpRequest(UrlReader reader);
    void onHttpResponse(Response response);
    void onDownLoadProgress(UrlReader reader, Progress progress);
    void onDownLoadFinish(String tableName, UrlReader reader);
}
