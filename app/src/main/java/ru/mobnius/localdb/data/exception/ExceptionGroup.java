package ru.mobnius.localdb.data.exception;

/**
 * Группы ошибок
 */
public interface ExceptionGroup {
    /**
     * Интерфейс
     */
    String USER_INTERFACE = "UI";
    /**
     * Настройки
     */
    String SETTING = "STG";
    /**
     * Фоновые службы
     */
    String SERVICE = "SRV";

    /**
     * На уровне всего приложения
     */
    String APPLICATION = "APP";

    /**
     * Диалоговые окна
     */
    String DIALOG = "UI_DLG";

    /**
     * Неизвестно
     */
    String NONE = "NONE";
}
