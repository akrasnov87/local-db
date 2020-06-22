package ru.mobnius.localdb.data.exception;

import java.util.List;

interface OnExceptionManagerListener {
    /**
     * Список исключений возникших в моб. приложении
     * @return список
     */
    List<ExceptionModel> getExceptionList();

    /**
     * Получение исключения
     * @param id идентификатор исключения
     * @return Исключение
     */
    ExceptionModel getException(String id);

    /**
     * Получение последнего исключения
     * @return если нет ошибки, то вернется null
     */
    ExceptionModel getLastException();
}