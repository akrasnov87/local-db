package ru.mobnius.localdb.data.tablePack;

public interface OnRunnableLoadListeners {
    /**
     * Буфер заполнен
     * @param count количество записей в буфере
     */
    void onBufferSuccess(int count);

    /**
     * Вставка в буфер
     * @param count количество записей в буфере
     */
    void onBufferInsert(int count);

    /**
     * Буфер пуст
     */
    void onBufferEmpty();

    /**
     * Загрузка завершена
     */
    void onLoaded();

    /**
     * Ошибка
     * @param message just error message
     */
    void onError(String message);

    /**
     * Прогресс
     * @param start старт
     * @param total всего
     */
    void onProgress(int start, int total);
}
