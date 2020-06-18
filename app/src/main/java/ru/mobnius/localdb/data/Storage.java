package ru.mobnius.localdb.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Storage {
    /**
     * Описание таблицы
     */
    String description();

    /**
     * Имя таблицы с учетом регистра
     */
    String table();
}
