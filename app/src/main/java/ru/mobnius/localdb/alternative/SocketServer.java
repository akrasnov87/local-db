package ru.mobnius.localdb.alternative;


import android.provider.SyncStateContract;

import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer extends Thread {
    private boolean running = false; // флаг для проверки, запущен ли сервер
    private ServerSocket serverSocket; // экземпляр класса ServerSocket

    public SocketServer() {

    }

    private void runServer() {
        running = true;

        try {
            // создаём серверный сокет, он будет прослушивать порт на наличие запросов
            serverSocket = new ServerSocket(8888);

            while (running) {
                // запускаем бесконечный цикл, внутри которого сокет будет слушать соединения и обрабатывать их
                // создаем клиентский сокет, метод accept() создаёт экземпляр Socket при новом подключении
                Socket client = serverSocket.accept();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override public void run() {
        super.run();
        runServer();
    }
}

