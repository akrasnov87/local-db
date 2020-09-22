package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.File;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.observer.EventListener;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.utils.FileUtil;
import ru.mobnius.localdb.utils.StorageUtil;
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
                        StorageUtil.processings(HttpService.getDaoSession(), unzipped.trim(), mCurrentTableName, false, target);
                        File file = new File(target);
                        file.delete();
                        File unzippedFile = new File(unzippedFilePath);
                        unzippedFile.delete();
                        String current = target.split(":")[1];
                        int cur = Integer.parseInt(current.split("-")[1]);
                        mUpdateUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(cur>=PreferencesManager.getInstance().getProgress().total)
                                mListener.onInsertProgress(mCurrentTableName, cur, PreferencesManager.getInstance().getProgress().total);

                            }
                        });

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


    public interface ZipDownloadListener {
        void onZipDownloaded(String zipFilePath);
    }
}
