package ru.mobnius.localdb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.mobnius.localdb.data.AvailableTimerTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.utils.JobSchedulerUtil;
import ru.mobnius.localdb.utils.ServiceUtil;

public class AutoRunReceiver extends BroadcastReceiver
        implements AvailableTimerTask.OnAvailableListener {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferencesManager.getInstance().isAuthorized()){
            JobSchedulerUtil.scheduleUpdateJob(context);
        }
        mContext = context;
        //Logger.setContext(context);
        Log.d(Names.TAG, "Receive");

        JobSchedulerUtil.scheduleServiceCheckJob(mContext);
        JobSchedulerUtil.scheduleSendErrorsJob(mContext);

        ((App) mContext).onAddLog(new LogItem("пул запущен, период проверки " + "15" + " мин.", false));
    }

    @Override
    public void onAvailable(boolean available) {
        ((App) mContext).onAvailable(available);
        if (!available) {
            boolean serviceAvailable = ServiceUtil.checkServiceRunning(mContext, HttpService.SERVICE_NAME);
            ((App) mContext).onAddLog(new LogItem("хост не доступен, служба " + (serviceAvailable ? "запущена" : "остановлена"), true));
        }

    }

}
