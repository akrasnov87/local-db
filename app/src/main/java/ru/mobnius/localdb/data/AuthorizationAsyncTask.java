package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.utils.Loader;

public class AuthorizationAsyncTask extends AsyncTask<Void, Void, String> {
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private String mLogin;
    private String mPassword;

    public AuthorizationAsyncTask(Context context, String login, String password) {
        mContext = context;
        mLogin = login;
        mPassword = password;
    }

    @Override
    protected String doInBackground(Void... avoids) {
        boolean isAuthorized = Loader.getInstance().auth(mLogin, mPassword);
        if (isAuthorized) {
            return "";
        } else {
            return "Логин или пароль введены неверно";
        }

    }

    @Override
    protected void onPostExecute(String data) {
        super.onPostExecute(data);
        if (data != null && !data.isEmpty()) {
            Intent intent = new Intent(Tags.ERROR_TAG);
            intent.putExtra(Tags.ERROR_TEXT, data);
            intent.putExtra(Tags.ERROR_TYPE, Tags.AUTH_ERROR);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }
}
