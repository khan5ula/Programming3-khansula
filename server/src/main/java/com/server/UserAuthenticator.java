package com.server;

import java.util.Hashtable;
import java.util.Map;

public class UserAuthenticator extends com.sun.net.httpserver.BasicAuthenticator {
    private Map<String,String> users = null;

    public UserAuthenticator() {
        super("warning");
        users = new Hashtable<String, String>();
        //users.put("dummy", "passwd");
    }

    @Override
    public boolean checkCredentials(String uname, String passwd) {
        for (String key : this.users.keySet()) {
            if (key.equals(uname)) {
                if (this.users.get(key).equals(passwd)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addUser(String userName, String password) {
        if (this.users.containsKey(userName)) {
            System.out.println("Error: The username is taken");
            return false;
        }
        users.put(userName, password);
        return true;
    }
}
