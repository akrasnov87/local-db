package ru.mobnius.localdb.data.tablePack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class SqlStatementInsert {
    private String mParams;
    private String mTableName;
    private DatabaseStatement mStatement;
    private Table mTable;

    public SqlStatementInsert(Table table, String tableName, AbstractDao abstractDao) {
        mTableName = tableName;
        mTable = table;

        StringBuilder builder = new StringBuilder();
        for (String name : table.getHeaders()) {
            if (isColumnExists(abstractDao, name.toLowerCase())) {
                builder.append("?,");
            }
        }
        mParams = builder.substring(0, builder.length() - 1);
        mStatement = abstractDao.getDatabase().compileStatement(convertToQuery());
    }

    /**
     * Получение объекта для передачи в запрос
     */
    public void bind(int idx) {
        mStatement.clearBindings();

        for(int i = 0; i < mTable.getHeaders().length; i++) {
            bindObjectToStatement(mStatement, i + 1, mTable.getValue(idx)[i]);
        }
        mStatement.execute();
    }

    /**
     * запрос в БД для вставки
     * @return возвращается запрос
     */
    private String convertToQuery() {
        return "INSERT INTO " + mTableName + "(" + mTable.getHeadersLineForSql() + ")" + " VALUES(" + mParams + ")";
    }

    /**
     * колонка доступна или нет
     *
     * @param abstractDao
     * @param columnName  имя колонки
     * @return true - колонка доступна в модели
     */
    private boolean isColumnExists(AbstractDao abstractDao, String columnName) {
        for (String s : abstractDao.getAllColumns()) {
            if (s.toLowerCase().equals(columnName)) {
                return true;
            }
        }

        return false;
    }

    private void bindObjectToStatement(DatabaseStatement statement, int index, Object value) {
        statement.bindString(index, String.valueOf(value));
    }
}
