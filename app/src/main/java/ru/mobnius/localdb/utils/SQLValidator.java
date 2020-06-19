package ru.mobnius.localdb.utils;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;

import java.util.regex.Pattern;

import ru.mobnius.localdb.AutoRunReceiver;

public class SQLValidator {
    public final static String NO_TABLE = "Ошибка: нет такой таблицы";
    public final static String NO_COLUMN = "Не выбрано ни одной колонки";

    public static String isTableAndColumnExist(String query) {
        AbstractDao<?, ?>[] abstractDao = AutoRunReceiver.getDaoSession().getAllDaos().toArray(new AbstractDao[0]);
        boolean has = false;
        boolean hasColumn = false;
        String[] check = query.split("from");
        if (check[0].contains("*")) {
            hasColumn = true;
        }
        for (AbstractDao<?, ?> dao : abstractDao) {
            if (Pattern.compile(Pattern.quote(dao.getTablename()), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                has = true;
                Property[] properties = dao.getProperties();
                for (Property property : properties) {
                    if (Pattern.compile(Pattern.quote(property.name), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                        String regex = "(?i)" + property.name;
                        query = query.replaceAll(regex, property.columnName);
                    }
                    if (Pattern.compile(Pattern.quote(property.columnName), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                        hasColumn = true;
                    }
                }
            }
        }
        if (!has) {
            return NO_TABLE;
        }
        if (!hasColumn) {
            return NO_COLUMN;
        }
        return query;
    }
}
