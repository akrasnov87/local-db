package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.util.TimeZone;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.utils.FileUtil;
import ru.mobnius.localdb.utils.StorageUtil;
import ru.mobnius.localdb.utils.UnzipUtil;

public class InsertHandler extends HandlerThread {
    private boolean mHasQuit = false;
    private static final int INSERT_ROWS = 0;
    private Handler mRequestHandler;
    private static final String TAG = "InsertHandler";
    private Context mContext;
    private String mCurrentTableName;

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public InsertHandler(Context context, String tableName) {
        super(TAG);
        mContext = context;
        mCurrentTableName = tableName;
    }

    public void insert(String target) {
        mRequestHandler.obtainMessage(INSERT_ROWS, target)
                .sendToTarget();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == INSERT_ROWS) {
                    String target = (String) msg.obj;
                    if (target.contains("UI_SV_FIAS:2220000-2230000")||target.contains("ED_Network_Routes:460000-470000")
                            ||target.contains("ED_Registr_Pts:1600000-1610000")||target.contains("ED_Device_Billing:1560000-1570000")){
                        Log.e("hak", "Вставлен архив" + System.currentTimeMillis());
                    }
                    UnzipUtil unzipUtil = new UnzipUtil(target,
                            FileUtil.getRoot(mContext, Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
                    String unzipped = unzipUtil.unzip();
                    StorageUtil.processings(HttpService.getDaoSession(), unzipped.trim(), mCurrentTableName, false, target);

                }
            }
        };
    }


    public interface ZipDownloadListener {
        void onZipDownloaded(String zipFilePath);
    }
}
