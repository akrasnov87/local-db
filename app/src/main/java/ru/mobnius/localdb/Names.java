package ru.mobnius.localdb;

import ru.mobnius.localdb.data.PreferencesManager;

public interface Names {
    String TAG = "LOCAL_DB";
    String UPDATE_URL = PreferencesManager.getInstance().getNodeUrl() + "/localdb.apk";
}
