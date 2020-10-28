package ru.mobnius.localdb;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.mobnius.localdb.data.HttpServerThread;
import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.utils.ServiceUtil;

public class HttpCheckJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.e(Names.TAG, "job started");
        startHttpService(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void startHttpService(JobParameters jobParameters){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean available = false;
                Log.e(Names.TAG, "thread started");
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://localhost:" + HttpServerThread.HTTP_SERVER_PORT);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(7000);
                    int status = urlConnection.getResponseCode();

                    Log.e(Names.TAG, "status:" + status);
                    if (status == 200) {
                        new BufferedInputStream(urlConnection.getInputStream());
                        available = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                Log.e(Names.TAG, "available?");
                if (!available){
                    Log.e(Names.TAG, "not available");
                    boolean serviceAvailable = ServiceUtil.checkServiceRunning(HttpCheckJobService.this, HttpService.SERVICE_NAME);
                    App app  = (App) getApplicationContext();
                    if (app != null) {
                        Log.e(Names.TAG, "app != null");
                        app.onAddLog(new LogItem("хост не доступен, служба " + (serviceAvailable ? "запущена" : "остановлена"), true));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.e(Names.TAG, "at the end");
                            startForegroundService(HttpService.getIntent(HttpCheckJobService.this, HttpService.AUTO));
                        } else {
                            Log.e(Names.TAG, "at the end");
                            startService(HttpService.getIntent(HttpCheckJobService.this, HttpService.AUTO));
                        }
                    }
                }
                HttpCheckJobService.this.jobFinished(jobParameters, false);
            }
        });
        thread.start();
    }



}
