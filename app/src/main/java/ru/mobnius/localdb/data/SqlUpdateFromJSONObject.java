package ru.mobnius.localdb.data;

import org.greenrobot.greendao.AbstractDao;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import ru.mobnius.localdb.utils.StorageUtil;

/**
 * Класс для обработки JSONObject и создания из него SQL запроса на обновление записи
 */
public class SqlUpdateFromJSONObject {
    private final String params;
    private final String tableName;
    private final String[] fields;
    private final String pkColumn;

    /**
     * Конструктор
     * @param object объект для обработки
     * @param tableName имя таблицы
     * @param pkColumn имя первичного ключа
     */
    public SqlUpdateFromJSONObject(JSONObject object, String tableName, String pkColumn, @SuppressWarnings("rawtypes") AbstractDao abstractDao) {
        this.tableName = tableName;
        this.pkColumn = pkColumn;

        StringBuilder builder = new StringBuilder();
        ArrayList<String> tempFields = new ArrayList<>();
        Iterator<String> keys = object.keys();
        String fieldName;
        while (keys.hasNext()){
            fieldName = keys.next();
            if(fieldName.equals(pkColumn)) {
                continue;
            }
            if (isColumnExists(abstractDao, StorageUtil.toSqlField(fieldName).toLowerCase())) {
                tempFields.add(fieldName);
                builder.append(StorageUtil.toSqlField(fieldName)).append("  = ?, ");
            }
        }
        fields = tempFields.toArray(new String[0]);
        params = builder.substring(0, builder.length() - 2);
    }

    /**
     * запрос в БД для обновления
     * @return возвращается запрос
     */
    public String convertToQuery() {
        return "UPDATE " + tableName + " set " + params + " where " + pkColumn + " = ?";
    }

    /**
     * Получение объекта для передачи в запрос
     * @param object объект для обработки
     * @return Массив значений полей
     */
    public Object[] getValues(JSONObject object) throws JSONException {
        ArrayList<Object> values = new ArrayList<>(fields.length);

        Object pk = null;

        for (String field : fields) {
            if (pkColumn.equals(field)) {
                pk = object.get(field);
                continue;
            }
            values.add(object.has(field) ? object.get(field) : null);
        }

        values.add(pk);

        return values.toArray();
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