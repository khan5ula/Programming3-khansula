package com.server;

/**
 * Represents a user in the system with 
 * a username, password, and email address.
 */
public class User {
    private String username;
    private String password;
    private String email;

    /**
     * Creates a new User object with the specified 
     * username, password, and email address.
     *
     * @param username: The username of the user
     * @param password: The password of the user
     * @param email: The email address of the user
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /* Basic getters */
    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmail() {
        return this.email;
    }

    /**
     * Sets the username of the user to the specified value.
     *
     * @param username The username String that will replace the original
     * @throws IllegalArgumentException if the specified username is empty
     */
    public void setUsername(String username) throws IllegalArgumentException {
        if (!username.isEmpty()) {
            this.username = username;
        } else {
            System.out.println("Error: Tried to register empty username");
            throw new IllegalArgumentException("Username cannot be empty");
        }
    }

    /**
     * Sets the password of the user to the specified value.
     * @param password The password string that will replace the original
     * @throws IllegalArgumentException if the specified password is empty 
     */
    public void setPassword(String password) throws IllegalArgumentException {
        if (!password.isEmpty()) {
            this.password = password;
        } else {
            System.out.println("Error: Tried to enter empty password");
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }
}
