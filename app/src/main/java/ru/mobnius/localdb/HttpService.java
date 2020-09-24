package ru.mobnius.localdb;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
import ru.mobnius.localdb.data.tablePack.OnRunnableLoadListeners;
import ru.mobnius.localdb.data.tablePack.PackManager;
import ru.mobnius.localdb.data.tablePack.RunnableLoad;
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
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.utils.UrlReader;

public class HttpService extends Service
        implements OnResponseListener,
        OnLogListener,
        OnExceptionIntercept {

    public static final int AUTO = 1;
    public static final int MANUAL = 2;

    private static final String MODE = "mode";
    private static final String TABLE = "table";

    public static Intent getIntent(Context context, int mode) {
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
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
        onExceptionIntercept();
        mDaoSession = new DaoMaster(new DbOpenHelper(getApplication(), "local-db.db").getWritableDb()).newSession();
        String message;
        File root = FileExceptionManager.getInstance(this).getRootCatalog();
        String[] files = root.list();
        if (files != null) {
            for (String fileName : files) {
                byte[] bytes = FileExceptionManager.getInstance(this).readPath(fileName);
                if (bytes != null) {
                    message = new String(bytes);
                    if (message.length() > 2000) {
                        message = message.substring(0, 1000) + ".........\n" + message.substring(message.length() - 1000, message.length() - 1);
                    }
                    App app = (App) getApplication();
                    String[] strings = {Tags.CRITICAL_ERROR, message};
                    app.getObserver().notify(Observer.ERROR, strings);
                }
            }
        }
        ExceptionUtils.saveLocalException(this, mDaoSession);

        mRequestListeners.add(new DefaultRequestListener());
        SyncStatusRequestListener syncStatusRequestListener = new SyncStatusRequestListener((App) getApplication());
        SyncRequestListener syncRequestListener = new SyncRequestListener((App) getApplication(), syncStatusRequestListener);
        mRequestListeners.add(syncRequestListener);
        mRequestListeners.add(syncStatusRequestListener);
        mRequestListeners.add(new AuthRequestListener(this));
        mRequestListeners.add(new SyncStopRequestListener((App) getApplication()));
        mRequestListeners.add(new TableRequestListener());
        mRequestListeners.add(new ErrorRequestListener());
        mRequestListeners.add(new InfoRequestListener(this));

        sHttpServerThread = new HttpServerThread(this);
        sHttpServerThread.start();
        Log.d(Names.TAG, "Http Service start");

        // для возобновления после destroy
        Progress progress = PreferencesManager.getInstance().getProgress();
        if (progress != null) {
            onAddLog(new LogItem("Возобновление загрузки " + progress.tableName, false));
            onResponse(new UrlReader("GET /sync?table=" + progress.tableName + "&restore=true HTTP/1.1"));
        }

        /*PackManager packManager = new PackManager(mDaoSession, "http://demo.it-serv.ru/repo", "UI_SV_FIAS", "1.2.641");
        mDaoSession.getFiasDao().deleteAll();
        mDaoSession.getFiasDao().detachAll();
       /* PackManager packManager = new PackManager(mDaoSession, "http://demo.it-serv.ru/repo", "ED_Network_Routes", "1.3.847");
        mDaoSession.getRegistrPtsDao().deleteAll();
        mDaoSession.getRegistrPtsDao().detachAll();

        long d_start = new Date().getTime();
        packManager.start(new OnRunnableLoadListeners() {
            @Override
            public void onBufferSuccess(int count) {

            }

            @Override
            public void onBufferInsert(int count) {

            }

            @Override
            public void onBufferEmpty() {

            }

            @Override
            public void onLoaded() {
                long d_end = new Date().getTime();
                Log.d(PackManager.TAG, String.valueOf(d_end - d_start));
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onProgress(int start, int total) {

            }
        }, 0);
    }
*/
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

    /**
     * Обработчик перехвата ошибок
     */
    public void onExceptionIntercept() {
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), getExceptionGroup(), getExceptionCode(), this));
    }

    /**
     * Группа ошибки из IExceptionGroup
     *
     * @return строка
     */
    public String getExceptionGroup() {
        return ExceptionGroup.SERVICE;
    }

    @Override
    public int getExceptionCode() {
        return ExceptionCode.HTTP_SERVICE;
    }
}
