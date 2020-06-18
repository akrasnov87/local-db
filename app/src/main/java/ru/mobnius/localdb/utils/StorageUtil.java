package ru.mobnius.localdb.utils;

import android.content.Context;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import ru.mobnius.localdb.data.Storage;
import ru.mobnius.localdb.model.StorageName;

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
                                Class<?>[] params = null;
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
}
