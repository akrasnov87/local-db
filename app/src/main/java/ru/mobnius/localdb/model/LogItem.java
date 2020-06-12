package ru.mobnius.localdb.model;

import java.util.Date;

public class LogItem {

    public LogItem(String message, boolean isError) {
        mMessage = message;
        mIsError = isError;
        mDate = new Date();
    }

    private String mMessage;
    private boolean mIsError;
    private Date mDate;

    public String getMessage() {
        return mMessage;
    }

    public boolean isError() {
        return mIsError;
    }

    public Date getDate() {
        return mDate;
    }
}
