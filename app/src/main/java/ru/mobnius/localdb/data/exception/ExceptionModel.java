package ru.mobnius.localdb.data.exception;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.UUID;

import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.storage.ClientErrors;
import ru.mobnius.localdb.utils.DateUtil;
import ru.mobnius.localdb.utils.VersionUtil;

/**
 * модель ошибки
 */
public class ExceptionModel {

    public static ExceptionModel getInstance(Date date, String message, String group, int code) {
        return new ExceptionModel(date, message, group, code);
    }

    @Expose
    private final String id;

    /**
     * Текст сообщения об ошибке
     */
    @Expose
    private final String message;

    @Expose
    private final int code;

    @Expose
    private final String group;

    /**
     * Дата возникновения ошибки
     */
    private final Date date;

    private ExceptionModel(Date date, String message, String group, int code) {
        this.id = DateUtil.convertDateToString(date);
        this.date = date;
        this.message = message;
        this.group = group;
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    private int getCode() {
        return code;
    }

    private String getGroup() {
        return group;
    }

    /**
     * Имя файла для хранения исключения
     */
    public String getFileName(){
        return String.format("%s.exc", this.id);
    }

    /**
     * Получение кода ошибки
     * @param isDebug включен ли режим отладки
     * @return код ошибки
     */
    public String getExceptionCode(boolean isDebug) {
        return String.format("%s%s%s", getGroup(), ExceptionUtils.codeToString(getCode()), isDebug ? "D" : "E");
    }

    @NonNull
    @Override
    public String toString() {
        Gson json = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
        return json.toJson(this);
    }

    /**
     * Преобразование в запись БД
     * @param context контекст
     * @return Объект
     */
    public ClientErrors toDbItem(Context context) {
        ClientErrors clientError = new ClientErrors();
        boolean isDebug = PreferencesManager.getInstance() != null && PreferencesManager.getInstance().isDebug();
        clientError.code = getExceptionCode(isDebug);
        clientError.message = getMessage();
        clientError.platform = Build.MODEL;
        clientError.date = getId();
        clientError.version = VersionUtil.getVersionName(context);
        clientError.user = PreferencesManager.getInstance().getLogin();
        clientError.id = UUID.randomUUID().toString();

        return clientError;
    }
}