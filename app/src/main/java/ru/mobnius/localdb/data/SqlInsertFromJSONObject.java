package ru.mobnius.localdb.data;

import org.greenrobot.greendao.AbstractDao;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
     * @param count количество
     * @return возвращается запрос
     */
    public String convertToQuery(int count) {
        StringBuilder builder = new StringBuilder();
        for (String field : fields) {
            builder.append(StorageUtil.toSqlField(field)).append(",");
        }

        StringBuilder paramsBuilder = new StringBuilder();
        for(int i = 0; i < count; i++) {
            paramsBuilder.append("(").append(params).append("),");
        }

        return "INSERT INTO " + tableName + "("+builder.substring(0, builder.length() - 1) + ")" + " VALUES " + paramsBuilder.substring(0, paramsBuilder.length() - 1) + ";";
    }

    /**
     * Получение объекта для передачи в запрос
     * @param object объект для обработки
     * @return Массив значений полей
     * @throws JSONException исключение
     */
    public List<Object> getValues(JSONObject object) throws JSONException {
        List<Object> values = new ArrayList<>(fields.length);

        for (String field : fields) {
            values.add(object.has(field) ? object.get(field) : null);
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
