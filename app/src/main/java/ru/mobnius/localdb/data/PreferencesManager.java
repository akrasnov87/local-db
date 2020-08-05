package ru.mobnius.localdb.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Objects;

import ru.mobnius.localdb.model.Progress;

public class PreferencesManager {

    public static final String NAME = "MBL";

    public static final String DEBUG = "MBL_DEBUG";
    public final static String APP_VERSION = "MBL_APP_VERSION";
    public final static String SERVER_APP_VERSION = "SERVER_APP_VERSION";
    public final static String SQL = "MBL_SQL";
    public static final String LOGIN = "MBL_LOGIN";
    public static final String PASSWORD = "MBL_PASSWORD";
    public static final String PROGRESS = "MBL_PROGRESS";
    public static final String LOGIN_RESET = "MBL_LOGIN_RESET";
    public static final String NODE_URL = "MBL_NODE_URL";
    public static final String RPC_URL = "MBL_RPC_URL";
    public static final String SIZE = "MBL_SIZE";
    public static final String GENERATED_ERROR = "MBL_GENERATED_ERROR";
    public static final String FIAS_COUNT = "MBL_FIAS_COUNT";
    public static final String DEVICE_BILLING_COUNT = "MBL_DEVICE_BILLING_COUNT";
    public static final String NETWORK_ROUTES_COUNT = "MBL_NETWORK_ROUTES_COUNT";
    public static final String REGISTR_PTS_COUNT = "MBL_REGISTR_PTS_COUNT";

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

    public void setNodeUrl(String nodeUrl) {
        getSharedPreferences().edit().putString(NODE_URL, nodeUrl).apply();
    }

    public String getNodeUrl() {
        return getSharedPreferences().getString(NODE_URL, null);
    }

    public void setRpcUrl(String rpcUrl) {
        getSharedPreferences().edit().putString(RPC_URL, rpcUrl).apply();
    }

    public String getRpcUrl() {
        return getSharedPreferences().getString(RPC_URL, null);
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

    public void setSize(int size) {
        getSharedPreferences().edit().putString(SIZE, String.valueOf(size)).apply();
    }

    public int getSize() {
        String value = getSharedPreferences().getString(SIZE, "10000");
        return Integer.parseInt(Objects.requireNonNull(value));
    }

    public void setTableRowCount(String fiasCount, String tableName){
        getSharedPreferences().edit().putString(tableName, fiasCount).apply();
    }

    public String getTableRowCount(String tableName){
        return getSharedPreferences().getString(tableName, "0");
    }


}
