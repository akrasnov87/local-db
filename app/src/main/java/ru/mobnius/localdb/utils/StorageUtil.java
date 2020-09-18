package ru.mobnius.localdb.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import dalvik.system.DexFile;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.SqlInsertFromJSONObject;
import ru.mobnius.localdb.data.SqlInsertFromString;
import ru.mobnius.localdb.data.Storage;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.storage.DaoSession;

public class StorageUtil {
    private final static String TAG = "naval";

    /**
     * Получение списка хранилищ для загрузки данных
     *
     * @param context     текущий контекст
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
                    Logger.error(e);
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        return storageNames.toArray(new StorageName[0]);
    }

    /**
     * Преобразование результата запроса в JSON
     *
     * @param database БД
     * @param query    запрос
     * @return объект JSON
     */

    public static JSONArray getResults(Database database, String query) {
        query = query.toLowerCase();
        Cursor cursor = database.rawQuery(query, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            if (isJSONValid(cursor.getString(i))) {
                                try {
                                    JSONObject object = new JSONObject(cursor.getString(i));
                                    rowObject.put(cursor.getColumnName(i), object);
                                } catch (JSONException e) {
                                    Logger.error(e);
                                }
                            } else {
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                            }
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Logger.error(e);
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
    public static void processing(DaoSession daoSession, RPCResult result, String tableName, boolean removeBeforeInsert, OnProgressUpdate listener) throws SQLiteFullException, SQLiteConstraintException {
        Database db = daoSession.getDatabase();
        if (removeBeforeInsert) {
            db.execSQL("delete from " + tableName);
            PreferencesManager.getInstance().setLocalRowCount("0", tableName);
        }
        AbstractDao abstractDao = null;
        int insertions = Integer.parseInt(PreferencesManager.getInstance().getLocalRowCount(tableName));

        for (AbstractDao ad : daoSession.getAllDaos()) {
            if (ad.getTablename().equals(tableName)) {
                abstractDao = ad;
                break;
            }
        }

        if (abstractDao == null) {
            return;
        }
        if (result.result.records.length > 0) {

            JSONObject firstObject = toNormal(result.result.records[0], abstractDao);
            SqlInsertFromJSONObject sqlInsert = new SqlInsertFromJSONObject(firstObject, tableName, abstractDao);

            //Вычисляем максимально возможную вставку за 1 раз. 999 за 1 раз - ограничение SQLite
            int columnsCount = abstractDao.getAllColumns().length;
            int max = 999 / columnsCount;

            int idx = 0;
            List<Object> values = new ArrayList<>(max);
            db.beginTransaction();
            try {
                for (JSONObject o : result.result.records) {
                    values.addAll(sqlInsert.getValues(toNormal(o, abstractDao)));
                    idx++;
                    if (idx >= max) {
                        try {
                            db.execSQL(sqlInsert.convertToSqlQuery(idx), values.toArray(new Object[0]));
                            insertions += idx;
                            listener.onUpdateProgress(insertions);
                        } catch (Exception e) {
                            Logger.error(e);
                        }
                        idx = 0;
                        values.clear();
                    }
                }
                if (idx == 0) {
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                if (idx == 0) {
                    db.endTransaction();
                    PreferencesManager.getInstance().setLocalRowCount(String.valueOf(insertions), tableName);
                } else {
                    try {
                        db.execSQL(
                                sqlInsert.convertToSqlQuery(idx), values.toArray(new Object[0]));
                        db.setTransactionSuccessful();
                        insertions += idx;
                        listener.onUpdateProgress(insertions);
                    } finally {
                        try {
                            db.endTransaction();
                            PreferencesManager.getInstance().setLocalRowCount(String.valueOf(insertions), tableName);
                        } catch (IllegalStateException e) {
                            Logger.error(e);
                        }
                    }
                }

            }
        }
    }

    public static void processings(DaoSession daoSession, String unzipped, String tableName, boolean removeBeforeInsert, String zip) throws SQLiteFullException, SQLiteConstraintException {
        Database db = daoSession.getDatabase();
        if (removeBeforeInsert) {
            db.execSQL("delete from " + tableName);
            PreferencesManager.getInstance().setLocalRowCount("0", tableName);
        }
        AbstractDao abstractDao = null;
        int insertions = Integer.parseInt(PreferencesManager.getInstance().getLocalRowCount(tableName));

        for (AbstractDao ad : daoSession.getAllDaos()) {
            if (ad.getTablename().equals(tableName)) {
                abstractDao = ad;
                break;
            }
        }
        if (abstractDao == null) {
            return;
        }

        SqlInsertFromString sqlInsertFromString = new SqlInsertFromString(unzipped, tableName);

        Object[] allValues = sqlInsertFromString.getValues();
        if (allValues == null) {
            return;
        }
        if (allValues.length > 0) {
            //Вычисляем максимально возможную вставку за 1 раз. 999 за 1 раз - ограничение SQLite
            int columnsCount = abstractDao.getAllColumns().length;
            int max = 999 / columnsCount;
            int next = max * columnsCount;
            int idx = 0;
            int dataLength = allValues.length;
            db.beginTransaction();
            try {
                for (int i = 0; i < dataLength; i += next) {
                    if (i + next<dataLength) {
                        Object[] s = Arrays.copyOfRange(allValues, i, (i + next));
                        try {
                            db.execSQL(sqlInsertFromString.convertToSqlQuery(max), s);
                            insertions += max;
                        } catch (Exception e) {
                            Logger.error(e);
                        }
                    }
                    idx = i;
                }
                if (idx != dataLength) {
                    Object[] s = Arrays.copyOfRange(allValues, idx, dataLength);
                    int last = s.length/columnsCount;

                    try {
                        db.execSQL(sqlInsertFromString.convertToSqlQuery(last), s);
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                }

            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }
    }
        /*
            int idx = 0;
            int x = 0;
            List<Object> values = new ArrayList<>(max);

            try {
                for (Object o : allValues) {
                    values.add(o);
                    x++;
                    if (x == 6) {
                        idx++;
                        x=0;
                    }
                    if (idx == max) {

                        idx = 0;
                        values.clear();
                    }
                }
                if (idx == 0) {
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                if (idx == 0) {
                    db.endTransaction();
                    PreferencesManager.getInstance().setLocalRowCount(String.valueOf(insertions), tableName);
                } else {
                    try {
                        db.execSQL(sqlInsertFromString.convertToSqlQuery(idx), values.toArray(new Object[0]));
                        db.setTransactionSuccessful();
                        insertions += idx;
                    } finally {
                        try {
                            db.endTransaction();
                            PreferencesManager.getInstance().setLocalRowCount(String.valueOf(insertions), tableName);
                        } catch (IllegalStateException e) {
                            Logger.error(e);
                        }
                    }
                }
            }
        }
    }*/

    /**
     * метод для приведения JSON объекта к виду key(название колнки в таблице)->value(либо значение из notNormal либо пустая строка "")
     *
     * @param notNormal   JSON объект полученный от сервера
     * @param abstractDao для получения названий всех колонок которые есть в данной таблице
     * @return нормализованный JSONObject
     */
    @SuppressWarnings("rawtypes")
    private static JSONObject toNormal(JSONObject notNormal, AbstractDao abstractDao) {
        JSONObject object = new JSONObject();
        for (String x : abstractDao.getAllColumns()) {
            try {
                object.put(x, "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Iterator<String> itr = notNormal.keys();

        while (itr.hasNext()) {
            String key = itr.next();
            try {
                object.put(key, notNormal.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    static int closestInteger(int a, int b) {
        int c1 = a - (a % b);
        return c1;
    }

    /**
     * Метод для проверки строки на валидность JSON
     *
     * @param test тестируемая строка
     * @return true если строка валидная
     */
    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public interface OnProgressUpdate {
        void onUpdateProgress(int progress);
    }
}
