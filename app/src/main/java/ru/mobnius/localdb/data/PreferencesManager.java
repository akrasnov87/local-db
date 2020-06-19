package ru.mobnius.localdb.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    public static final String NAME = "MBL";

    public static final String DEBUG = "MBL_DEBUG";
    public final static String APP_VERSION = "MBL_APP_VERSION";
    public final static String SERVER_APP_VERSION = "SERVER_APP_VERSION";
    public final static String SQL = "MBL_SQL";

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
}
