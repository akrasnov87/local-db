package ru.mobnius.localdb.utils;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;

import java.util.regex.Pattern;

import ru.mobnius.localdb.AutoRunReceiver;

public class SQLValidation {
    public final static String NO_TABLE = "Ошибка: нет такой таблицы";
    public final static String NO_COLUMN = "Не выбрано ни одной колонки";

    public static String getRightQuery(String query) {
        AbstractDao<?, ?>[] abstractDao = AutoRunReceiver.getDaoSession().getAllDaos().toArray(new AbstractDao[0]);
        boolean has = false;
        boolean hasColumn = false;
        String[] check = query.split("from");
        if (check[0].contains("*")) {
            hasColumn = true;
        }
        for (int i = 0; i < abstractDao.length; i++) {
            if (Pattern.compile(Pattern.quote(abstractDao[i].getTablename()), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                has = true;
                Property[] properties = abstractDao[i].getProperties();
                for (int j = 0; j < properties.length; j++) {
                    if (Pattern.compile(Pattern.quote(properties[j].name), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                        String regex = "(?i)" + properties[j].name;
                        query = query.replaceAll(regex, properties[j].columnName);
                    }
                    if (Pattern.compile(Pattern.quote(properties[j].columnName), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
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

    public static String primitiveMatcher(String query) {
        String queryIgnoreCase = query.toLowerCase();
        if (queryIgnoreCase.matches("select\\b.*? from\\b.*?where\\b.*")) {
            return queryIgnoreCase;
        }
        if (queryIgnoreCase.matches("select\\b.*? top\\b.*?from\\b.*")) {
            return queryIgnoreCase;
        }
        return "";
    }

}
