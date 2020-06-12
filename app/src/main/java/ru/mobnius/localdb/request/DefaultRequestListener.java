package ru.mobnius.localdb.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

/**
 * Обработчик по умолчанию. Привязка происходит в конструкторе HttpService
 */
public class DefaultRequestListener implements OnRequestListener {

    private int mStatus = Response.RESULT_OK;
    private String mContent = "SUCCESS";

    public DefaultRequestListener(int status, String content) {
        mStatus = status;
        mContent = content;
    }

    public DefaultRequestListener() {}

    @Override
    public boolean isValid(String query) {
        // https://javarush.ru/groups/posts/regulyarnye-vyrazheniya-v-java
        Pattern pattern = Pattern.compile("^/$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        return matcher.find();
    }

    @Override
    public Response getResponse(UrlReader urlReader) {
        Response response = new Response(mStatus, urlReader.getParts()[2]);
        response.setContent(mContent);
        return response;
    }
}
