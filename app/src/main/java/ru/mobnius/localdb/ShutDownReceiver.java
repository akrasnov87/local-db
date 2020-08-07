package ru.mobnius.localdb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ShutDownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(Tags.CANCEL_TASK_TAG);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
    }
}
