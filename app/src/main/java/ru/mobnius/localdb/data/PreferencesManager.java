package ru.mobnius.localdb.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import ru.mobnius.localdb.model.Progress;

public class PreferencesManager {

    public static final String NAME = "MBL";

    public static final String DEBUG = "MBL_DEBUG";
    public final static String APP_VERSION = "MBL_APP_VERSION";
    public final static String SERVER_APP_VERSION = "SERVER_APP_VERSION";
    public static final String LOGIN = "MBL_LOGIN";
    public static final String PASSWORD = "MBL_PASSWORD";
    public static final String PROGRESS = "MBL_PROGRESS";
    public static final String LOGIN_RESET = "MBL_LOGIN_RESET";

    private static PreferencesManager preferencesManager;
    private final SharedPreferences sharedPreferences;

    public static PreferencesManager getInstance() {
        return preferencesManager;
    }

    public static void createInstance(Context context, String preferenceName) {
        preferencesManager = new PreferencesManager(context, preferenceName);
    }

    private PreferencesManager(Context context, String preferenceName){
        sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences(){
        return sharedPreferences;
    }

    public boolean isDebug() {
        return getSharedPreferences().getBoolean(DEBUG, false);
    }

    public void setDebug(boolean value) {
        getSharedPreferences().edit().putBoolean(DEBUG, value).apply();
    }

    public void setLogin(String login) {
        getSharedPreferences().edit().putString(LOGIN, login).apply();
    }

    public String getLogin() {
        return getSharedPreferences().getString(LOGIN, null);
    }

    public void setPassword(String password) {
        getSharedPreferences().edit().putString(PASSWORD, password).apply();
    }

    public String getPassword() {
        return getSharedPreferences().getString(PASSWORD, null);
    }

    public boolean isAuthorized() {
        return getLogin() != null && getPassword() != null;
    }

    public void setProgress(Progress progress) {
        getSharedPreferences().edit().putString(PROGRESS, progress == null ? null : new Gson().toJson(progress)).apply();
    }

    public Progress getProgress() {
        String value = getSharedPreferences().getString(PROGRESS, null);
        if(value == null) {
            return null;
        }
        return new Gson().fromJson(value, Progress.class);
    }
}
