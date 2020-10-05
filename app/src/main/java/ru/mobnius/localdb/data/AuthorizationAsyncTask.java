package ru.mobnius.localdb.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.Tags;
import ru.mobnius.localdb.observer.Observer;
import ru.mobnius.localdb.utils.Loader;

public class AuthorizationAsyncTask extends AsyncTask<Void, Void, String> {
    @SuppressLint("StaticFieldLeak")
    private App mApp;
    private String mLogin;
    private String mPassword;

    public AuthorizationAsyncTask(App app, String login, String password) {
        mApp = app;
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
            mApp.getObserver().notify(Observer.ERROR, data);
        }
    }
}
