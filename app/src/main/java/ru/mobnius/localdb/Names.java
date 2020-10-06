package ru.mobnius.localdb;

import ru.mobnius.localdb.data.PreferencesManager;

public interface Names {
    String TAG = "LOCAL_DB";
    String INT_FORMAT = "###,###,###";
    String UPDATE_LOCALDB_URL = PreferencesManager.getInstance().getNodeUrl() + "/files/localdb.apk";
    String UPDATE_MO_URL = PreferencesManager.getInstance().getNodeUrl() + "/files/client.apk";
}
