package ru.mobnius.localdb.model;

import com.google.gson.Gson;

import ru.mobnius.localdb.model.progress.ProgressRecords;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.model.rpc.RpcMeta;

public class DefaultResult {
    public static DefaultResult getSuccessInstance() {
        DefaultResult result = new DefaultResult();
        result.meta = new RpcMeta();
        result.meta.success = true;
        return result;
    }

    public RpcMeta meta;

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
