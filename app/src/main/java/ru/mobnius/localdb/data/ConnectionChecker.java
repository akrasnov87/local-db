package ru.mobnius.localdb.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.mobnius.localdb.utils.NetworkUtil;

public class ConnectionChecker extends BroadcastReceiver {

    private CheckConnection mCheckConnection;

    public void setListener(CheckConnection checkConnection) {
        this.mCheckConnection = checkConnection;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (mCheckConnection != null ) {
            mCheckConnection.onConnectionChange(NetworkUtil.isNetworkAvailable(context));
        }
    }

    public interface CheckConnection {
        void onConnectionChange(boolean isConnected);
    }
}

