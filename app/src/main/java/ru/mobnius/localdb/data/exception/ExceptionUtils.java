package ru.mobnius.localdb.data.exception;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.storage.ClientErrors;
import ru.mobnius.localdb.storage.DaoSession;
import ru.mobnius.localdb.utils.DateUtil;

public class ExceptionUtils {
    /**
     * Преобразование кода в строку
     * @param code числовой код
     * @return выходной код
     */
    public static String codeToString(int code) {
        String number = String.valueOf(code);
        StringBuilder fill = new StringBuilder();
        for(int i = number.length(); i < 3; i++){
            fill.append("0");
        }
        return String.format("%s%s", fill.toString(), number);
    }

    /**
     * Преобразование строки в модель
     * @param json строка в json формате
     * @return Объект для хранения ошибки
     */
    public static ExceptionModel toModel(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            Date dt = DateUtil.convertStringToDate(jsonObject.getString("id"));
            String message = jsonObject.getString("message");
            int code = jsonObject.getInt("code");
            String group = jsonObject.getString("group");
            return ExceptionModel.getInstance(dt, message, group, code);
        } catch (JSONException e) {
            Logger.error("Ошибка преобразования строки в JSONObject для исключения.", e);
            return null;
        } catch (ParseException e) {
            Logger.error("Ошибка преобразования даты для исключения.", e);
        }
        return null;
    }

    /**
     * Сохранение локально созданных ошибок в БД Sqlite
     * @param context Контекст
     * @param daoSession подключение к БД
     */
    public static void saveLocalException(Context context, DaoSession daoSession) {
        try {
            OnFileExceptionManagerListener fileExceptionManager = FileExceptionManager.getInstance(context);
            File directory = fileExceptionManager.getRootCatalog();
            String[] files = directory.list();
            if (files != null) {
                List<ClientErrors> list = new ArrayList<>(files.length);

                for (String fileName : files) {
                    ExceptionModel model = toModel(new String(fileExceptionManager.readPath(fileName)));
                    if (model!=null) {
                        list.add(model.toDbItem(context));
                    }
                }
                if (list.size() > 0) {
                    daoSession.getClientErrorsDao().insertInTx(list);
                }
                fileExceptionManager.deleteFolder();
            }

        }catch (Exception exc){
            Logger.error("Ошибка сохранения локальных ошибок в БД SqLite", exc);
        }
    }

    /**
     * Запись ошибки в Sqlite
     * @param context контекст
     * @param daoSession соединение
     * @param exc ошибка
     * @param group группа
     * @param code код ошибки
     */
    public static void saveException(Context context, DaoSession daoSession, Exception exc, String group, int code) {
        ExceptionModel model = ExceptionModel.getInstance(new Date(), exc.toString(), group, code);
        daoSession.getClientErrorsDao().insert(model.toDbItem(context));
    }
}
