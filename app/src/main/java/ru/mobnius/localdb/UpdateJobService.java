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
                String remoteLocalDBVersion = getServerVersion(PreferencesManager.getInstance().getNodeUrl() + "/versions/localdb.apk");
                String remoteMOVersion = getServerVersion(PreferencesManager.getInstance().getNodeUrl() + "/versions/ms.apk");
                if (remoteLocalDBVersion != null) {
                    PreferencesManager.getInstance().setRemoteLocalDBVersion(remoteLocalDBVersion);
                }
                if (remoteMOVersion != null) {
                    PreferencesManager.getInstance().setRemoteMOVersion(remoteMOVersion);
                }
                UpdateJobService.this.jobFinished(jobParameters, false);
            }
        });
        t.start();
    }

    private String getServerVersion(String url) {
        String remoteVersion = null;
        InputStream input = null;
        HttpURLConnection connection = null;
        try {
            URL ldbURL = new URL(url);
            connection = (HttpURLConnection) ldbURL.openConnection();
            connection.connect();
            input = connection.getInputStream();
            InputStream ldbBufferedStream = new BufferedInputStream(input);
            Scanner scanner = new Scanner(ldbBufferedStream).useDelimiter("\\A");
            String info = scanner.hasNext() ? scanner.next() : "";
            if (!info.isEmpty()) {
                JSONObject object = new JSONObject(info);
                JSONObject infoObject = object.getJSONObject("info");
                remoteVersion = infoObject.getString("version");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return remoteVersion;
    }
}

