package ru.mobnius.localdb.request;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.Names;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;
import ru.mobnius.localdb.utils.VersionUtil;

public class VersionRequestListener  implements OnRequestListener {

    private App mApp;
    public static final String LOCAL_DB_APK = "localdb.apk";
    public static final String MO_APK = "client.apk";

    public VersionRequestListener(App app) {
        mApp = app;
    }

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/check_updates", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response;
        String versionMO = urlReader.getParam("version");
        boolean isMONeedUpdate = false;
        String remoteVersionMO = PreferencesManager.getInstance().getRemoteMOVersion();
        if (versionMO!=null&&versionMO.contains(".") && remoteVersionMO.contains(".")) {
            String[] versionNumbers = versionMO.split("\\.");
            String[] remoteVersionNumbers = remoteVersionMO.split("\\.");
            if (versionNumbers.length == remoteVersionNumbers.length) {
                for (int i = 0; i < versionNumbers.length; i++) {
                    try {
                        int local = Integer.parseInt(versionNumbers[i]);
                        int remote = Integer.parseInt(remoteVersionNumbers[i]);
                        if (remote > local) {
                            isMONeedUpdate = true;
                            break;
                        }
                        if(remote<local){
                            break;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        JSONObject meta = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject object = new JSONObject();

        try {
            meta.put("success", true);
            object.put("meta", meta);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isMONeedUpdate) {
            if (!PreferencesManager.getInstance().isMOReadyToUpdate()) {
                getApk(Names.UPDATE_MO_URL, MO_APK, mApp);
                try {
                    data.put("mobiletrackerVersion", remoteVersionMO);
                    data.put("mobiletrackerReady", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    data.put("mobiletrackerVersion", remoteVersionMO);
                    data.put("mobiletrackerReady", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                data.put("mobiletrackerVersion", "0");
                data.put("mobiletrackerReady", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String remoteLocalDBVersion = PreferencesManager.getInstance().getRemoteLocalDBVersion();
        if (!remoteLocalDBVersion.isEmpty()) {
            if (VersionUtil.isUpgradeVersion(mApp, remoteLocalDBVersion, true)) {
                if (!PreferencesManager.getInstance().isLocalDBReadyToUpdate()) {
                    getApk(Names.UPDATE_LOCALDB_URL, LOCAL_DB_APK, mApp);
                    try {
                        data.put("localdbVersion", remoteLocalDBVersion);
                        data.put("localdbReady", false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        data.put("localdbVersion", remoteLocalDBVersion);
                        data.put("localdbReady", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            try {
                data.put("localdbVersion","0");
                data.put("localdbReady", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            JSONArray array = new JSONArray();
            array.put(data);
            JSONObject records = new JSONObject();
            records.put("records", array);
            object.put("result", records);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response = Response.getInstance(urlReader, object.toString());
        return response;
    }

    private void getApk(String url, String apkType, Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        Thread thread = new Thread(() -> {
            File folder = new File(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)).toString());

            File file = new File(folder.getAbsolutePath(), apkType);
            if (file.exists()) {
                file.delete();
            }
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL sUrl = new URL(url);
                connection = (HttpURLConnection) sUrl.openConnection();
                connection.connect();

                input = connection.getInputStream();
                output = new FileOutputStream(file);

                byte[] data = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                handler.post(() -> {
                    if (apkType.equals(LOCAL_DB_APK)) {
                        PreferencesManager.getInstance().setLocalDBReadyToUpdate(true);
                    }
                    if (apkType.equals(MO_APK)) {
                        PreferencesManager.getInstance().setMOReadyToUpdate(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }

        });
        thread.start();
    }
}
