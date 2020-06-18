package ru.mobnius.localdb.data;

import android.database.Cursor;
import android.os.AsyncTask;

import ru.mobnius.localdb.HttpService;

public class StorageDataCountAsyncTask extends AsyncTask<String, Void, Long> {
    private OnStorageCountListener mListener;

    public StorageDataCountAsyncTask(OnStorageCountListener listener) {
        mListener = listener;
    }

    @Override
    protected Long doInBackground(String... strings) {
        Cursor cursor = HttpService.getDaoSession().getDatabase().rawQuery("SELECT COUNT(*) FROM " + strings[0], null);
        if(cursor.moveToFirst()) {
            return cursor.getLong(0);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);

        mListener.onStorageCount(aLong);
    }

    public interface OnStorageCountListener {
        void onStorageCount(Long count);
    }
}
