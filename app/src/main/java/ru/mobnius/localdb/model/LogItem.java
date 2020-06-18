package ru.mobnius.localdb.model;

import java.util.Date;

public class LogItem {

    public LogItem(String message, boolean isError) {
        mMessage = message;
        mIsError = isError;
        mDate = new Date();
    }

    private final String mMessage;
    private final boolean mIsError;
    private final Date mDate;

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
