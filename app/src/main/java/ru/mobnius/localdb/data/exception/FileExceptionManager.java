package ru.mobnius.localdb.data.exception;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import ru.mobnius.localdb.Logger;
import ru.mobnius.localdb.utils.FileUtil;

public class FileExceptionManager
        implements OnExceptionManagerListener, OnFileExceptionManagerListener {
    private final Context mContext;
    /**
     * Папка для хранения исключений
     */
    private static final String EXCEPTION_FOLDER = "exceptions";
    private static OnFileExceptionManagerListener fileExceptionManager = null;

    public static OnFileExceptionManagerListener getInstance(Context context) {
        if(fileExceptionManager == null){
            return fileExceptionManager = new FileExceptionManager(context);
        }else{
            return fileExceptionManager;
        }
    }

    private FileExceptionManager(Context context) {
        mContext = context;
    }

    public File getRootCatalog() {
        return new File(FileUtil.getRoot(mContext, Environment.DIRECTORY_DOCUMENTS), EXCEPTION_FOLDER);
    }

    @Override
    public void writeBytes(String fileName, byte[] bytes) {
        File dir = getRootCatalog();
        if(!dir.exists()){
            if(!dir.mkdirs()) {
                Logger.error(new Exception("Ошибка создания каталога"));
            }
        }

        File file = new File(dir, fileName);

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream (outputStream);
            bos.write(bytes, 0, bytes.length);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            Logger.error("Ошибка записи исключения в файл", e);
        }
    }

    @Override
    public byte[] readPath(String fileName) {
        File dir = getRootCatalog();
        File file = new File(dir, fileName);
        if(file.exists()){
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream (inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int result = bis.read();
                while(result != -1) {
                    buf.write((byte) result);
                    result = bis.read();
                }
                return buf.toByteArray();
            } catch (IOException e) {
                Logger.error("Ошибка чтения исключения из файла", e);
            }
        }

        return null;
    }

    @Override
    public boolean exists(String fileName) {
        File dir = getRootCatalog();
        File file = new File(dir, fileName);
        return file.exists();
    }

    @Override
    public void deleteFile(String fileName) {
        File dir = getRootCatalog();
        if(!dir.exists()) {
            Logger.error(new Exception("Корневая директория " + EXCEPTION_FOLDER + " не найдена."));
            return;
        }
        File file = new File(dir, fileName);
        if(file.exists()) {
            deleteRecursive(file);
        }else{
            Logger.error(new Exception("Файл " + fileName + " в директории " + EXCEPTION_FOLDER + " не найден."));
        }
    }

    @Override
    public void deleteFolder() {
        File dir = getRootCatalog();
        if(dir.exists()){
            deleteRecursive(dir);
        }else {
            Logger.error(new Exception("Директория " + EXCEPTION_FOLDER + " не найдена."));
        }
    }

    /**
     * удаление объекта File
     * @param fileOrDirectory файл или директория
     */
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles()))
                deleteRecursive(child);

        if(!fileOrDirectory.delete()) {
            Logger.error(new Exception("Ошибка удаления каталога"));
        }
    }

    @Override
    public List<ExceptionModel> getExceptionList() {
        String[] files = getRootCatalog().list();
        if(files != null) {
            List<ExceptionModel> list = new ArrayList<>(files.length);
            for (String fileName : files) {
                byte[] bytes = readPath(fileName);
                if (bytes != null) {
                    ExceptionModel model = ExceptionUtils.toModel(new String(bytes));
                    if (model != null) {
                        list.add(model);
                    }
                }
            }
            if(list.size() > 0) {
                return list;
            }else{
                return null;
            }
        }
        return null;
    }

    @Override
    public ExceptionModel getException(String id) {
        List<ExceptionModel> list = getExceptionList();
        if(list == null)
            return null;
        for(ExceptionModel model : list){
            if(model.getId().equals(id)){
                return model;
            }
        }
        return null;
    }

    @Override
    public ExceptionModel getLastException() {
        List<ExceptionModel> list = getExceptionList();
        if(list == null)
            return null;
        if(list.size() > 0) {
            if(list.size() == 1)
                return list.get(0);

            Collections.sort(list, (o1, o2) -> (int)o2.getDate().getTime() - (int)o1.getDate().getTime());
            return list.get(0);
        }
        return null;
    }
}