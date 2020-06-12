package ru.mobnius.localdb;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.util.ArrayList;
import java.util.List;

import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.LogItem;

public class App extends Application
        implements OnLogListener, AvailableTimerTask.OnAvailableListener {

    private AutoRunReceiver mAutoRunReceiver;

    private List<OnLogListener> mLogListeners;
    private List<AvailableTimerTask.OnAvailableListener> mAvailableListeners;

    @Override
    public void onCreate() {
        super.onCreate();
        mLogListeners = new ArrayList<>();
        mAvailableListeners = new ArrayList<>();


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
        unregisterReceiver(mAutoRunReceiver);
        super.onTerminate();
    }
}
