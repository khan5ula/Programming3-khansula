package com.server;

/**
 * Class that wraps the information required by a User Query object.
 */
public class UserQuery {
    private String nickname;

    /**
     * Creates a new User Query with the given nickname.
     * @param nickname String, the nickname that will be queried from the database.
     */
    public UserQuery(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Sets the class variable nickname.
     * @param nickname String, the nickname that will be set as the class variable nickname.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Getter for the class variable nickname.
     * @return String, the class variable nickname.
     */
    public String getNickname() {
        return this.nickname;
    }
}
