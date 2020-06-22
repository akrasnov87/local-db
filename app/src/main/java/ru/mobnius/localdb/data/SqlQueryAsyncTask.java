package ru.mobnius.localdb.data;

import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SqlQueryAsyncTask extends AsyncTask<String, Void, String> {
    private Database mDatabase;
    private OnSqlQuery mListener;
    private boolean isError = false;
     public SqlQueryAsyncTask(Database database, OnSqlQuery listener){
         mDatabase = database;
         mListener = listener;
     }

    @Override
    protected String doInBackground(String... strings) {
        Cursor cursor;
        try {
            cursor = mDatabase.rawQuery(strings[0], null);
        } catch (SQLException e) {
            isError = true;
            return e.toString();
        }
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        int maxCount = 0;
        while (!cursor.isAfterLast()&&maxCount<1000) {
            maxCount++;
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        isError = true;
                        return e.toString();
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        String s;
        if (resultSet.length()==0){
            isError = true;
            return  "Результат запроса пуст";
        }
        try {
            s = resultSet.toString(4);
        } catch (JSONException e) {
            isError = true;
            return e.toString();
        }
        return s;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mListener.onSqlQueryCompleted(s, isError);
    }

    public interface OnSqlQuery {
        void onSqlQueryCompleted(String queryResult, boolean isError);
    }
}
