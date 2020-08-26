package ru.mobnius.localdb.data;


import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.mobnius.localdb.HttpService;

/**
 * Класс для обработки JSONObject и создания из него SQL запроса на добавление записи
 */
public class SqlInsertFromJSONObject {
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
            if (isColumnExists(abstractDao, name.toLowerCase())) {
                builder.append("?,");
                tempFields.add(name);
            }
        }
        fields = tempFields.toArray(new String[0]);
        params = builder.substring(0, builder.length() - 1);
    }

    /**
     * запрос в БД для вставки
     *
     * @param inserts количество
     * @return возвращается запрос
     */
    public void insertToDataBase(int inserts, int columnCount, List<Object> values ) {
        StringBuilder builder = new StringBuilder();
        for (String field : fields) {
            builder.append(field).append(",");
        }
        Database database = HttpService.getDaoSession().getDatabase();
        String s = "INSERT INTO " + tableName + "(" + builder.substring(0, builder.length() - 1) + ")" + " VALUES " + "(" + params + ")" + ";";
        DatabaseStatement statement = database.compileStatement(s);
        insert(statement, columnCount, inserts, values);

      //  StringBuilder paramsBuilder = new StringBuilder();
      //  for (int i = 0; i < inserts; i++) {
       //     paramsBuilder.append("(").append(params).append("),");
       // }

       // return "INSERT INTO " + tableName + "(" + builder.substring(0, builder.length() - 1) + ")" + " VALUES " + paramsBuilder.substring(0, paramsBuilder.length() - 1) + ";";
    }

    /**
     * Получение объекта для передачи в запрос
     *
     * @param object объект для обработки
     * @return Массив значений полей
     * @throws JSONException исключение
     */
    public List<Object> getValues(JSONObject object) throws JSONException {
        List<Object> values = new ArrayList<>(fields.length);
        for (String field : fields) {
            if (field.contains("F_Registr_Pts")) {
                JSONObject obj = object.getJSONObject("F_Registr_Pts");
                String x = obj.getString("LINK");
                values.add(x);
            } else {
                values.add(object.has(field) ? object.get(field) : null);
            }
        }
        return values;
    }

    /**
     * колонка доступна или нет
     *
     * @param columnName имя колонки
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

    private void insert (DatabaseStatement statement, int columnCount, int insertCount, List<Object> values){
        int i = 0;
        switch (columnCount){
            case 6:
                while (insertCount>0){
                    statement.clearBindings();
                    statement.bindString(1, String.valueOf(values.get(i++)));
                    statement.bindString(2, String.valueOf(values.get(i++)));
                    statement.bindString(3, String.valueOf(values.get(i++)));
                    statement.bindString(4, String.valueOf(values.get(i++)));
                    statement.bindString(5, String.valueOf(values.get(i++)));
                    statement.bindString(6, String.valueOf(values.get(i++)));
                    statement.executeInsert();
                    insertCount--;
                }
            break;
            case 4:
                while (insertCount>0){
                    statement.clearBindings();
                    statement.bindString(1, String.valueOf(values.get(i++)));
                    statement.bindString(2, String.valueOf(values.get(i++)));
                    statement.bindString(3, String.valueOf(values.get(i++)));
                    statement.bindString(4, String.valueOf(values.get(i++)));
                    statement.executeInsert();
                    insertCount--;
                }
                break;

        }

    }


}
