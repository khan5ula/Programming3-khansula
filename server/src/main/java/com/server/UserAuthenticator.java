package com.server;

import java.sql.SQLException;
import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {
    private MessageDatabase messageDatabase;

    public UserAuthenticator() {
        super("warning");
        this.messageDatabase = MessageDatabase.getInstance();
    }

    /* Method that checks if the given username can be found, and the password matches */
    @Override
    public boolean checkCredentials(String uname, String passwd) throws IllegalArgumentException {
        try {
            if (this.messageDatabase.authenticateUser(uname, passwd)) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error occured while authenticating user: " + e.getMessage());
        }

        return false;
    }

    /* Check if the given username is available. If it is and the input is valid, add the new user to the list */
    public boolean addUser(JSONObject user) throws SQLException {
        if (this.messageDatabase.setUser(user)) {
            return true;
        }

        return false;
    }
}
