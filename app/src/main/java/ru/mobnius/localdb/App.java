package ru.mobnius.localdb;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.ConnectionChecker;
import ru.mobnius.localdb.data.OnHttpListener;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.exception.ExceptionCode;
import ru.mobnius.localdb.data.exception.ExceptionGroup;
import ru.mobnius.localdb.data.exception.OnExceptionIntercept;
import ru.mobnius.localdb.data.exception.MyUncaughtExceptionHandler;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;
public class App extends Application implements
        OnLogListener,
        AvailableTimerTask.OnAvailableListener,
        OnHttpListener,
        OnExceptionIntercept {

    private AutoRunReceiver mAutoRunReceiver;
    private ConnectionChecker mConnectionReceiver;

    private List<OnLogListener> mLogListeners;
    private List<AvailableTimerTask.OnAvailableListener> mAvailableListeners;
    private List<OnHttpListener> mHttpListeners;


    @Override
    public void onCreate() {
        super.onCreate();
        onExceptionIntercept();

        Log.d(Names.TAG, "Старт приложения");
        mLogListeners = new ArrayList<>();
        mAvailableListeners = new ArrayList<>();
        mHttpListeners = new ArrayList<>();

        PreferencesManager.createInstance(this, PreferencesManager.NAME);

        mConnectionReceiver = new ConnectionChecker();
        IntentFilter filterq = new IntentFilter();
        filterq.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, filterq);

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
    public ConnectionChecker getConnectionReceiver(){
        return mConnectionReceiver;
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
        unregisterReceiver( mAutoRunReceiver);
        unregisterReceiver( mConnectionReceiver);
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
    public void onDownLoadProgress(UrlReader reader, int progress, int total) {
        for(OnHttpListener listener : mHttpListeners) {
            listener.onDownLoadProgress(reader, progress, total);
        }
    }

    @Override
    public void onDownLoadFinish(String tableName, UrlReader reader) {
        for(OnHttpListener listener : mHttpListeners) {
            listener.onDownLoadFinish(tableName, reader);
        }
    }

    @Override
    public void onExceptionIntercept() {
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), getExceptionGroup(), getExceptionCode(), this));
    }

    @Override
    public String getExceptionGroup() {
        return ExceptionGroup.APPLICATION;
    }

    @Override
    public int getExceptionCode() {
        return ExceptionCode.ALL;
    }
}
