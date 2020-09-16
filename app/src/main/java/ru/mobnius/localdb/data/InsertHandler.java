package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InsertHandler<T> extends HandlerThread {
    private boolean mHasQuit = false;
    private static final int INSERT_ROWS = 0;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private static final String TAG = "InsertHandler";

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public InsertHandler() {
        super(TAG);
    }

    public void insert(T target) {

    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == INSERT_ROWS) {
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target) {
        final String insertData = mRequestMap.get(target);
        if (insertData == null) {
            return;
        }
    }
}
