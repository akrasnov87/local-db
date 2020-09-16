package ru.mobnius.localdb.observer;

public interface EventListener {
    void update(String eventType, String... args);
}