package models;

import controllers.UserManager;

public class User {
    private int id;
    private String username;

    public User(String username) {
        id = UserManager.getId();
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
