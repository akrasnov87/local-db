package ru.mobnius.localdb.data;

import ru.mobnius.localdb.model.LogItem;

public interface OnLogListener {
    void onAddLog(LogItem item);
}
