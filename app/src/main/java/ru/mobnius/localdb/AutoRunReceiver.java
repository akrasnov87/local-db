package ru.mobnius.localdb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;

import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.storage.DaoMaster;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.storage.DbOpenHelper;
import ru.mobnius.localdb.utils.ServiceUtil;

public class AutoRunReceiver extends BroadcastReceiver
        implements AvailableTimerTask.OnAvailableListener {
    private static DaoSession mDaoSession;

    public static String getRpcUrl() {
        String baseUrl = "http://demo.it-serv.ru";
        String virtualDirPath = "/MobileServiceSevKav";

        return baseUrl + virtualDirPath;
    }

    public static String getNodeUrl() {
        String baseUrl = "http://demo.it-serv.ru";
        String virtualDirPath = "/armnext/demo_kavkaz";

        return baseUrl + virtualDirPath;
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        Log.d(Names.TAG, "Receive");
        context.startService(HttpService.getIntent(context, HttpService.AUTO));

        Timer timer = new Timer();
        AvailableTimerTask availableTimerTask = new AvailableTimerTask(this);
        int TIMEOUT = 1000;
        timer.schedule(availableTimerTask, 1000, TIMEOUT);
        ((App)mContext.getApplicationContext()).onAddLog(new LogItem("пул запущен, период проверки " + (TIMEOUT / 1000) + " сек.", false));

        mDaoSession = new DaoMaster(new DbOpenHelper(mContext, "local-db.db").getWritableDb()).newSession();
    }

    @Override
    public void onAvailable(boolean available) {
        // тут нужно автоматически перезапускать сервис
        ((App)mContext.getApplicationContext()).onAvailable(available);
        if(!available) {
            boolean serviceAvailable = ServiceUtil.checkServiceRunning(mContext, HttpService.SERVICE_NAME);
            ((App) mContext.getApplicationContext()).onAddLog(new LogItem("хост не доступен, служба " + (serviceAvailable ? "запущена" : "остановлена"), true));
            mContext.startService(HttpService.getIntent(mContext, HttpService.AUTO));
        }
    }
}
