package ru.mobnius.localdb;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Timer;

import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.SendErrorTimerTask;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.utils.ServiceUtil;

public class AutoRunReceiver extends BroadcastReceiver
        implements AvailableTimerTask.OnAvailableListener {
    private Context mContext;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Logger.setContext(context);

        Log.d(Names.TAG, "Receive");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(HttpService.getIntent(context, HttpService.AUTO));
        } else {
            context.startService(HttpService.getIntent(context, HttpService.AUTO));
        }

        Timer timer = new Timer();
        AvailableTimerTask availableTimerTask = new AvailableTimerTask(this);
        int TIMEOUT = 10000;
        timer.schedule(availableTimerTask, 1000, TIMEOUT);

        // отправка ошибок
        Timer timerSend = new Timer();
        if (isMyServiceRunning(HttpService.class)) {
            SendErrorTimerTask sendErrorTimerTask = new SendErrorTimerTask();
            timerSend.schedule(sendErrorTimerTask, 1000, 60 * 1000);
        }

        ((App) mContext).onAddLog(new LogItem("пул запущен, период проверки " + (TIMEOUT / 1000) + " сек.", false));
    }

    @Override
    public void onAvailable(boolean available) {
        // тут нужно автоматически перезапускать сервис
        ((App) mContext).onAvailable(available);
        if (!available) {
            boolean serviceAvailable = ServiceUtil.checkServiceRunning(mContext, HttpService.SERVICE_NAME);
            ((App) mContext).onAddLog(new LogItem("хост не доступен, служба " + (serviceAvailable ? "запущена" : "остановлена"), true));
            mContext.startService(HttpService.getIntent(mContext, HttpService.AUTO));
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
