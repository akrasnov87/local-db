package ru.mobnius.localdb.request;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import ru.mobnius.localdb.R;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;
import ru.mobnius.localdb.utils.VersionUtil;

public class VersionRequestListener implements OnRequestListener {

    private App mApp;
    public static final String LOCAL_DB_APK = "localdb.apk";
    public static final String MO_APK = "ms.apk";
    private static final int LOCAL_DB_NOTIFICATION = 45;
    private static final int MO_NOTIFICATION = 46;

    public VersionRequestListener(App app) {
        mApp = app;
    }

    @Override
    public boolean isValid(String query) {
        if (!PreferencesManager.getInstance().isAuthorized()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^/check_updates", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response;
        JSONObject meta = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject object = new JSONObject();
        try {
            meta.put("success", true);
            object.put("meta", meta);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (PreferencesManager.getInstance().getProgress()!=null){
            try {
                data.put("mobiletrackerVersion", "0");
                data.put("mobiletrackerReady", false);
                data.put("localdbVersion", "0");
                data.put("localdbReady", false);
                JSONArray array = new JSONArray();
                array.put(data);
                JSONObject records = new JSONObject();
                records.put("records", array);
                object.put("result", records);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Response.getInstance(urlReader, object.toString());
        }
        String versionMO = urlReader.getParam("version");
        boolean isMONeedUpdate = false;
        String remoteVersionMO = PreferencesManager.getInstance().getRemoteMOVersion();
        if (versionMO != null && versionMO.contains(".") && remoteVersionMO.contains(".")) {
            isMONeedUpdate = VersionUtil.isUpgradeMOVersion(versionMO, remoteVersionMO);
        }
        if (isMONeedUpdate) {
            if (!PreferencesManager.getInstance().isMOReadyToUpdate()) {
                if (!PreferencesManager.getInstance().isDownloadingMO()) {
                    PreferencesManager.getInstance().setIsDownloadingMO(true);
                    getApk(PreferencesManager.getInstance().getNodeUrl() + "/files/versions/ms.apk", MO_APK, mApp);
                }
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
        if (!remoteLocalDBVersion.isEmpty() && VersionUtil.isUpgradeVersion(mApp, remoteLocalDBVersion, true)) {
            if (!PreferencesManager.getInstance().isLocalDBReadyToUpdate()) {
                if (!PreferencesManager.getInstance().isDownloadingLocalDB()) {
                    PreferencesManager.getInstance().setIsDownloadingLocalDB(true);
                    getApk(PreferencesManager.getInstance().getNodeUrl() + "/files/versions/localdb.apk", LOCAL_DB_APK, mApp);
                }
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
        } else {
            try {
                data.put("localdbVersion", "0");
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                    String app = "";
                    int notif = 0;
                    if (apkType.equals(LOCAL_DB_APK)) {
                        PreferencesManager.getInstance().setLocalDBReadyToUpdate(true);
                        PreferencesManager.getInstance().setIsDownloadingLocalDB(false);
                        app = "LocalDB";
                        notif = LOCAL_DB_NOTIFICATION;
                    }
                    if (apkType.equals(MO_APK)) {
                        PreferencesManager.getInstance().setMOReadyToUpdate(true);
                        PreferencesManager.getInstance().setIsDownloadingMO(false);
                        app = "Мобильный обходчик";
                        notif = MO_NOTIFICATION;
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "httpServiceClientLocalDB")
                            .setSmallIcon(R.drawable.ic_update_icon)
                            .setColor(context.getResources().getColor(R.color.colorPrimary))
                            .setContentTitle("ДОСТУПНО ОБНОВЛЕНИЕ")
                            .setContentText("Обновление приложения " + app + " готово к установке. Перейдите в Мобильный обходчик для установки.")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText("Обновление приложения " + app + " готово к установке. Перейдите в Мобильный обходчик для установки."))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(notif, builder.build());
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
