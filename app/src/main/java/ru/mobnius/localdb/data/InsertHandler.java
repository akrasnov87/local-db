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

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.data.tablePack.TableInsertKeyValue;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.FileUtil;
import ru.mobnius.localdb.utils.UnzipUtil;

public class InsertHandler extends HandlerThread implements EventListener {
    private boolean mHasQuit = false;
    private static final int INSERT_ROWS = 0;
    private Handler mInsertHandler;
    private ConcurrentMap<String, String> mInsertMap = new ConcurrentHashMap<>();
    private static final String TAG = "InsertHandler";
    private App mApp;
    private Handler mUpdateUIHandler;

    public void insert(String tableName, String filePath) {
        mInsertHandler.obtainMessage(INSERT_ROWS, new TableInsertKeyValue(tableName, filePath))
                .sendToTarget();
    }

    private LoadAsyncTask.OnLoadListener mListener;

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public InsertHandler(App context, LoadAsyncTask.OnLoadListener listener, Handler handler) {
        super(TAG);
        mApp = context;
        mListener = listener;
        mUpdateUIHandler = handler;
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mInsertHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!mHasQuit) {
                    if (msg.what == INSERT_ROWS) {
                        TableInsertKeyValue target = (TableInsertKeyValue) msg.obj;
                        UnzipUtil unzipUtil = new UnzipUtil(target.getFilePath(),
                                FileUtil.getRoot(mApp, Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
                        String unzipped = unzipUtil.unzip();
                        if (unzipped != null) {
                            String unzippedFilePath = unzipUtil.getAbsPath();
                            String current = target.getFilePath().split(":")[1];
                            int cur = Integer.parseInt(current.split("-")[0]);
                            int total = Integer.parseInt(PreferencesManager.getInstance().getRemoteRowCount(target.getTableName()));
                            processing(HttpService.getDaoSession(), unzipped.trim(), target.getTableName(), target.getFilePath(), cur, total);
                            if (!mHasQuit) {
                                PreferencesManager.getInstance().setProgress(new Progress(cur, total, target.getTableName(), null));
                            }
                            File file = new File(target.getFilePath());
                            file.delete();
                            File unzippedFile = new File(unzippedFilePath);
                            unzippedFile.delete();
                            if (cur + 10000 >= total) {
                                mUpdateUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mListener.onInsertFinish(target.getTableName());
                                        PreferencesManager.getInstance().setProgress(null);
                                    }
                                });
                            } else {
                                mUpdateUIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mListener.onInsertProgress(target.getTableName(), Integer.parseInt(PreferencesManager.getInstance().getLocalRowCount(target.getTableName())), total);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        };
    }

    @Override
    public void update(String eventType, String... args) {
        if (eventType.equals(Observer.STOP_THREAD)) {
            mInsertHandler.removeMessages(INSERT_ROWS);
            mInsertMap.clear();
            quit();
            if (PreferencesManager.getInstance().getProgress() != null) {
                PreferencesManager.getInstance().setProgress(null);
            }
        }
    }

    private void processing(DaoSession daoSession, String unzipped, String tableName, String zip, int cur, int total) throws SQLiteFullException, SQLiteConstraintException {
        Database db = daoSession.getDatabase();

        AbstractDao abstractDao = null;

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
                        for (int j = 3; j < s.length; j += 6) {
                            String sd = "";
                            if (s[j].equals("false") || s[j].equals("true")) {
                                sd = "ok";
                            } else {
                                sd = "not ok";
                            }
                            String t = sd + "fdf";
                        }
                        try {
                            db.execSQL(sqlInsertFromString.convertToSqlQuery(max), s);
                            cur += max;
                            PreferencesManager.getInstance().setLocalRowCount(String.valueOf(cur), tableName);
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
                        PreferencesManager.getInstance().setLocalRowCount(String.valueOf(cur), tableName);
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
        void onZipDownloaded(String tableName, String zipFilePath);
    }
}
