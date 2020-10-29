package ru.mobnius.localdb.data.dowloadInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("FieldCanBeLocal")
public class TableInfo {
    private final String VERSION_PACK_MAX = "version_Pack_Max";
    private final String GET_JUST_LAST_VERSION = "0.0.0";
    private final String VERSION_KEY = "VERSION";
    private final String PART_KEY = "PART";
    private final String FILE_COUNT_KEY = "FILE_COUNT";
    private final String TOTAL_COUNT_KEY = "TOTAL_COUNT";
    private final String SIZE_KEY = "SIZE";
    public final int size;
    public final int fileCount;
    public final int hddSize;
    public final String mVersion;
    public final int mTotal;
    public final boolean isError;
    private JSONObject tableInfo;

    public TableInfo(JSONObject resource, JSONObject versions, String tableName) throws JSONException, NumberFormatException {
        String actualVersion = versions.getString(VERSION_PACK_MAX);

        if (actualVersion.equals(GET_JUST_LAST_VERSION)) {
            tableInfo = resource.getJSONArray(tableName).getJSONObject(0);
        } else {
            JSONArray array = resource.getJSONArray(tableName);
            for (int i = 0; i < array.length(); i++) {
                JSONObject temp = array.getJSONObject(i);
                if (temp.getString(VERSION_KEY).equals(actualVersion)) {
                    tableInfo = array.getJSONObject(i);
                    break;
                }
                if (i == array.length() - 1) {
                    tableInfo = array.getJSONObject(0);
                }
            }
        }
        if (tableInfo != null) {
            mVersion = tableInfo.getString(VERSION_KEY);
            size = Integer.parseInt(tableInfo.getString(PART_KEY));
            fileCount = Integer.parseInt(tableInfo.getString(FILE_COUNT_KEY));
            mTotal = Integer.parseInt(tableInfo.getString(TOTAL_COUNT_KEY));
            hddSize = Integer.parseInt(tableInfo.getString(SIZE_KEY)) / 1024 / 1024;
            isError = false;
        } else {
            isError = true;
            mTotal = 0;
            size = 0;
            fileCount = 0;
            hddSize = 0;
            mVersion = "";
        }
    }
}
