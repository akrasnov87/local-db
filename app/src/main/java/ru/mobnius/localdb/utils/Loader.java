package ru.mobnius.localdb.utils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.model.User;
import ru.mobnius.localdb.storage.ClientErrors;
import ru.mobnius.localdb.storage.DaoSession;

public class Loader {
    /**
     * время на проверку подключения к серверу в милисекундах
     */
    public final static int SERVER_CONNECTION_TIMEOUT = 3000;

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

    public boolean auth(String login, String password) {
        boolean authSuccess = false;
        try {
            String urlParams = String.format("UserName=%s&Password=%s", login, password);
            byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            URL url = new URL(PreferencesManager.getInstance().getRpcUrl() + "/auth");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataLength));
                urlConnection.setDoOutput(true);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setUseCaches(false);
                urlConnection.getOutputStream().write(postData);
                int status = urlConnection.getResponseCode();
                if (status/100 == 4 || status/100 == 5){
                    return false;
                }else {
                    authSuccess = true;
                }
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(in).useDelimiter("\\A");
                String responseText = s.hasNext() ? s.next() : "";
                try {
                    Gson gson = new Gson();
                    mUser = gson.fromJson(responseText, User.class);
                } catch (Exception ignored) {

                }
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception ignored) {

        }
        return authSuccess;
    }

    /**
     * запрос к БД
     * @param action имя таблицы
     * @param method метод
     * @param data данные для фильтрации
     * @return результат
     */
    public RPCResult[] rpc(String action, String method, String data) {
        String urlParams = "[{ \"action\": \"" + action + "\", \"method\": \"" + method + "\", \"data\": " + data + ", \"tid\": 0, \"type\": \"rpc\" }]";
        byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(PreferencesManager.getInstance().getRpcUrl() + "/rpc");
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
            int status = urlConnection.getResponseCode();
            if (status/100 == 4 || status/100 == 5){
                return null;
            }
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Scanner s = new Scanner(in).useDelimiter("\\A");
            String serverResult = s.hasNext() ? s.next() : "";
            return RPCResult.createInstance(serverResult);
        } catch (IOException e) {
            Logger.error(e);
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
        URL url = new URL(PreferencesManager.getInstance().getNodeUrl() + "/download/localdb");
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
     * Отправка ошибок на сервер
     */
    public void sendErrors(DaoSession daoSession) {
        if(daoSession.getClientErrorsDao().count() > 0) {
            List<ClientErrors> items = daoSession.getClientErrorsDao().loadAll();
            String urlParams = new Gson().toJson(items);
            byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                if(PreferencesManager.getInstance().getNodeUrl()==null){
                    return;
                }
                url = new URL(PreferencesManager.getInstance().getNodeUrl() + "/local-db-error");
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
                urlConnection.setDoOutput(true);
                urlConnection.setInstanceFollowRedirects( false );
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(SERVER_CONNECTION_TIMEOUT);

                urlConnection.getOutputStream().write(postData);

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(in).useDelimiter("\\A");
                String serverResult = s.hasNext() ? s.next() : "";

                JSONObject jsonObject = new JSONObject(serverResult);
                boolean success = jsonObject.getJSONObject("meta").getBoolean("success");

                if(success) {
                    daoSession.getClientErrorsDao().deleteAll();
                } else {
                    Logger.error(new Exception(jsonObject.getJSONObject("meta").getString("msg")));
                }
            } catch (IOException | JSONException e) {
                Logger.error(e);
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
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
}
