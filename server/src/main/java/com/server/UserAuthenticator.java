package com.server;

import java.util.ArrayList;
import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {
    private ArrayList<User> users;

    public UserAuthenticator() {
        super("warning");
        users = new ArrayList<User>();
    }

    @Override
    public boolean checkCredentials(String uname, String passwd) throws IllegalArgumentException {
    /* Iterate through the list of known users and check if given nickname can be found. If can, check if the given password matches with the one registered to the user */

        if (uname == null || passwd == null) {
            System.out.println("Error: Empty nickname or password, can't check credentials");
            throw new IllegalArgumentException();
        }

        for (User pointer : this.users) {
            if (pointer.getPassword().equals(passwd)) {
                System.out.println("Success: The nickname and password match");
                return true;
            }
        }

        System.out.println("Incorrect credentials");
        return false;
    }

    public boolean addUser(String uname, String passwd, String email) {
    /* Check if the given nickname is available. If it is and the input is valid, add the new user to the list */

        if (uname == null || passwd == null || email == null) {
            System.out.println("Error: Can't add user, empty nickname/password/email");
            throw new IllegalArgumentException();
        }

        for (User pointer: this.users) {
            if (pointer.getNickname().equals(uname)) {
                System.out.println("Error: The nickname is already taken");
                return false;
            }
        }

        System.out.println("Success: Added the new user to the list");
        users.add(new User(uname, passwd, email));
        return true;
    }
}
