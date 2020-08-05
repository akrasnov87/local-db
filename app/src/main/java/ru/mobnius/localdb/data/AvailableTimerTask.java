package ru.mobnius.localdb.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

import ru.mobnius.localdb.Logger;

public class AvailableTimerTask extends TimerTask {

    private final OnAvailableListener mListener;

    public AvailableTimerTask(OnAvailableListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        boolean available = false;
        HttpURLConnection urlConnection = null;
        for (int tryOnceMore = 0; tryOnceMore < 3; tryOnceMore++) {
            try {
                URL url = new URL("http://localhost:" + HttpServerThread.HTTP_SERVER_PORT);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(3000);

                new BufferedInputStream(urlConnection.getInputStream());
                available = true;
                break;
            } catch (IOException e) {
                Logger.error(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
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