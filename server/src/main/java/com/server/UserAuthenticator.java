package com.server;

import java.sql.SQLException;

import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;

/**
 * Simple user authenticator class that is passed to the server.
 * Gets the database connection with a singleton object
 */
public class UserAuthenticator extends BasicAuthenticator {
    private final MessageDatabase messageDatabase;

    /**
     * Basic constructor that calls the database singleton instance
     */
    public UserAuthenticator() {
        super("warning");
        this.messageDatabase = MessageDatabase.getInstance();
    }

    /**
     * Method that checks if the given username can be found
     * and if the password matches
     * @param uname The username String that the client provided
     * @param passwd The password String that the client provided
     * @return boolean, true if the credentials matched, false if not
     */
    @Override
    public boolean checkCredentials(final String uname, final String passwd) throws IllegalArgumentException {
        try {
            if (this.messageDatabase.authenticateUser(uname, passwd)) {
                System.out.println("Status: User authenticator accepted the credentials");
                return true;
            }
        } catch (final SQLException e) {
            System.out.println("Error occured while authenticating user: " + e.getMessage());
        }
        System.out.println("Status: User authenticator detected incorrect credentials");
        return false;
    }

    /**
     * Method that checks if the given username is available.
     * <p>If it is and the input is valid, add the new user to the list
     * @param user The new user to be added as a JSONObject
     * @return boolean, true if the database allows the user to be added,
     * false if not
     */
    public boolean addUser(final JSONObject user) throws SQLException {
        if (this.messageDatabase.setUser(user)) {
            return true;
        }
        return false;
    }
}
