package ru.mobnius.localdb.data;

import android.database.Cursor;
import android.os.AsyncTask;

import ru.mobnius.localdb.HttpService;

public class StorageDataCountAsyncTask extends AsyncTask<String, Void, Long> {
    private final OnStorageCountListener mListener;
    private String tableName;

    public StorageDataCountAsyncTask(OnStorageCountListener listener) {
        mListener = listener;
    }

    @Override
    protected Long doInBackground(String... strings) {
        tableName = strings[0];
        Cursor cursor = HttpService.getDaoSession().getDatabase().rawQuery("SELECT COUNT(*) FROM " + strings[0], null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
        if (aLong != null) {
            PreferencesManager.getInstance().setLocalRowCount(String.valueOf(aLong), tableName);
        }
        mListener.onStorageCount(aLong);
    }

    public interface OnStorageCountListener {
        void onStorageCount(Long count);
    }
}
