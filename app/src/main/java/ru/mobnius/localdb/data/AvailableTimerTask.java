package ru.mobnius.localdb.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

public class AvailableTimerTask extends TimerTask {

    private final OnAvailableListener mListener;

    public AvailableTimerTask(OnAvailableListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        boolean available = false;
        HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://localhost:" + HttpServerThread.HTTP_SERVER_PORT);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(7000);
                int status = urlConnection.getResponseCode();
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

        if(mListener != null) {
            mListener.onAvailable(available);
        }
    }


    public interface OnAvailableListener {
        void onAvailable(boolean available);
    }
}