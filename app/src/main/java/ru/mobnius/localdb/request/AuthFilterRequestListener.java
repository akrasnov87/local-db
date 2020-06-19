package ru.mobnius.localdb.request;

import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public abstract class AuthFilterRequestListener
        implements OnRequestListener{

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = null;
        if(!PreferencesManager.getInstance().isAuthorized()) {
            response = Response.getErrorInstance(urlReader, "Не авторизован", Response.RESULT_NO_AUTH);
        }

        return response;
    }
}
