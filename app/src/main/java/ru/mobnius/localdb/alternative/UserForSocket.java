package ru.mobnius.localdb.alternative;

public class UserForSocket {
    private String username; // имя игрока
    private String message; // последнее сообщение
    private int userID; // идентификатор игрока (в данном случае это порт сокета)

    public UserForSocket() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}