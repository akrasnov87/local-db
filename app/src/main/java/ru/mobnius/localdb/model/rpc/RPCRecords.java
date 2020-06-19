package ru.mobnius.localdb.model.rpc;

import org.json.JSONObject;

/**
 * результат с записями
 */
public class RPCRecords {
    /**
     * список записей
     */
    public JSONObject[] records;

    /**
     * количество записей
     */
    public int total;
}
