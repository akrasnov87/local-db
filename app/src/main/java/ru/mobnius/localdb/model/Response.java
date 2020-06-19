package ru.mobnius.localdb.model;

import com.google.gson.Gson;

import ru.mobnius.localdb.model.rpc.RpcMeta;
import ru.mobnius.localdb.utils.UrlReader;

public class Response {

    /**
     * Возвращается результат с ошибкой
     * @param urlReader информация о запросе
     * @param errorMessage текст сообщения
     * @param code код ошибки Response.RESULT_FAIL|Response.RESULT_NOT_FOUND
     */
    public static Response getErrorInstance(UrlReader urlReader, String errorMessage, int code) {
        Response response = new Response(code, urlReader.getParts()[2]);
        response.setContentType(Response.APPLICATION_JSON);
        DefaultResult result = new DefaultResult();
        result.meta = new RpcMeta();
        result.meta.success = false;
        result.meta.msg = errorMessage;

        response.setContent(new Gson().toJson(result));
        return response;
    }

    /**
     * Возвращение контента
     * @param urlReader информация о запросе
     * @param content контент
     */
    public static Response getInstance(UrlReader urlReader, String content) {
        Response response = new Response(Response.RESULT_OK, urlReader.getParts()[2]);
        response.setContentType(Response.APPLICATION_JSON);
        response.setContent(content);
        return response;
    }

    public final static int RESULT_OK = 200;
    public final static int RESULT_FAIL = 500;
    public final static int RESULT_NO_AUTH = 401;
    public final static int RESULT_NOT_FOUNT = 404;

    public final static String TEXT_PLAIN = "text/plain";
    public final static String APPLICATION_JSON = "application/json";

    private final int mStatus;
    private final String mHost;
    private String mContentType = TEXT_PLAIN;
    private String mContent = "";

    /**
     *
     * @param status статус ответа. По умолчанию ставить RESULT_OK
     * @param host обычно HTTP/1.1
     */
    public Response(int status, String host) {
        mStatus = status;
        mHost = host;
    }

    /**
     * Устанвока типа контента
     * @param type контент. По умолчанию TEXT_PLAIN
     */
    public void setContentType(String type) {
        mContentType = type;
    }

    /**
     * Содержимое для передачи
     * @param content контент
     */
    public void setContent(String content) {
        mContent = content;
    }

    public int getStatus() {
        return mStatus;
    }

    public String toResponseString() {
        return mHost + " " + mStatus + "\r\n" +
                "Content-Type: " + mContentType + "; charset=utf-8\r\n" +
                "Content-Length: " + mContent.getBytes().length + "\r\n" +
                "\r\n" +
                mContent;
    }
}
