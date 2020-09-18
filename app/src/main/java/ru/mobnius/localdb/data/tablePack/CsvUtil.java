package ru.mobnius.localdb.data.tablePack;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.gson.JsonObject;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import ru.mobnius.localdb.storage.DaoSession;

public class CsvUtil {
    public static Table convert(String input) {
        if(input != null && !input.equals("")) {
            String[] lines = input.split("\n");
            if(lines.length > 0) {
                Table table = new Table(lines[0], lines.length - 1);

                for(int i = 1; i < lines.length; i++) {
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

    /**
     *
     * @param baseUrl http://demo.it-serv.ru/repo
     * @param tableName
     * @param version
     * @param start
     * @param limit
     * @return
     */
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
        }
        catch (Exception e) {
            Log.d(PackManager.TAG, e.getMessage());
        }
        finally {
            urlConnection.disconnect();
        }

        return result;
    }

    public static JSONObject getInfo(String baseUrl) {
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
        }
        catch (Exception e) {
            Log.d(PackManager.TAG, e.getMessage());
        }
        finally {
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
                for(int j = 0; j < table.getHeaders().length; j++) {
                    String value = table.getValue(i)[j];
                    if(value == null) {
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
        }catch (Exception e) {
            Log.d(PackManager.TAG, e.getMessage());
            result = false;
        }
        finally {
            database.endTransaction();
        }

        return result;
    }

    public static Readme convertReadme(String input) {
        if(input != null && !input.equals("")) {
            String[] lines = input.split("\n");
            if(lines.length > 0) {
                Readme table = new Readme(lines[0], lines.length - 1);

                for(int i = 1; i < lines.length; i++) {
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
