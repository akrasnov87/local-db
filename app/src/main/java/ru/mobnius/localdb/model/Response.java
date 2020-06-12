package ru.mobnius.localdb.model;

public class Response {

    public final static int RESULT_OK = 200;
    public final static int RESULT_FAIL = 500;
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
                "content-type:" + " " + mContentType + "\r\n; charset=utf-8\r\n" +
                "content-length:" + " " + mContent.length() + "\r\n" +
                "\r\n" +
                mContent;
    }
}
