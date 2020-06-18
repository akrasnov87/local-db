package ru.mobnius.localdb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;

import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.utils.ServiceUtil;

public class AutoRunReceiver extends BroadcastReceiver
        implements AvailableTimerTask.OnAvailableListener {

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
        ((App)mContext).onAddLog(new LogItem("пул запущен, период проверки " + (TIMEOUT / 1000) + " сек.", false));
    }

    @Override
    public void onAvailable(boolean available) {
        // тут нужно автоматически перезапускать сервис
        ((App)mContext).onAvailable(available);

        if(!available) {
            boolean serviceAvailable = ServiceUtil.checkServiceRunning(mContext, HttpService.SERVICE_NAME);
            ((App) mContext).onAddLog(new LogItem("хост не доступен, служба " + (serviceAvailable ? "запущена" : "остановлена"), true));
            mContext.startService(HttpService.getIntent(mContext, HttpService.AUTO));
        }
    }
}
