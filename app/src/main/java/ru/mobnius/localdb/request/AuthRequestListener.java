package ru.mobnius.localdb.request;

import android.content.ComponentName;
import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.data.AuthorizationAsyncTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.Loader;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * авторизация
 * http://localhost:8888/auth?login=iserv&password=iserv&node=http://demo.it-serv.ru/armnext/demo_kavkaz&rpc=http://demo.it-serv.ru/MobileServiceSevKav&repo=http://demo.it-serv.ru/repo
 */
public class AuthRequestListener
        implements OnRequestListener {
    private Context mContext;

    public AuthRequestListener(Context context) {
        mContext = context;
    }

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

        String rpc = urlReader.getParam("rpc");
        String node = urlReader.getParam("node");

        String repo = urlReader.getParam("repo");

        if(login != null && password != null && rpc != null && node != null && repo != null) {
            new AuthorizationAsyncTask(mContext, login, password).execute();
            PreferencesManager.getInstance().setLogin(login);
            PreferencesManager.getInstance().setPassword(password);
            PreferencesManager.getInstance().setNodeUrl(node);
            PreferencesManager.getInstance().setRepoUrl(repo);
            PreferencesManager.getInstance().setRpcUrl(rpc);
            response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
        } else {
            response = Response.getErrorInstance(urlReader, "Информация об авторизации не передана полностью", Response.RESULT_FAIL);
        }
        return response;
    }
}
