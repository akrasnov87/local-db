package ru.mobnius.localdb.model;

/**
 * Информация о пользователе
 */
public class UserInfo {
    /**
     * Имя пользователя
     */
    public String userName;
    /**
     * Роли пользователя
     */
    public String[] claims;
    /**
     * Роли польтзователя
     */
    public String[] roles;
    /**
     * Идентификатор пользователя
     */
    public String userId;
    /**
     * Версия основного приложения
     */
    public String version;
}
