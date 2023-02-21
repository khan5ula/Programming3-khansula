package com.server;

public class User {
    String username;
    String password;
    String email;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    String getUsername() {
        return this.username;
    }

    String getPassword() {
        return this.password;
    }

    String getEmail() {
        return this.email;
    }

    public void setUsername(String username) throws IllegalArgumentException {
        if (!username.isEmpty()) {
            this.username = username;
        } else {
            System.out.println("Error: Entered invalid nickname");
            throw new IllegalArgumentException();
        }
    }
}
