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
    public static final String DOWNLOAD_PROGRESS = "MBL_DOWNLOAD_PROGRESS";
    public static final String LOGIN_RESET = "MBL_LOGIN_RESET";
    public static final String NODE_URL = "MBL_NODE_URL";
    public static final String REPO_URL = "MBL_REPO_URL";
    public static final String RPC_URL = "MBL_RPC_URL";
    public static final String SIZE = "MBL_SIZE";
    public static final String GENERATED_ERROR = "MBL_GENERATED_ERROR";
    public static final String CLEAR = "MBL_CLEAR";
    public static final String ERROR_VISIBILITY = "MBL_ERROR_VISIBILITY";
    public static final String BUSY_PORT = "MBL_BUSY_PORT";
    public static final String ALL_TABLES = "MBL_ALL_TABLES";
    private static final String ALL_TABLES_NAMES = "MBL_ALL_TABLES_NAMES";
    private static final String REMOTE_LOCAL_DB_VERSION = "MBL_REMOTE_LOCAL_DB_VERSION";
    private static final String LOCAL_DB_READY_TO_UPDATE= "MBL_LOCAL_DB_READY_TO_UPDATE";
    private static final String REMOTE_MO_VERSION = "MBL_REMOTE_MO_VERSION";
    private static final String MO_READY_TO_UPDATE= "MBL_MO_READY_TO_UPDATE";
    private static final String MO_DOWNLOADING_NOW = "MBL_MO_DOWNLOADING_NOW";
    private static final String LOCAL_DB_DOWNLOADING_NOW = "MBL_LOCAL_DB_DOWNLOADING_NOW";

    private static PreferencesManager preferencesManager;
    private final SharedPreferences sharedPreferences;

    public static PreferencesManager getInstance() {
        return preferencesManager;
    }

    public static void createInstance(Context context, String preferenceName) {
        preferencesManager = new PreferencesManager(context, preferenceName);
    }

    private PreferencesManager(Context context, String preferenceName) {
        sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
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

    public String getRepoUrl() {
        return getSharedPreferences().getString(REPO_URL, null);
        //return "http://demo.it-serv.ru/repo";
    }

    public void setRepoUrl(String repoUrl) {
        getSharedPreferences().edit().putString(REPO_URL, repoUrl).apply();
    }

    public void setRpcUrl(String rpcUrl) {
        getSharedPreferences().edit().putString(RPC_URL, rpcUrl).apply();
    }

    public String getRpcUrl() {
        return getSharedPreferences().getString(RPC_URL, null);
    }


    public boolean isAuthorized() {
        return getLogin() != null && getPassword() != null && getRepoUrl() != null;
    }

    public void setProgress(Progress progress) {
        getSharedPreferences().edit().putString(PROGRESS, progress == null ? null : new Gson().toJson(progress)).apply();
    }

    public void setDownloadProgress(Progress progress) {
        getSharedPreferences().edit().putString(DOWNLOAD_PROGRESS, progress == null ? null : new Gson().toJson(progress)).apply();
    }

    public Progress getDownloadProgress() {
        String value = getSharedPreferences().getString(DOWNLOAD_PROGRESS, null);
        if (value == null) {
            return null;
        }
        return new Gson().fromJson(value, Progress.class);
    }

    public Progress getProgress() {
        String value = getSharedPreferences().getString(PROGRESS, null);
        if (value == null) {
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

    public void setLocalRowCount(String localRowCount, String tableName) {
        getSharedPreferences().edit().putString(tableName, localRowCount).apply();
    }

    public String getLocalRowCount(String tableName) {
        return getSharedPreferences().getString(tableName, "0");
    }

    public void setRemoteRowCount(String remoteRowCount, String tableName) {
        getSharedPreferences().edit().putString(tableName + "remote", remoteRowCount).apply();
    }

    public String getRemoteRowCount(String tableName) {
        return getSharedPreferences().getString(tableName + "remote", "0");
    }

    public void setPortIsBusy(boolean value) {
        getSharedPreferences().edit().putBoolean(BUSY_PORT, value).apply();
    }

    public boolean isPortBusy() {
        return getSharedPreferences().getBoolean(BUSY_PORT, false);
    }

    public void setIsAllTables(boolean value) {
        getSharedPreferences().edit().putBoolean(ALL_TABLES, value).apply();
    }

    public void setAllTablesArray(String[] value) {
        String allTables;
        if (value == null || value.length == 0) {
            allTables = "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : value) {
                sb.append(s).append(",");
            }
            allTables = sb.toString();
        }
        getSharedPreferences().edit().putString(ALL_TABLES_NAMES, allTables).apply();
    }

    public String[] getAllTablesArray() {
        String allTables = getSharedPreferences().getString(ALL_TABLES_NAMES, "");
        if (allTables != null && !allTables.isEmpty()) {
            return allTables.split(",");
        }
        return null;
    }

    public String getRemoteLocalDBVersion() {
        return getSharedPreferences().getString(REMOTE_LOCAL_DB_VERSION, "");
    }
    public void setRemoteLocalDBVersion(String value) {
        getSharedPreferences().edit().putString(REMOTE_LOCAL_DB_VERSION, value).apply();
    }

    public String getRemoteMOVersion() {
        return getSharedPreferences().getString(REMOTE_MO_VERSION, "");
    }
    public void setRemoteMOVersion(String value) {
        getSharedPreferences().edit().putString(REMOTE_MO_VERSION, value).apply();
    }

    public void setLocalDBReadyToUpdate(boolean value) {
        getSharedPreferences().edit().putBoolean(LOCAL_DB_READY_TO_UPDATE, value).apply();
    }

    public boolean isLocalDBReadyToUpdate() {
        return getSharedPreferences().getBoolean(LOCAL_DB_READY_TO_UPDATE, false);
    }

    public void setMOReadyToUpdate(boolean value) {
        getSharedPreferences().edit().putBoolean(MO_READY_TO_UPDATE, value).apply();
    }

    public boolean isMOReadyToUpdate() {
        return getSharedPreferences().getBoolean(MO_READY_TO_UPDATE, false);
    }

    public void setIsDownloadingMO(boolean value) {
        getSharedPreferences().edit().putBoolean(MO_DOWNLOADING_NOW, value).apply();
    }

    public boolean isDownloadingMO() {
        return getSharedPreferences().getBoolean(MO_DOWNLOADING_NOW, false);
    }

    public void setIsDownloadingLocalDB(boolean value) {
        getSharedPreferences().edit().putBoolean(LOCAL_DB_DOWNLOADING_NOW, value).apply();
    }

    public boolean isDownloadingLocalDB() {
        return getSharedPreferences().getBoolean(LOCAL_DB_DOWNLOADING_NOW, false);
    }

}
