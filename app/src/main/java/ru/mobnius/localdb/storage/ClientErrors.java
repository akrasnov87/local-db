package ru.mobnius.localdb.storage;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "sd_client_errors")
public class ClientErrors {
    /**
     * Идентификатор
     */
    @Id
    public String id;

    /**
     * Дата
     */
    public String date;

    /**
     * ткст ошибки
     */
    public String message;

    /**
     * Код
     */
    public String code;

    /**
     * пользователь
     */
    public String user;

    /**
     * версия приложения
     */
    public String version;

    /**
     * Платформа
     */
    public String platform;

    @Generated(hash = 1895835611)
    public ClientErrors(String id, String date, String message, String code,
            String user, String version, String platform) {
        this.id = id;
        this.date = date;
        this.message = message;
        this.code = code;
        this.user = user;
        this.version = version;
        this.platform = platform;
    }

    @Generated(hash = 2056679704)
    public ClientErrors() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}

