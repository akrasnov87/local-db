package ru.mobnius.localdb.request;

import android.content.Intent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.App;
import ru.mobnius.localdb.model.DefaultResult;
import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.ui.UpdateActivity;
import ru.mobnius.localdb.utils.UrlReader;

public class InvokeUpdateActivityListener extends AuthFilterRequestListener implements OnRequestListener {
    private App mApp;

    public InvokeUpdateActivityListener(App app) {
        mApp = app;
    }

    @Override
    public boolean isValid(String query) {
        Pattern pattern = Pattern.compile("^/invoke", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = super.getResponse(urlReader);
        if (response != null) {
            return response;
        }
        response = Response.getInstance(urlReader, DefaultResult.getSuccessInstance().toJsonString());
        Intent intent = new Intent(mApp, UpdateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mApp.startActivity(intent);
        return response;
    }


}
