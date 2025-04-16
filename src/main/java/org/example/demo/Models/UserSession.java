package org.example.demo.Models;

public class UserSession {
    private static UserSession instance;

    private int userId;
    private String username;
    private String email;

    private UserSession(int userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public UserSession(int userId) {
        this.userId = userId;
    }

    public static void createInstance(int userId) {
        instance = new UserSession(userId);
    }

    public static void initialize(int userId, String username, String email) {
        if (instance == null) {
            instance = new UserSession(userId, username, email);
        }
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void clear() {
        instance = null;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void logout() {
        instance = null;
    }
}
