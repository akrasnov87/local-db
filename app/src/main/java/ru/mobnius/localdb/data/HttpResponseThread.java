package ru.mobnius.localdb.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ru.mobnius.localdb.model.LogItem;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public class HttpResponseThread extends Thread {
    private final Socket mSocket;
    private OnLogListener mLogListener;
    private OnResponseListener mResponseListener;

    public HttpResponseThread(Context context, Socket socket) {
        mSocket = socket;
        if(context instanceof OnLogListener) {
            mLogListener = (OnLogListener)context;
        }

        if(context instanceof OnResponseListener) {
            mResponseListener = (OnResponseListener)context;
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader is = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            String request = is.readLine();
            PrintWriter os = new PrintWriter(mSocket.getOutputStream(), true);

            int status;

            if(mResponseListener != null && request != null) {
                Response response = mResponseListener.onResponse(new UrlReader(request));
                if(response != null) {
                    status = response.getStatus();
                    os.print(response.toResponseString());
                }else {
                    String txt = "Результат неизвестен";
                    os.print("HTTP/1.1 " + Response.RESULT_FAIL + "\r\n");
                    os.print("Content-Type: " + Response.TEXT_PLAIN + "; charset=utf-8\r\n");
                    os.print("Content-Length: " + txt.getBytes().length + "\r\n");
                    os.print("\r\n");
                    os.print(txt + "\r\n");

                    status = Response.RESULT_FAIL;
                }
            } else {
                String txt = "Обработчик не определен";
                os.print("HTTP/1.1 " + Response.RESULT_FAIL + "\r\n");
                os.print("Content-Type: " + Response.TEXT_PLAIN + "; charset=utf-8\r\n");
                os.print("Content-Length: " + txt.getBytes().length+ "\r\n");
                os.print("\r\n");
                os.print(txt + "\r\n");

                status = Response.RESULT_FAIL;
            }
            os.flush();
            mSocket.close();

            if(mLogListener != null && request != null && new UrlReader(request).getSegments().length > 0) {
                mLogListener.onAddLog(new LogItem("request: " + request + " - " + status, false));
            }

        } catch (IOException e) {
            if(mLogListener != null) {
                e.printStackTrace();
                mLogListener.onAddLog(new LogItem(e.getMessage(), true));
            }
        }
    }
}
