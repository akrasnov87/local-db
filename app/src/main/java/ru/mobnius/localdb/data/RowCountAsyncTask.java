package ru.mobnius.localdb.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.model.Progress;

public class RowCountAsyncTask extends AsyncTask<String, Void, Integer> {
    private final App mApp;
    private final LoadAsyncTask.OnLoadListener mListener;
    private BroadcastReceiver mMessageReceiver;

    public RowCountAsyncTask(App app, LoadAsyncTask.OnLoadListener listener) {
        mApp = app;
        mListener = listener;
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Tags.CANCEL_TASK_TAG)) {
                    LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mMessageReceiver);
                    RowCountAsyncTask.this.cancel(false);
                }
            }
        };
        LocalBroadcastManager.getInstance(mApp).registerReceiver(
                mMessageReceiver, new IntentFilter(Tags.CANCEL_TASK_TAG));
    }

    @Override
    protected Integer doInBackground(String... strings) {
        if (!isCancelled()) {
            String tableName = strings[0];
            String query = "Select count(1) as length from " + tableName;
            StringBuilder sb = new StringBuilder();
            Cursor cursor = null;
            try {
                cursor = HttpService.getDaoSession().getDatabase().rawQuery(query, null);
            } catch (SQLException e) {
                Logger.error(e);
            }
            JSONObject rowObject = new JSONObject();
            if (cursor != null) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast() && sb.length() < 4096) {
                    int totalColumn = cursor.getColumnCount();
                    for (int i = 0; i < totalColumn; i++) {
                        if (cursor.getColumnName(i) != null) {
                            try {
                                if (cursor.getString(i) != null) {
                                    rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                                } else {
                                    rowObject.put(cursor.getColumnName(i), "");
                                }
                            } catch (Exception e) {
                                Logger.error(e);
                            }
                        }
                    }
                    cursor.moveToNext();
                }
                cursor.close();
            }
            if (!rowObject.has("length")) {
                return 0;
            } else {
                try {
                    return Integer.parseInt(rowObject.getString("length"));
                } catch (JSONException e) {
                    Logger.error(e);
                }
            }
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        if (integer != 0 && integer % 10000 == 0 && !isCancelled()) {
            Progress progress = PreferencesManager.getInstance().getProgress();
            progress.current = integer;
            PreferencesManager.getInstance().setProgress(progress);
            PreferencesManager.getInstance().setLocalRowCount(String.valueOf(integer), progress.tableName);

            PreferencesManager.getInstance().setAllTablesArray(new String[] {progress.tableName});

            new LoadAsyncTask(PreferencesManager.getInstance().getAllTablesArray(), mListener, mApp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PreferencesManager.getInstance().getLogin(), PreferencesManager.getInstance().getPassword());
        }
        LocalBroadcastManager.getInstance(mApp).unregisterReceiver(mMessageReceiver);
    }
}
