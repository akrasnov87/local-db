package ru.mobnius.localdb.data;

import android.os.AsyncTask;

import org.greenrobot.greendao.database.Database;

import ru.mobnius.localdb.adapter.holder.StorageNameHolder;

public class DeleteTableAsyncTask extends AsyncTask<Database, Void, Void> {
    private final String mTableName;
    private final StorageNameHolder.OnDeleteTableListener mOnDeleteTableListener;
    private final int mPosition;

    public DeleteTableAsyncTask(String tableName,StorageNameHolder.OnDeleteTableListener onDeleteTableListener, int position){
        mTableName = tableName;
        mOnDeleteTableListener = onDeleteTableListener;
        mPosition = position;
    }
    @Override
    protected Void doInBackground(Database... databases) {
        Database db = databases[0];
        db.execSQL("delete from " + mTableName);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        PreferencesManager.getInstance().setLocalRowCount("0", mTableName);
        mOnDeleteTableListener.onTableDeleted(mPosition);
    }

}
