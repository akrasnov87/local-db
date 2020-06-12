package ru.mobnius.localdb.data;

import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public interface OnResponseListener {
    Response onResponse(UrlReader urlReader);
}
