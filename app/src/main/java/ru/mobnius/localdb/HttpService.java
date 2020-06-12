package ru.mobnius.localdb;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import ru.mobnius.localdb.data.HttpServerThread;
import ru.mobnius.localdb.data.OnLogListener;
import ru.mobnius.localdb.data.OnResponseListener;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.request.DefaultRequestListener;
import ru.mobnius.localdb.request.OnRequestListener;
import ru.mobnius.localdb.utils.UrlReader;

public class HttpService extends Service
        implements OnResponseListener,
        OnLogListener {

    public static final int AUTO = 1;
    public static final int MANUAL = 2;

    private static final String MODE = "mode";

    public static Intent getIntent(Context context, int mode) {
        Intent intent =  new Intent();
        intent.setClass(context, HttpService.class);
        intent.putExtra(MODE, mode);
        return intent;
    }

    /**
     * Имя сервиса
     */
    public static final String SERVICE_NAME = "ru.mobnius.localdb.HttpService";

    private HttpServerThread httpServerThread;

    private List<OnRequestListener> mRequestListeners;

    public HttpService() {
        mRequestListeners = new ArrayList<>();
        mRequestListeners.add(new DefaultRequestListener());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        httpServerThread = new HttpServerThread(this);
        httpServerThread.start();

        int mode = intent.getIntExtra(MODE, 0);
        String strMode;
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

        ((App)getApplication()).onAddLog(new LogItem("служба и хост запущены " + strMode, false));

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        httpServerThread.onDestroy();
        super.onDestroy();
    }

    /**
     * Обработчик для HTTP запросов
     * @param urlReader служебный класс для анализа строки запроса
     * @return результат запроса
     */
    @Override
    public Response onResponse(UrlReader urlReader) {
        for (OnRequestListener req:
             mRequestListeners) {
            if(req.isValid(urlReader.getParts()[1])) {
                return req.getResponse(urlReader);
            }
        }
        return new DefaultRequestListener(Response.RESULT_NOT_FOUNT, "NOT FOUND").getResponse(urlReader);
    }

    @Override
    public void onAddLog(LogItem item) {
        ((App)getApplication()).onAddLog(item);
    }
}
