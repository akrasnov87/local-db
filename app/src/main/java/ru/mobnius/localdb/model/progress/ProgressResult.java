package ru.mobnius.localdb.model.progress;

import com.google.gson.Gson;

import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.rpc.RpcMeta;

public class ProgressResult extends DefaultResult {

    public static ProgressResult getInstance(Progress progress) {
        ProgressResult result = new ProgressResult();
        result.result = new ProgressRecords();
        result.result.records = new Progress[1];
        result.result.records[0] = progress;
        result.result.total = 1;
        result.meta = new RpcMeta();
        result.meta.success = true;
        return result;
    }

    public ProgressRecords result;
}
