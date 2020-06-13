package ru.mobnius.localdb.model.fias;

import ru.mobnius.localdb.model.RpcMeta;

public class FiasResult {
    public String action;
    public String method;
    public int tid;
    public FiasRecords result;
    public String type;
    public RpcMeta meta;
}
