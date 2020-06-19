package ru.mobnius.localdb.data;

import org.greenrobot.greendao.AbstractDao;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Класс для обработки JSONObject и создания из него SQL запроса на добавление записи
 */
public class SqlInsertFromJSONObject{
    private final String params;
    private final String tableName;
    private final String[] fields;

    /**
     * Конструктор
     *
     * @param object      объект для обработки
     * @param tableName   имя таблицы
     * @param abstractDao внутренняя сущность
     */
    public SqlInsertFromJSONObject(JSONObject object, String tableName, @SuppressWarnings("rawtypes") AbstractDao abstractDao) {
        this.tableName = tableName;

        StringBuilder builder = new StringBuilder();
        ArrayList<String> tempFields = new ArrayList<>();
        Iterator<String> keys = object.keys();

        while (keys.hasNext()) {
            String name = keys.next();
            if (isColumnExists(abstractDao, StorageUtil.toSqlField(name).toLowerCase())) {
                builder.append("?,");
                tempFields.add(name);
            }
        }
        fields = tempFields.toArray(new String[0]);
        params = builder.substring(0, builder.length() - 1);
    }

    /**
     * запрос в БД для вставки
     * @return возвращается запрос
     */
    public String convertToQuery() {
        StringBuilder builder = new StringBuilder();
        for (String field : fields) {
            builder.append(StorageUtil.toSqlField(field)).append(",");
        }

        return "INSERT INTO " + tableName + "("+builder.substring(0, builder.length() - 1) + ")" + " VALUES(" + params + ")";
    }

    /**
     * Получение объекта для передачи в запрос
     * @param object объект для обработки
     * @return Массив значений полей
     * @throws JSONException исключение
     */
    public Object[] getValues(JSONObject object) throws JSONException {
        Object[] values = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {
            values[i] = object.has(fields[i]) ? object.get(fields[i]) : null;
        }
        return values;
    }

    /**
     * колонка доступна или нет
     *
     * @param columnName  имя колонки
     * @return true - колонка доступна в модели
     */
    private boolean isColumnExists(@SuppressWarnings("rawtypes") AbstractDao abstractDao, String columnName) {
        for (String s : abstractDao.getAllColumns()) {
            if (s.toLowerCase().equals(columnName)) {
                return true;
            }
        }

        return false;
    }
}
