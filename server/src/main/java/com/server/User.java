package com.server;

public class User {
    String nickname;
    String password;
    String email;

    public User(String nickname, String password, String email) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
    }

    String getNickname() {
        return this.nickname;
    }

    String getPassword() {
        return this.password;
    }

    String getEmail() {
        return this.email;
    }

    public void setNickname(String nickname) throws IllegalArgumentException {
        if (!nickname.isEmpty()) {
            this.nickname = nickname;
        } else {
            System.out.println("Error: Entered invalid nickname");
            throw new IllegalArgumentException();
        }
    }
}
