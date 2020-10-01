package ru.mobnius.localdb.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Observer {
    public static final String STOP_ASYNC_TASK = "stopAsyncTask";
    public static final String STOP_THREAD = "stopThread";
    public static final String ERROR = "errorLocalDB";
    ConcurrentMap<String, List<EventListener>> listeners = new ConcurrentHashMap<>();


    public Observer(String... operations) {
        for (String operation : operations) {
            this.listeners.put(operation, new ArrayList<>());
        }
    }

    public void subscribe(String eventType, EventListener listener) {
        List<EventListener> users = listeners.get(eventType);
        users.add(listener);
    }

    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> users = listeners.get(eventType);
        users.remove(listener);
    }

    public void notify(String eventType, String... args) {
        List<EventListener> users = listeners.get(eventType);
        for (EventListener listener : users) {
            listener.update(eventType, args);
        }
    }
}
