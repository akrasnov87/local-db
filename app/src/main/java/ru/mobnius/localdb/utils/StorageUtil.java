package ru.mobnius.localdb.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.os.Looper;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import ru.mobnius.localdb.HttpService;
import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.data.LoadAsyncTask;
import ru.mobnius.localdb.data.PreferencesManager;
import ru.mobnius.localdb.data.SqlInsertFromJSONObject;
import ru.mobnius.localdb.data.Storage;
import ru.mobnius.localdb.model.StorageName;
import ru.mobnius.localdb.model.rpc.RPCResult;
import ru.mobnius.localdb.storage.DaoSession;

public class StorageUtil {

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
                                }catch (JSONException e){
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
    public static void processing(DaoSession daoSession, RPCResult result, String tableName, boolean removeBeforeInsert) throws SQLiteFullException, SQLiteConstraintException {
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
        String [] pkColumns = abstractDao.getPkColumns();

        if (removeBeforeInsert) {
            db.execSQL("delete from " + tableName);
            PreferencesManager.getInstance().setTableRowCount("0", tableName);
        }

        if (result.result.records.length > 0) {
            JSONObject firstObject = result.result.records[0];
            SqlInsertFromJSONObject sqlInsert = new SqlInsertFromJSONObject(firstObject, tableName, abstractDao);

            int idx = 0;
            int max = 100;
            List<Object> values = new ArrayList<>(max);
            try {
                for (JSONObject o : result.result.records) {
                    if (idx == 0) {
                        db.beginTransaction();
                    }
                    values.addAll(sqlInsert.getValues(o));

                    idx++;

                    if (idx >= max) {
                        try {
                            db.execSQL(sqlInsert.convertToQuery(idx, pkColumns), values.toArray(new Object[0]));
                            db.setTransactionSuccessful();
                            int previousRowCount = Integer.parseInt(PreferencesManager.getInstance().getTableRowCount(tableName));
                            PreferencesManager.getInstance().setTableRowCount(String.valueOf(previousRowCount + idx),tableName);
                        } finally {
                            try {
                                db.endTransaction();
                            } catch (IllegalStateException e) {
                                Logger.error(e);
                            }
                        }
                        idx = 0;
                        values.clear();
                    }
                }
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                if (idx > 0) {
                    try {
                        db.execSQL(sqlInsert.convertToQuery(idx, pkColumns), values.toArray(new Object[0]));
                        db.setTransactionSuccessful();
                        int previousRowCount = Integer.parseInt(PreferencesManager.getInstance().getTableRowCount(tableName));
                        PreferencesManager.getInstance().setTableRowCount(String.valueOf(previousRowCount + idx),tableName);
                    } finally {
                        try {
                            db.endTransaction();
                        } catch (IllegalStateException e) {
                            Logger.error(e);
                        }
                    }
                }
            }
        }
    }

    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

}
