package ru.mobnius.localdb.request;

import com.google.gson.Gson;

import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.AuthResult;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.model.RpcMeta;
import ru.mobnius.localdb.utils.UrlReader;

public abstract class AuthFilterRequestListener
        implements OnRequestListener{

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = null;
        if(!PreferencesManager.getInstance().isAuthorized()) {
            response = new Response(Response.RESULT_NO_AUTH, urlReader.getParts()[2]);
            response.setContentType(Response.APPLICATION_JSON);
            AuthResult authResult = new AuthResult();
            authResult.meta = new RpcMeta();
            authResult.meta.success = false;
            authResult.meta.msg = "Не авторизован";
            response.setContent(new Gson().toJson(authResult));
        }

        return response;
    }
}
