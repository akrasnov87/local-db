package ru.mobnius.localdb.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import dalvik.system.DexFile;
import ru.mobnius.localdb.data.SqlInsertFromJSONObject;
import ru.mobnius.localdb.data.SqlUpdateFromJSONObject;
import ru.mobnius.localdb.data.Storage;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.storage.DaoSession;

public class StorageUtil {

    /**
     * Получение списка хранилищ для загрузки данных
     * @param context текущий контекст
     * @param packageName имя пакета
     * @return Список хранилищ
     */
    public static StorageName[] getStorage(Context context, String packageName) {
        List<StorageName> storageNames = new ArrayList<>();
        String packageCodePath = context.getPackageCodePath();
        try {
            DexFile df = new DexFile(packageCodePath);
            for (Enumeration<String> item = df.entries(); item.hasMoreElements(); ) {
                String className = item.nextElement();
                try {
                    if (className.contains(packageName)) {
                        Class<?> act = Class.forName(className);
                        for (Annotation annotation :
                                act.getAnnotations()) {
                            if (annotation.annotationType().getName().equals(Storage.class.getName())) {
                                Class<?>[] params = new Class<?>[0];
                                Object[] args = new Object[0];
                                StorageName storageName = new StorageName();
                                storageName.description = (String) annotation.annotationType().getMethod("description", params).invoke(annotation, args);
                                storageName.table = (String) annotation.annotationType().getMethod("table", params).invoke(annotation, args);
                                storageNames.add(storageName);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return storageNames.toArray(new StorageName[0]);
    }

    /**
     * Преобразование результата запроса в JSON
     * @param database БД
     * @param query запрос
     * @return объект JSON
     */
    public static JSONArray getResults(Database database, String query) {
        Cursor cursor = database.rawQuery(query, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for(int i = 0;  i < totalColumn; i++) {
                if(cursor.getColumnName(i) != null) {
                    try
                    {
                        if(cursor.getString(i) != null) {
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put( cursor.getColumnName(i), "");
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }

    @SuppressWarnings("rawtypes")
    public static void processing(DaoSession daoSession, RPCResult result, String tableName, boolean isAppend) {
        Database db = daoSession.getDatabase();
        AbstractDao abstractDao = null;

        for (AbstractDao ad : daoSession.getAllDaos()) {
            if (ad.getTablename().equals(tableName)) {
                abstractDao = ad;
                break;
            }
        }

        if (abstractDao == null) {
            return;
        }

        if(!isAppend) {
            db.execSQL("delete from " + tableName);
            // таким образом очищаем кэш http://greenrobot.org/greendao/documentation/sessions/
            abstractDao.detachAll();
        }

        if (result.result.records.length > 0) {
            db.beginTransaction();
            JSONObject firstObject = result.result.records[0];
            SqlInsertFromJSONObject sqlInsert = new SqlInsertFromJSONObject(firstObject, tableName, abstractDao);
            try {
                for (JSONObject object : result.result.records) {
                    try {
                        db.execSQL(sqlInsert.convertToQuery(), sqlInsert.getValues(object));
                    } catch (SQLiteConstraintException e) {
                        Log.e("SYNC_ERROR", Objects.requireNonNull(e.getMessage()));
                        // тут нужно обновить запись
                        String pkColumnName = "";
                        for (AbstractDao a : daoSession.getAllDaos()) {
                            if (a.getTablename().equals(tableName)) {
                                pkColumnName = a.getPkProperty().columnName;
                                break;
                            }
                        }
                        if (pkColumnName.isEmpty()) {
                            throw new Exception("Колонка для первичного ключа, таблицы " + tableName + " не найден.");
                        } else {
                            // тут обновление будет только у тех записей у которых не было изменений.
                            SqlUpdateFromJSONObject sqlUpdate = new SqlUpdateFromJSONObject(firstObject, tableName, pkColumnName, abstractDao);
                            db.execSQL(sqlUpdate.convertToQuery(), sqlUpdate.getValues(object));
                        }
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            }
            db.endTransaction();
        }
    }

    public static String toSqlField(String column) {
        return column.toUpperCase().replace("_", "__");
    }
}
