package ru.mobnius.localdb.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Observer {
    public static final String STOP_ASYNC_TASK = "stopAsyncTask";
    public static final String STOP_THREAD = "stopThread";
    public static final String ERROR = "errorLocalDB";
    private final ConcurrentMap<String, List<EventListener>> listeners = new ConcurrentHashMap<>();


    public Observer(String... operations) {
        for (String operation : operations) {
            this.listeners.put(operation, new ArrayList<>());
        }
    }

    public void subscribe(String eventType, EventListener listener) {
        List<EventListener> eventListeners = this.listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.add(listener);
        }
    }

    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    public void notify(String eventType, String... args) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                listener.update(eventType, args);
            }
        }
    }
}
