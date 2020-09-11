package ru.mobnius.localdb.data;

import android.content.Context;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.model.LogItem;

public class HttpServerThread extends Thread {
    private OnLogListener mLogListener;
    private final Context mContext;

    private ServerSocket httpServerSocket;
    public static final int HTTP_SERVER_PORT = 8888;

    public HttpServerThread(Context context) {
        mContext = context;

        if (context instanceof OnLogListener) {
            mLogListener = (OnLogListener) context;
        }
    }

    @Override
    public void run() {
        if (checkPort(HTTP_SERVER_PORT)) {
            Socket socket;
            try {
                httpServerSocket = new ServerSocket(HTTP_SERVER_PORT);
                //noinspection InfiniteLoopStatement
                while (true) {
                    socket = httpServerSocket.accept();
                    HttpResponseThread httpResponseThread = new HttpResponseThread(mContext, socket);
                    httpResponseThread.start();
                }
            } catch (IOException e) {
                if (mLogListener != null) {
                    Logger.error(e);
                    mLogListener.onAddLog(new LogItem(e.getMessage(), true));
                }
            }
        } else {
            PreferencesManager.getInstance().setPortIsBusy(true);
        }
    }

    public void onDestroy() {
        if (httpServerSocket != null) {
            try {
                httpServerSocket.close();
            } catch (IOException e) {
                if (mLogListener != null) {
                    Logger.error(e);
                    mLogListener.onAddLog(new LogItem(e.getMessage(), true));
                }
            }
        }
    }

    public static boolean checkPort(int port) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            Logger.error(e);
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        }

        return false;
    }
}