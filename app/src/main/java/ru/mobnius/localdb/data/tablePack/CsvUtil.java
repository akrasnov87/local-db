package ru.mobnius.localdb.data.tablePack;

import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

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

    public static boolean insertToTable(Table table, String tableName, AbstractDaoSession session) {
        boolean result;
        Database db = session.getDatabase();
        AbstractDao abstractDao = null;
        for (AbstractDao ad : session.getAllDaos()) {
            if (ad.getTablename().equals(tableName)) {
                abstractDao = ad;
                break;
            }
        }

        db.beginTransaction();
        try {
            SqlStatementInsert sqlStatementInsert = new SqlStatementInsert(table, tableName, abstractDao);
            for (int i = 0; i < table.count(); i++) {
                sqlStatementInsert.bind(i);
            }

            db.setTransactionSuccessful();
            result = true;
        }catch (Exception e) {
            result = false;
        }
        finally {
            db.endTransaction();
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
