package ru.mobnius.localdb;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telecom.Connection;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.mobnius.localdb.data.HttpServerThread;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.OnResponseListener;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.exception.ExceptionCode;
import ru.mobnius.localdb.data.exception.ExceptionGroup;
import ru.mobnius.localdb.data.exception.ExceptionUtils;
import ru.mobnius.localdb.data.exception.FileExceptionManager;
import ru.mobnius.localdb.data.exception.MyUncaughtExceptionHandler;
import ru.mobnius.localdb.data.exception.OnExceptionIntercept;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.request.AuthRequestListener;
import ru.mobnius.localdb.request.DefaultRequestListener;
import ru.mobnius.localdb.request.ErrorRequestListener;
import ru.mobnius.localdb.request.InfoRequestListener;
import ru.mobnius.localdb.request.OnRequestListener;
import ru.mobnius.localdb.request.SyncRequestListener;
import ru.mobnius.localdb.request.SyncStatusRequestListener;
import ru.mobnius.localdb.request.SyncStopRequestListener;
import ru.mobnius.localdb.request.TableRequestListener;
import ru.mobnius.localdb.request.InvokeUpdateActivityListener;
import ru.mobnius.localdb.request.VersionRequestListener;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.utils.UrlReader;

public class HttpService extends Service
        implements OnResponseListener,
        OnLogListener {

    public static final int AUTO = 1;
    public static final int MANUAL = 2;

    private static final String MODE = "mode";
    private static final String TABLE = "table";

    public static Intent
    getIntent(Context context, int mode) {
        Intent intent = new Intent();
        intent.setClass(context, HttpService.class);
        intent.putExtra(MODE, mode);
        return intent;
    }

    public static Intent getIntent(Context context, String tableName) {
        Intent intent = new Intent();
        intent.setClass(context, HttpService.class);
        intent.putExtra(MODE, MANUAL);
        intent.putExtra(TABLE, tableName);
        return intent;
    }

    /**
     * Имя сервиса
     */
    public static final String SERVICE_NAME = "ru.mobnius.localdb.HttpService";

    private static DaoSession mDaoSession;

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }

    private HttpServerThread sHttpServerThread;

    private final List<OnRequestListener> mRequestListeners;

    public HttpService() {
        mRequestListeners = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel;
        String channelId = "httpServiceClientLocalDB";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(channelId, "ltnChannel", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, channelId).setContentTitle("").setContentText("").build();
            startForeground(1, notification);
        }
        App app = (App) getApplication();
        mDaoSession = app.getDaoSession();

        mRequestListeners.add(new DefaultRequestListener());
        SyncStatusRequestListener syncStatusRequestListener = new SyncStatusRequestListener();
        SyncRequestListener syncRequestListener = new SyncRequestListener((App) getApplication(), syncStatusRequestListener);
        mRequestListeners.add(syncRequestListener);
        mRequestListeners.add(syncStatusRequestListener);
        mRequestListeners.add(new AuthRequestListener((App) getApplication()));
        mRequestListeners.add(new SyncStopRequestListener((App) getApplication()));
        mRequestListeners.add(new TableRequestListener());
        mRequestListeners.add(new ErrorRequestListener());
        mRequestListeners.add(new InfoRequestListener(this));
        mRequestListeners.add(new InvokeUpdateActivityListener((App) getApplication()));
        mRequestListeners.add(new VersionRequestListener((App) getApplication()));

        sHttpServerThread = new HttpServerThread(this);
        sHttpServerThread.start();
        Log.e(Names.TAG, "Http Service start");

        // для возобновления после destroy
        if (PreferencesManager.getInstance() != null) {
            Progress progress = PreferencesManager.getInstance().getProgress();
            if (progress != null) {
                onAddLog(new LogItem("Возобновление загрузки " + progress.tableName, false));
                onResponse(new UrlReader("GET /sync?table=" + progress.tableName + "&restore=true HTTP/1.1"));
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String strMode;
        if (intent != null) {
            int mode = intent.getIntExtra(MODE, 0);
            switch (mode) {
                case AUTO:
                    strMode = "автоматически";
                    break;

                case MANUAL:
                    strMode = "в ручную";
                    break;

                default:
                    strMode = "неизвестным образом";
                    break;
            }
        } else {
            strMode = "автоматически";
        }
        // запуск синхронизации через интерфейс
        if (intent != null && intent.hasExtra(TABLE)) {
            String table = intent.getStringExtra(TABLE);
            onResponse(new UrlReader("GET /sync?table=" + table + " HTTP/1.1"));
        } else {
            ((App) getApplication()).onAddLog(new LogItem("служба и хост запущены " + strMode, false));
        }

        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Names.TAG, "Остановка сервиса");
        sHttpServerThread.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Обработчик для HTTP запросов
     *
     * @param urlReader служебный класс для анализа строки запроса
     * @return результат запроса
     */
    @Override
    public Response onResponse(UrlReader urlReader) {
        ((App) getApplication()).onHttpRequest(urlReader);

        for (OnRequestListener req :
                mRequestListeners) {
            if (req.isValid(urlReader.getParts()[1])) {
                Response response = req.getResponse(urlReader);
                ((App) getApplication()).onHttpResponse(response);
                return response;
            }
        }
        Response defaultResponse = new DefaultRequestListener(Response.RESULT_NOT_FOUNT, "NOT FOUND").getResponse(urlReader);
        ((App) getApplication()).onHttpResponse(defaultResponse);
        return defaultResponse;
    }


    @Override
    public void onAddLog(LogItem item) {
        ((App) getApplication()).onAddLog(item);
    }

    public class MyBinder extends Binder {
        public HttpService getService() {
            return HttpService.this;
        }
    }

}
