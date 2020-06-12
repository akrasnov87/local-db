package ru.mobnius.localdb.request;

import ru.mobnius.localdb.model.Response;
import ru.mobnius.localdb.utils.UrlReader;

public interface OnRequestListener {
    /**
     * валидность обработчик
     * @param query запрос
     * @return валиден ли обработчик
     */
    boolean isValid(String query);

    /**
     * Обработка запроса
     * @param urlReader информация о запросе
     * @return ответ
     */
    Response getResponse(UrlReader urlReader);
}
