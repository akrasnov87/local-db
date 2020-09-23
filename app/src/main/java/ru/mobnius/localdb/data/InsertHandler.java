package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.util.Arrays;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.FileUtil;
import ru.mobnius.localdb.utils.UnzipUtil;

public class InsertHandler extends HandlerThread implements EventListener {
    private boolean mHasQuit = false;
    private static final int INSERT_ROWS = 0;
    private Handler mInsertHandler;
    private static final String TAG = "InsertHandler";
    private Context mContext;
    private Handler mUpdateUIHandler;

    public void setTableName(String mCurrentTableName) {
        this.mCurrentTableName = mCurrentTableName;
    }

    public String getTableName() {
        return mCurrentTableName;
    }

    private String mCurrentTableName;
    private LoadAsyncTask.OnLoadListener mListener;

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public InsertHandler(Context context, LoadAsyncTask.OnLoadListener listener, Handler handler) {
        super(TAG);
        mContext = context;
        mListener = listener;
        mUpdateUIHandler = handler;
    }

    public void insert(String target) {
        mInsertHandler.obtainMessage(INSERT_ROWS, target)
                .sendToTarget();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mInsertHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!mHasQuit) {
                    if (msg.what == INSERT_ROWS) {
                        String target = (String) msg.obj;
                        if (target.contains("UI_SV_FIAS:2220000-2230000") || target.contains("ED_Network_Routes:460000-470000")
                                || target.contains("ED_Registr_Pts:1600000-1610000") || target.contains("ED_Device_Billing:1560000-1570000")) {
                            Log.e("hak", "Вставлен архив" + System.currentTimeMillis());
                        }
                        UnzipUtil unzipUtil = new UnzipUtil(target,
                                FileUtil.getRoot(mContext, Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
                        String unzipped = unzipUtil.unzip();
                        String unzippedFilePath = unzipUtil.getAbsPath();
                        String current = target.split(":")[1];
                        int cur = Integer.parseInt(current.split("-")[1]);
                        processing(HttpService.getDaoSession(), unzipped.trim(), mCurrentTableName, false, target, cur, PreferencesManager.getInstance().getProgress().total);
                        File file = new File(target);
                        file.delete();
                        File unzippedFile = new File(unzippedFilePath);
                        unzippedFile.delete();
                        if (cur >= PreferencesManager.getInstance().getProgress().total) {
                            mUpdateUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mListener.onLoadFinish(mCurrentTableName);
                                }
                            });
                        }
                    }
                }
            }
        };
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.STOP)) {
            this.quit();
            if (PreferencesManager.getInstance().getProgress() != null) {
                PreferencesManager.getInstance().setProgress(null);
            }
        }
    }

    private void processing(DaoSession daoSession, String unzipped, String tableName, boolean removeBeforeInsert, String zip, int cur, int total) throws SQLiteFullException, SQLiteConstraintException {
        Database db = daoSession.getDatabase();
        if (removeBeforeInsert) {
            db.execSQL("delete from " + tableName);
            PreferencesManager.getInstance().setLocalRowCount("0", tableName);
        }
        AbstractDao abstractDao = null;
        int insertions = Integer.parseInt(PreferencesManager.getInstance().getLocalRowCount(tableName));

        for (AbstractDao ad : daoSession.getAllDaos()) {
            if (ad.getTablename().equals(tableName)) {
                abstractDao = ad;
                break;
            }
        }
        if (abstractDao == null) {
            return;
        }

        SqlInsertFromString sqlInsertFromString = new SqlInsertFromString(unzipped, tableName);

        Object[] allValues = sqlInsertFromString.getValues();
        if (allValues == null) {
            return;
        }
        if (allValues.length > 0) {
            //Вычисляем максимально возможную вставку за 1 раз. 999 за 1 раз - ограничение SQLite
            int columnsCount = abstractDao.getAllColumns().length;
            int max = 999 / columnsCount;
            int next = max * columnsCount;
            int idx = 0;
            int dataLength = allValues.length;
            db.beginTransaction();
            try {
                for (int i = 0; i < dataLength; i += next) {
                    if (i + next < dataLength) {
                        Object[] s = Arrays.copyOfRange(allValues, i, (i + next));
                        try {
                            db.execSQL(sqlInsertFromString.convertToSqlQuery(max), s);
                            insertions += max;
                            cur += max;
                            PreferencesManager.getInstance().setLocalRowCount(String.valueOf(cur), mCurrentTableName);

                            mUpdateUIHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    mListener.onInsertProgress(mCurrentTableName, Integer.parseInt(PreferencesManager.getInstance().getLocalRowCount(mCurrentTableName)), total);
                                }
                            });
                        } catch (Exception e) {
                            Logger.error(e);
                        }
                    }
                    idx = i;
                }
                if (idx != dataLength) {
                    Object[] s = Arrays.copyOfRange(allValues, idx, dataLength);
                    int last = s.length / columnsCount;

                    try {
                        db.execSQL(sqlInsertFromString.convertToSqlQuery(last), s);
                        cur += last;
                        PreferencesManager.getInstance().setLocalRowCount(String.valueOf(cur), mCurrentTableName);
                        mUpdateUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onInsertProgress(mCurrentTableName, Integer.parseInt(PreferencesManager.getInstance().getLocalRowCount(mCurrentTableName)), total);
                            }
                        });
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                }

            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }
    }

    public interface ZipDownloadListener {
        void onZipDownloaded(String zipFilePath);
    }
}
