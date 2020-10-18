package ru.mobnius.localdb.alternative;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import ru.mobnius.localdb.model.User;

public class UserManager extends Thread {
    private UserForSocket user; // экземпляр класса User, хранящий информацию о пользователе
    private Socket socket; // сокет, созданный при подключении пользователя
    private PrintWriter bufferSender;
    private boolean running; // флаг для проверки, запущен ли сокет
    private UserManagerDelegate managerDelegate; // экземпляр интерфейса UserManagerDelegate

    public UserManager(Socket socket, UserManagerDelegate managerDelegate) {
        this.user = new UserForSocket();
        this.socket = socket;
        this.managerDelegate = managerDelegate;
        running = true;
    }

    public UserForSocket getUser() {
        return user;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override public void run() {
        super.run();
        try {
            // отправляем сообщение клиенту
            bufferSender =
                    new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            // читаем сообщение от клиента
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // в бесконечном цикле ждём сообщения от клиента и смотрим, что там
            while (running) {
                String message = null;
                try {
                    message = in.readLine();
                } catch (IOException e) {
                }

                // проверка на команды
                if (hasCommand(message)) {
                    continue;
                }

                if (message != null && managerDelegate != null) {
                    user.setMessage(message); // сохраняем сообщение
                    managerDelegate.messageReceived(user, null); // уведомляем сервер о сообщении
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        running = false;

        if (bufferSender != null) {
            bufferSender.flush();
            bufferSender.close();
            bufferSender = null;
        }

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        socket = null;
    }

    public void sendMessage(String message) {
        if (bufferSender != null && !bufferSender.checkError()) {
            bufferSender.println(message);
            bufferSender.flush();
        }
    }

    public boolean hasCommand(String message) {
        if (message != null) {
            if (message.contains("7777")) {
                close();
                managerDelegate.userDisconnected(this, user.getUsername());
                return true;
            } else if (message.contains("6666")) {
                user.setUsername(message.replaceAll("6666", ""));
                user.setUserID(socket.getPort());
                managerDelegate.userConnected(user);
                return true;
            } else if (message.contains("5555")) {
                return true;
            }
        }

        return false;
    }

    // интерфейс, который передает результаты операций в SocketServer
    public interface UserManagerDelegate {
        void userConnected(UserForSocket connectedUser);

        void userDisconnected(UserManager userManager, String username);

        void messageReceived(UserForSocket fromUser, UserForSocket toUser);
    }
}
