package ru.mobnius.localdb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.Date;

import ru.mobnius.localdb.data.exception.ExceptionModel;
import ru.mobnius.localdb.data.exception.FileExceptionManager;
import ru.mobnius.localdb.data.exception.ExceptionCode;
import ru.mobnius.localdb.data.exception.ExceptionGroup;
import ru.mobnius.localdb.utils.StringUtil;

/**
 * Запись и хранение логов
 */
public class Logger {
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static void setContext(Context context) {
        sContext = context;
    }

    /**
     * Запись ошибки в лог
     * @param e ошибка
     */
    public static void error(Exception e) {
        error("", e);
    }

    /**
     * Запись ошибки с описанием
     * @param description описание ошибки
     * @param e ошибка
     */
    public static void error(String description, Exception e) {
        String exceptionString = StringUtil.exceptionToString(e) + description;
        Log.d(Names.TAG, exceptionString);
        ExceptionModel exceptionModel = ExceptionModel.getInstance(new Date(), exceptionString, ExceptionGroup.NONE, ExceptionCode.ALL);
        FileExceptionManager.getInstance(sContext).writeBytes(exceptionModel.getFileName(), exceptionModel.toString().getBytes());
    }
}