package ru.mobnius.localdb;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.OnHttpListener;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public class App extends Application implements
        OnLogListener,
        AvailableTimerTask.OnAvailableListener,
        OnHttpListener {

    private AutoRunReceiver mAutoRunReceiver;

    private List<OnLogListener> mLogListeners;
    private List<AvailableTimerTask.OnAvailableListener> mAvailableListeners;
    private List<OnHttpListener> mHttpListeners;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Names.TAG, "Старт приложения");
        mLogListeners = new ArrayList<>();
        mAvailableListeners = new ArrayList<>();
        mHttpListeners = new ArrayList<>();

        PreferencesManager.createInstance(this, PreferencesManager.NAME);

        mAutoRunReceiver = new AutoRunReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(mAutoRunReceiver, filter);
    }

    public void registryLogListener(OnLogListener listener) {
        mLogListeners.add(listener);
    }

    public void unRegistryLogListener(OnLogListener listener) {
        mLogListeners.remove(listener);
    }

    public void registryHttpListener(OnHttpListener listener) {
        mHttpListeners.add(listener);
    }

    public void unRegistryHttpListener(OnHttpListener listener) {
        mHttpListeners.remove(listener);
    }

    public void registryAvailableListener(AvailableTimerTask.OnAvailableListener listener) {
        mAvailableListeners.add(listener);
    }

    public void unRegistryAvailableListener(AvailableTimerTask.OnAvailableListener listener) {
        mAvailableListeners.remove(listener);
    }

    @Override
    public void onAddLog(LogItem item) {
        for(OnLogListener listener : mLogListeners) {
            listener.onAddLog(item);
        }
    }

    @Override
    public void onAvailable(boolean available) {
        for(AvailableTimerTask.OnAvailableListener listener : mAvailableListeners) {
            listener.onAvailable(available);
        }
    }

    @Override
    public void onTerminate() {
        Log.d(Names.TAG, "Остановка приложения");
        unregisterReceiver(mAutoRunReceiver);
        super.onTerminate();
    }

    @Override
    public void onHttpRequest(UrlReader reader) {
        for(OnHttpListener listener : mHttpListeners) {
            listener.onHttpRequest(reader);
        }
    }

    @Override
    public void onHttpResponse(Response response) {
        for(OnHttpListener listener : mHttpListeners) {
            listener.onHttpResponse(response);
        }
    }

    @Override
    public void onDownLoadProgress(UrlReader reader, Progress progress) {
        Log.d(Names.TAG, "HttpListeners: " + mHttpListeners.size());
        for(OnHttpListener listener : mHttpListeners) {
            listener.onDownLoadProgress(reader, progress);
        }
    }

    @Override
    public void onDownLoadFinish(UrlReader reader) {
        for(OnHttpListener listener : mHttpListeners) {
            listener.onDownLoadFinish(reader);
        }
    }
}
