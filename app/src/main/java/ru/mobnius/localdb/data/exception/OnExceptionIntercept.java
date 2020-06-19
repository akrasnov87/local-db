package ru.mobnius.localdb.data.exception;

/**
 * Интерфейс перехвата ошибок
 */
@SuppressWarnings("unused")
public interface OnExceptionIntercept {
    /**
     * Обработчик перехвата ошибок
     */
    void onExceptionIntercept();

    /**
     * Группа ошибки из IExceptionGroup
     * @return строка
     */
    String getExceptionGroup();

    /**
     * Числовой код ошибки из IExceptionCode
     * @return строка
     */
    int getExceptionCode();
}

