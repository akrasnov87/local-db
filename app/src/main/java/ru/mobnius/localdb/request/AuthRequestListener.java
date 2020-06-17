package ru.mobnius.localdb.request;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.AuthResult;
import ru.mobnius.localdb.model.Progress;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.RpcMeta;
import ru.mobnius.localdb.model.progress.ProgressRecords;
import ru.mobnius.localdb.model.progress.ProgressResult;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * авторизация
 */
public class AuthRequestListener
        implements OnRequestListener {

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/auth", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response;

        String login = urlReader.getParam("login");
        String password = urlReader.getParam("password");

        if(login != null && password != null) {
            PreferencesManager.getInstance().setLogin(login);
            PreferencesManager.getInstance().setPassword(password);

            response = new Response(Response.RESULT_OK, urlReader.getParts()[2]);
            response.setContentType(Response.APPLICATION_JSON);

            AuthResult result = new AuthResult();
            result.meta = new RpcMeta();
            result.meta.success = true;

            response.setContent(new Gson().toJson(result));
        } else {
            response = new Response(Response.RESULT_FAIL, urlReader.getParts()[2]);
            response.setContentType(Response.APPLICATION_JSON);

            AuthResult result = new AuthResult();
            result.meta = new RpcMeta();
            result.meta.success = false;
            result.meta.msg = "Логин или пароль не переданы";

            response.setContent(new Gson().toJson(result));
        }
        return response;
    }
}
