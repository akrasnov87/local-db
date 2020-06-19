package ru.mobnius.localdb.utils;

import java.util.ArrayList;
import java.util.List;

import ru.mobnius.localdb.model.KeyValue;

/**
 * Разбор запроса
 */
public class UrlReader {
    private final String mRequest;
    //GET /?idx=0 HTTP/1.1
    public UrlReader(String request) {
        mRequest = request;
    }

    /**
     * Части запроса
     */
    public String[] getParts() {
        return mRequest.split(" ");
    }

    /**
     * метод запроса
     * @return метод. Обычно GET
     */
    public String getMethod() {
        return getParts()[0];
    }

    /**
     * Сегменты запроса
     * @return сегменты
     */
    public String[] getSegments() {
        String[] segments = getParts()[1].split("/");
        List<String> results = new ArrayList<>();
        for (String segment:
             segments) {
            if(segment.length() > 0) {
                results.add(segment);
            }
        }

        return results.toArray(new String[0]);
    }

    /**
     * Получение параметров
     * @return парметры
     */
    public KeyValue[] getParams() {
        String[] query = getParts()[1].split("\\?");
        if(query.length > 0) {
            String param = query[1];
            if(param.length() > 0) {
                String[] keyValues = param.split("&");
                if(keyValues.length > 0) {
                    List<KeyValue> values = new ArrayList<>();
                    for (String kv:
                         keyValues) {
                        if(kv.length() > 0) {
                            String[] data = kv.split("=");
                            KeyValue keyValue = new KeyValue();
                            keyValue.key = data[0];
                            try {
                                keyValue.value = data[1];
                            }catch (IndexOutOfBoundsException ignored) {

                            }

                            values.add(keyValue);
                        }
                    }
                    return values.toArray(new KeyValue[0]);
                }
            }
        }
        return null;
    }

    /**
     * Получение значение параметра key
     * @param key ключ
     * @return значение ключа
     */
    public String getParam(String key) {
        KeyValue[] keyValues = getParams();
        for (KeyValue keyValue:
             keyValues) {
            if(keyValue.key.equals(key)) {
                return keyValue.value;
            }
        }
        return null;
    }
}
