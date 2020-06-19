package ru.mobnius.localdb.data.exception;

import java.io.File;

public interface OnFileExceptionManagerListener {
    /**
     * Получение корневого каталога
     * @return каталог
     */
    File getRootCatalog();

    /**
     * Запись байтов в файловую систему
     * @param fileName имя файла
     * @param bytes массив байтов
     */
    void writeBytes(String fileName, byte[] bytes);

    /**
     * Чтение информации о файле
     * @param fileName имя файла
     * @return возвращается массив байтов
     */
    byte[] readPath(String fileName);

    /**
     * Доступен ли файл
     * @param fileName имя файла
     * @return возвращается доступен ли файл
     */
    boolean exists(String fileName);

    /**
     * удаление файла
     * @param fileName имя файла
     */
    void deleteFile(String fileName);

    /**
     * очистка папки
     */
    void deleteFolder();
}

