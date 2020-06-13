package ru.mobnius.localdb.utils;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import ru.mobnius.localdb.AutoRunReceiver;
import ru.mobnius.localdb.model.User;

public class Loader {
    /**
     * время на проверку подключения к серверу в милисекундах
     */
    private final static int SERVER_CONNECTION_TIMEOUT = 3000;

    private static Loader sLoader;

    public static Loader getInstance() {
        if(sLoader == null) {
            sLoader = new Loader();
        }
        return sLoader;
    }

    private User mUser;

    private Loader() {

    }

    public void auth(String login, String password) {
        try {
            String urlParams = String.format("UserName=%s&Password=%s", login, password);
            byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            URL url = new URL(AutoRunReceiver.getRpcUrl() + "/auth");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataLength));
                urlConnection.setDoOutput(true);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setUseCaches(false);
                urlConnection.getOutputStream().write(postData);

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(in).useDelimiter("\\A");
                String responseText = s.hasNext() ? s.next() : "";
                try {
                    Gson gson = new Gson();
                    mUser = gson.fromJson(responseText, User.class);
                } catch (Exception ignored) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * запрос к БД
     * @param action имя таблицы
     * @param method метод
     * @param data данные для фильтрации
     * @param classOfT тип возвращаемого результата
     * @param <T> тип
     * @return результат
     */
    public <T> T rpc(String action, String method, String data, Class<T> classOfT) {
        String urlParams = "[{ \"action\": \"" + action + "\", \"method\": \"" + method + "\", \"data\": " + data + ", \"tid\": 0, \"type\": \"rpc\" }]";
        byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(AutoRunReceiver.getRpcUrl() + "/rpc");
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            urlConnection.setRequestProperty("Accept","application/json");

            urlConnection.setRequestProperty("Authorization", "Token " + getUser().token);

            urlConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects( false );
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(SERVER_CONNECTION_TIMEOUT);

            urlConnection.getOutputStream().write(postData);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Scanner s = new Scanner(in).useDelimiter("\\A");
            String serverResult = s.hasNext() ? s.next() : "";
            Gson gson = new Gson();
            return gson.fromJson(serverResult, classOfT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    /**
     * Проверка на доступность подключения к серверу приложения
     *
     * @return возвращается строка если возникла ошибка, либо объект ServerExists
     * @throws IOException общая ошибка
     */
    public String version() throws IOException {
        URL url = new URL(AutoRunReceiver.getNodeUrl() + "/download/localdb");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {

            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(SERVER_CONNECTION_TIMEOUT);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Scanner s = new Scanner(in).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (Exception ignored) {

        } finally {
            urlConnection.disconnect();
        }

        return "0.0.0.0";
    }

    /**
     * Получение информации о пользователе
     * @return информация от сервера
     */
    public User getUser() {
        return mUser;
    }

    public boolean isAuthorized() {
        return getUser() != null;
    }

    public void destroy() {
        sLoader = null;
    }
}
