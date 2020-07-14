package ru.mobnius.localdb;

import ru.mobnius.localdb.data.PreferencesManager;

public interface Names {
    String TAG = "LOCAL_DB";
    String INT_FORMAT = "###,###,###";
    String UPDATE_URL = PreferencesManager.getInstance().getNodeUrl() + "/localdb.apk";
    String ERROR_TAG = "Error_tag";
    String ASYNC_NOT_CANCELLED_TAG = "Async_not_cancelled_tag";
    String ASYNC_CANCELLED_TAG = "Async_cancelled_tag";
    String CANCEL_TASK_TAG = "Cancel_task_tag";
    String ERROR_TEXT = "Error_text";
    String ASYNC_NOT_CANCELLED_TEXT = "sync_not_cancelled_text";
}
