package ru.mobnius.localdb.data.tablePack;

import android.util.Log;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import ru.mobnius.localdb.data.dowloadInfo.TableInfo;
import ru.mobnius.localdb.storage.DaoSession;

public class CsvUtil {

    private static final String VERSION_PACK_MAX = "version_Pack_Max";
    private static final String GET_JUST_LAST_VERSION = "0.0.0";
    private static final String VERSION_KEY = "VERSION";
    private static final String PART_KEY = "PART";
    private static final String FILE_COUNT_KEY = "FILE_COUNT";
    private static final String TOTAL_COUNT_KEY = "TOTAL_COUNT";
    private static final String SIZE_KEY = "SIZE";

    public static JSONObject getInfo(String baseUrl, String tableName) {
        JSONObject result = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(baseUrl + "/csv-zip");
            urlConnection = (HttpURLConnection) url.openConnection();


            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            Scanner s = new Scanner(in).useDelimiter("\\A");
            result = new JSONObject(s.hasNext() ? s.next() : "");
            if (result.getJSONArray(tableName).length() < 0) {
                return null;
            }
        } catch (Exception e) {
            Log.d(PackManager.TAG, Objects.requireNonNull(e.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }

    public static JSONObject getActualVersions(String baseUrl, String tableName) {
        JSONObject result = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(baseUrl);
            urlConnection = (HttpURLConnection) url.openConnection();


            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            Scanner s = new Scanner(in).useDelimiter("\\A");
            JSONArray array = new JSONArray(s.hasNext() ? s.next() : "");
            if (array != null && array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject temp = array.getJSONObject(i);
                    if (temp.getString("table_Name").toLowerCase().equals(tableName.toLowerCase())) {
                        result = array.getJSONObject(i);
                        break;
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.d(PackManager.TAG, Objects.requireNonNull(e.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }

    public static TableInfo getTableInfo(JSONObject resource, JSONObject versions, String tableName) {
        TableInfo t = null;
        JSONObject tableInfo = null;
        String actualVersion;
        try {
            if (resource.getJSONArray(tableName).length() > 0) {
                JSONArray array = resource.getJSONArray(tableName);
                actualVersion = versions.getString(VERSION_PACK_MAX);
                if (actualVersion.equals(GET_JUST_LAST_VERSION)) {
                    tableInfo = array.getJSONObject(0);
                } else {
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
            }
            String version = tableInfo.getString(VERSION_KEY);
            int size = Integer.parseInt(tableInfo.getString(PART_KEY));
            int fileCount = Integer.parseInt(tableInfo.getString(FILE_COUNT_KEY));
            int hddSize = Integer.parseInt(tableInfo.getString(SIZE_KEY)) / 1024 / 1024;
            int totalRows = Integer.parseInt(tableInfo.getString(TOTAL_COUNT_KEY));
            t = new TableInfo(version, size, fileCount, hddSize, totalRows);
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }
        return t;
    }


    public static Table convert(String input) {
        if (input != null && !input.equals("")) {
            String[] lines = input.split("\n");
            if (lines.length > 0) {
                Table table = new Table(lines[0], lines.length - 1);

                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    String[] values = new String[table.getHeaders().length];
                    int idx = 0;
                    for (String s : line.split("\\|")) {
                        values[idx] = s;
                        idx++;
                    }
                    table.addValues(values);
                }

                return table;
            }
        }
        return null;
    }

    public static byte[] getFile(String baseUrl, String tableName, String version, int start, int limit) {
        int nextStep = start + limit;
        return getFile(baseUrl + "/csv-zip/" + tableName + "/" + version + "/" + start + "-" + nextStep + ".zip");
    }

    public static byte[] getFile(String baseUrl) {
        HttpURLConnection conn;
        try {
            URL url = new URL(baseUrl);
            conn = (HttpURLConnection) url.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(url.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();
            return buffer;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getReadme(String baseUrl) throws IOException {
        String result = null;
        URL url = new URL(baseUrl + "/readme.txt");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {

            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            Scanner s = new Scanner(in).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            Log.d(PackManager.TAG, Objects.requireNonNull(e.getMessage()));
        } finally {
            urlConnection.disconnect();
        }

        return result;
    }

    public static boolean insertToTable(Table table, String tableName, DaoSession mDaoSession) {
        boolean result;

        StringBuilder builder = new StringBuilder();
        for (String name : table.getHeaders()) {
            builder.append("?,");
        }

        String mParams = builder.substring(0, builder.length() - 1);
        Database database = mDaoSession.getDatabase();
        database.beginTransaction();

        DatabaseStatement mStatement = database.compileStatement("INSERT INTO " + tableName + "(" + table.getHeadersLineForSql() + ")" + " VALUES(" + mParams + ")");

        try {
            for (int i = 0; i < table.count(); i++) {
                for (int j = 0; j < table.getHeaders().length; j++) {
                    String value = table.getValue(i)[j];
                    if (value == null) {
                        mStatement.bindNull(j + 1);
                    } else {
                        mStatement.bindString(j + 1, value);
                    }
                }
                mStatement.execute();
                mStatement.clearBindings();
            }

            database.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            Log.d(PackManager.TAG, Objects.requireNonNull(e.getMessage()));
            result = false;
        } finally {
            database.endTransaction();
        }

        return result;
    }

    public static Readme convertReadme(String input) {
        if (input != null && !input.equals("")) {
            String[] lines = input.split("\n");
            if (lines.length > 0) {
                Readme table = new Readme(lines[0], lines.length - 1);

                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    String[] values = new String[table.getHeaders().length];
                    int idx = 0;
                    for (String s : line.split("\\|")) {
                        values[idx] = s;
                        idx++;
                    }

                    table.addValues(values);
                }

                return table;
            }
        }
        return null;
    }
}
