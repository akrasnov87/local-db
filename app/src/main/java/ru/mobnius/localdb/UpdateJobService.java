package ru.mobnius.localdb;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import ru.mobnius.localdb.data.PreferencesManager;

public class UpdateJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("hak", "job started");
        getApkVersion(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void getApkVersion(JobParameters jobParameters) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String remoteLocalDBVersion = null;
                String remoteMOVersion = null;
                InputStream ldbInput = null;
                InputStream moInput = null;
                HttpURLConnection ldbConnection = null;
                HttpURLConnection moConnection = null;

                try {
                    URL ldbURL = new URL("http://demo.it-serv.ru/ARMNext/demo_kavkaz/versions/localdb.apk");
                    ldbConnection = (HttpURLConnection) ldbURL.openConnection();
                    ldbConnection.connect();
                    ldbInput = ldbConnection.getInputStream();
                    InputStream ldbBufferedStream = new BufferedInputStream(ldbInput);
                    Scanner ldbScaner = new Scanner(ldbBufferedStream).useDelimiter("\\A");
                    String ldbVersionInfo = ldbScaner.hasNext() ? ldbScaner.next() : "";
                    if (!ldbVersionInfo.isEmpty()) {
                        JSONObject object = new JSONObject(ldbVersionInfo);
                        JSONObject info = object.getJSONObject("info");
                        remoteLocalDBVersion = info.getString("version");
                    }
                    URL moURL = new URL("http://demo.it-serv.ru/ARMNext/demo_kavkaz/versions/ms.apk");
                    moConnection = (HttpURLConnection) moURL.openConnection();
                    moConnection.connect();
                    moInput = moConnection.getInputStream();
                    InputStream moBufferedStream = new BufferedInputStream(moInput);
                    Scanner moScanner = new Scanner(moBufferedStream).useDelimiter("\\A");
                    String moVersionInfo = moScanner.hasNext() ? moScanner.next() : "";
                    if (!moVersionInfo.isEmpty()) {
                        JSONObject object = new JSONObject(moVersionInfo);
                        JSONObject info = object.getJSONObject("info");
                        remoteMOVersion = info.getString("version");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (ldbInput != null) {
                            ldbInput.close();
                        }
                        if (moInput != null) {
                            moInput.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (ldbConnection != null) {
                        ldbConnection.disconnect();
                    }
                    if (moConnection != null) {
                        moConnection.disconnect();
                    }
                }
                if (remoteLocalDBVersion != null && remoteMOVersion != null) {
                    PreferencesManager.getInstance().setRemoteLocalDBVersion(remoteLocalDBVersion);
                    PreferencesManager.getInstance().setRemoteMOVersion(remoteMOVersion);
                }
                UpdateJobService.this.jobFinished(jobParameters, false);
            }
        });
        t.start();
    }
}

