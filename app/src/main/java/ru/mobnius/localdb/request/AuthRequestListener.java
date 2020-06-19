package ru.mobnius.localdb.request;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.rpc.RpcMeta;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * авторизация
 * http://localhost:8888/auth?login=iserv&password=iserv&node=http://demo.it-serv.ru/armnext/demo_kavkaz&rpc=http://demo.it-serv.ru/MobileServiceSevKav
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

        String rpc = urlReader.getParam("rpc");
        String node = urlReader.getParam("node");

        if(login != null && password != null && rpc != null && node != null) {
            PreferencesManager.getInstance().setLogin(login);
            PreferencesManager.getInstance().setPassword(password);
            PreferencesManager.getInstance().setNodeUrl(node);
            PreferencesManager.getInstance().setRpcUrl(rpc);

            response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
        } else {
            response = Response.getErrorInstance(urlReader, "Инфоврмация об авторизации не передана полностью", Response.RESULT_FAIL);
        }
        return response;
    }
}
