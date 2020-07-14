package ru.mobnius.localdb.model.rpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.mobnius.localdb.Logger;

/**
 * Результат RPC вызова
 */
public class RPCResult {
    /**
     * метаописание результат запроса
     */
    public RpcMeta meta;
    public RPCRecords result;

    /**
     * обработка JSONObject
     *
     * @param obj объект для обработки данных
     * @return Возвращается объект
     */
    private static RPCResult processingJSONObject(JSONObject obj) {
        RPCResult result;
        try {
            result = new RPCResult();

            RpcMeta meta = new RpcMeta();
            JSONObject metaJSONObject = obj.getJSONObject("meta");
            meta.success = metaJSONObject.getBoolean("success");
            try {
                meta.msg = metaJSONObject.getString("msg");
            } catch (Exception ignored) {

            }
            result.meta = meta;

            RPCRecords records = new RPCRecords();
            JSONObject resultJSONObject = obj.getJSONObject("result");
            try {
                records.total = resultJSONObject.getInt("total");
            } catch (Exception e) {
                records.total = 0;
            }

            try {
                JSONArray array = resultJSONObject.getJSONArray("records");
                if (array.length() > 0) {
                    records.records = new JSONObject[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            records.records[i] = array.getJSONObject(i);
                        } catch (JSONException e) {
                            try {
                                JSONObject innerObj = new JSONObject();
                                innerObj.put("value", array.get(i));
                                records.records[i] = innerObj;
                            } catch (Exception inner) {
                                Logger.error(inner);
                            }
                        }
                    }
                } else {
                    records.records = new JSONObject[0];
                }
            } catch (Exception e) {
                records.records = new JSONObject[0];
                result.meta = new RpcMeta();
                result.meta.success = false;
                result.meta.msg = resultJSONObject.getString("records");
            }
            result.result = records;

            return result;
        } catch (Exception e) {
            result = new RPCResult();
            result.meta = new RpcMeta();
            result.meta.success = false;
            result.meta.msg = e.toString();
            return result;
        }
    }

    /**
     * создание экземпляра
     *
     * @param requestResult результат запроса
     * @return обработанный объект
     */
    public static RPCResult[] createInstance(String requestResult) {
        try {
            RPCResult[] result;
            if (requestResult.indexOf("[") == 0) {
                JSONArray array;
                try {
                    array = new JSONArray(requestResult);
                } catch (JSONException e) {
                    Logger.error(e);
                    return null;
                }
                result = new RPCResult[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    result[i] = processingJSONObject(array.getJSONObject(i));
                }
            } else {
                result = new RPCResult[1];
                result[0] = processingJSONObject(new JSONObject(requestResult));
            }
            return result;
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }
}
