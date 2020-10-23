package ru.mobnius.localdb;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.User;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.storage.ClientErrors;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.utils.UrlReader;

public class App extends Application implements
        OnLogListener,
        AvailableTimerTask.OnAvailableListener,
        OnHttpListener {

    private AutoRunReceiver mAutoRunReceiver;
    private ConnectionChecker mConnectionReceiver;

    private List<OnLogListener> mLogListeners;
    private List<AvailableTimerTask.OnAvailableListener> mAvailableListeners;
    private List<OnHttpListener> mHttpListeners;
    private Observer mObserver;
    private DaoSession mDaoSession;


    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        {
            // Setup handler for uncaught exceptions.
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable e) {
                    handleUncaughtException(thread, e);
                }
            });
        }
        Log.d(Names.TAG, "Старт приложения");
        mLogListeners = new ArrayList<>();
        mAvailableListeners = new ArrayList<>();
        mHttpListeners = new ArrayList<>();
        PreferencesManager.createInstance(this, PreferencesManager.NAME);
        mConnectionReceiver = new ConnectionChecker();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, intentFilter);
        mAutoRunReceiver = new AutoRunReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(mAutoRunReceiver, filter);
        mObserver = new Observer(Observer.STOP_ASYNC_TASK, Observer.STOP_THREAD, Observer.ERROR);
        DbOpenHelper dbOpenHelper = new DbOpenHelper(this, "local-db.db");
        mDaoSession = new DaoMaster(dbOpenHelper.getWritableDb()).newSession();
        //dbOpenHelper.onUpgrade((SQLiteDatabase) mDaoSession.getDatabase().getRawDatabase(), 1, 2);
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

    public ConnectionChecker getConnectionReceiver() {
        return mConnectionReceiver;
    }

    public Observer getObserver() {
        return mObserver;
    }

    @Override
    public void onAddLog(LogItem item) {
        for (OnLogListener listener : mLogListeners) {
            listener.onAddLog(item);
        }
    }

    @Override
    public void onAvailable(boolean available) {
        for (AvailableTimerTask.OnAvailableListener listener : mAvailableListeners) {
            listener.onAvailable(available);
        }
    }

    @Override
    public void onTerminate() {
        Log.d(Names.TAG, "Остановка приложения");
        unregisterReceiver(mAutoRunReceiver);
        unregisterReceiver(mConnectionReceiver);
        super.onTerminate();
    }

    @Override
    public void onHttpRequest(UrlReader reader) {
        for (OnHttpListener listener : mHttpListeners) {
            listener.onHttpRequest(reader);
        }
    }

    @Override
    public void onHttpResponse(Response response) {
        for (OnHttpListener listener : mHttpListeners) {
            listener.onHttpResponse(response);
        }
    }

    @Override
    public void onDownLoadProgress(UrlReader reader, int progress, int total) {
        for (OnHttpListener listener : mHttpListeners) {

            listener.onDownLoadProgress(reader, progress, total);
        }
    }

    @Override
    public void onDownLoadFinish(String tableName, UrlReader reader) {
        for (OnHttpListener listener : mHttpListeners) {
            listener.onDownLoadFinish(tableName, reader);
        }
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        if (getDaoSession() != null && getDaoSession().getClientErrorsDao().loadAll().size() < 10) {
            ClientErrors error = new ClientErrors();
            error.setId(UUID.randomUUID().toString());
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = DateFormat.getDateInstance();
            String strDate = dateFormat.format(date);
            error.setDate(strDate);
            error.setMessage(getStackTrace(e));
            if (PreferencesManager.getInstance() != null) {
                if (PreferencesManager.getInstance().getLogin() != null) {
                    error.setUser(PreferencesManager.getInstance().getLogin());
                }
            }
           getDaoSession().getClientErrorsDao().insert(error);
        }

        /*
        Intent intent = new Intent ();
        intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity (intent);
        */
        System.exit(1); // kill off the crashed app
    }
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
