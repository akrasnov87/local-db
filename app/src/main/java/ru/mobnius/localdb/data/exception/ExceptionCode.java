package ru.mobnius.localdb.data.exception;

public interface ExceptionCode {
    /**
     * Ошибка на уровне приложения.
     * Не была перехвачено ни кем
     */
    int ALL = 666;

    /**
     * Общее
     */
    int MAIN = 0;
    int SETTING = 1;
    int HTTP_SERVICE = 2;
    int AUTH = 3;
    int DOWNLOAD_LIST = 4;
    int DOWNLOAD_PROGRESS = 5;
    int SQL_ACTIVITY = 6;
}

