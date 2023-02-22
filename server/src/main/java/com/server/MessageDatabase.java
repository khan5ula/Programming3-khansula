package com.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageDatabase {
    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;

    /* Public getter for Singleton implementation */
    public static synchronized MessageDatabase getInstance() {
        if (dbInstance == null) {
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    /* Private constructor for Singleton implementation */
    private MessageDatabase() {
        try {
            initialize();
        } catch (SQLException e) {
            System.out.println("Error: Something went wrong in the DB constructor: " + e.getMessage());
        }
    }

    /* Method that creates a SQL connection */
    private boolean initialize() throws SQLException {
        System.out.println("Trying to initialize database connection");

        if (this.dbConnection != null) {
            createMessageTable();
            createUserTable();
            return true;
        }

        return false;
    }

    /* Method for the server, opens a database connection with a DB of given path */
    public void open(String dbName) throws SQLException {
        System.out.println("Trying to open the database");
        boolean exists = false;
        File file = new File(dbName);
        System.out.println("Checking the given file: " + file.toString());

        if (!file.isDirectory()) {
            System.out.println(file.toString() + " is not a directory");
            if (file.isFile()) {
                System.out.println(file.toString() + " is a file");
                exists = true;
            }
        }

        try {
            String address = "jdbc:sqlite:" + dbName;
            this.dbConnection = DriverManager.getConnection(address);
        } catch (Exception e) {
            System.out.println("Error while estabilishing dbConnection: " + e.getMessage());
        }

        // If the given file was not found, initialize a new database
        if (!exists) {
            System.out.println("Database was not found, initializing a new one");
            initialize();
        }
    }

    /* Method that handles the message table creation */
    private void createMessageTable() throws SQLException {
        System.out.println("Trying to create a message table");
        
        try {
            String createPrompt = 
            "CREATE TABLE messages (" +
            "sent INT NOT NULL," +
            "nickname VARCHAR (50) NOT NULL," +
            "latitude DOUBLE NOT NULL," +
            "longitude DOUBLE NOT NULL," +
            "dangertype VARCHAR(255)," +
            "PRIMARY KEY (sent, nickname))";

            Statement createStatement = this.dbConnection.createStatement();
            createStatement.executeUpdate(createPrompt);
            createStatement.close();

            System.out.println("Message table initialized succesfully");
        } catch (SQLException e) {
            System.out.println("SQLException occured while creating message table: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error occured while creating message table: " + e.getMessage());
        }
    }

    /* Method that handles the user table creation */
    private void createUserTable() throws SQLException {
        System.out.println("Trying to create a user table");
        try {
            String createPrompt =
            "CREATE TABLE users (" +
            "username VARCHAR (50) PRIMARY KEY," +
            "password VARCHAR (50) NOT NULL," +
            "email VARCHAR (50) NOT NULL)";

            Statement createStatement = this.dbConnection.createStatement();
            createStatement.executeUpdate(createPrompt);
            createStatement.close();

            System.out.println("User table initialized succesfully");
        } catch (SQLException e) {
            System.out.println("SQLException occured while creating user table: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error occured while creating user table: " + e.getMessage());
        }
    }

    /* Method for closing the database connection */
    public void closeDB() throws SQLException {
        if (this.dbConnection == null) {
            this.dbConnection.close();
            System.out.println("Closing database connection");
            this.dbConnection = null;
        }
    }

    /* Method for inserting a message to database */
    public void setMessage(WarningMessage message) throws SQLException {
        StringBuilder temp = new StringBuilder("insert into messages ");

        temp.append("VALUES('");
        temp.append(message.dateAsInt());
        temp.append("','");
        temp.append(message.getNickname());
        temp.append("','");
        temp.append(message.getLatitude());
        temp.append("','");
        temp.append(message.getLongitude());
        temp.append("','");
        temp.append(message.getDangertype());
        temp.append("')");

        String setMessageString = temp.toString();
        Statement createStatement;
        createStatement = this.dbConnection.createStatement();
        createStatement.executeUpdate(setMessageString);
        createStatement.close();
    }

    /*  Method that checks if there are messages in the database
        If there are none, return -1
        If there is only one, return 0
        If there are several, return 1
    */
    public int messageChecker() throws SQLException {
        int count = -1;
        Statement queryStatement = this.dbConnection.createStatement();
        ResultSet result = queryStatement.executeQuery("SELECT * FROM messages ORDER BY rowid");

        while (result.next()) {
            count++;
            if (count > 1) {
                break;
            }
        }

        return count;
    }

    /* Method for getting several messages from the database */
    public JSONArray getMessages() throws SQLException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        Statement queryStatement = this.dbConnection.createStatement();
        ResultSet result = queryStatement.executeQuery("SELECT * FROM messages ORDER BY rowid");
        WarningMessage msg = new WarningMessage(result.getString("nickname"), result.getDouble("latitude"), result.getDouble("longitude"), result.getString("dangertype"), WarningMessage.setSent(result.getLong("sent")));

        while (result.next()) {
            jsonObject.put("sent", msg.getSent());
            jsonObject.put("nickname", msg.getNickname());
            jsonObject.put("latitude", msg.getLatitude());
            jsonObject.put("longitude", msg.getLongitude());
            jsonObject.put("dangertype", msg.getDangertype());
            jsonArray.put(jsonObject);
            jsonObject = new JSONObject();
        }

        return jsonArray;

    }

    /* Method for getting the only message from the database */
    public JSONObject getMessage() throws SQLException {
        JSONObject jsonObject = new JSONObject();
    
        Statement queryStatement = this.dbConnection.createStatement();
        ResultSet result = queryStatement.executeQuery("SELECT * FROM messages ORDER BY rowid");
        WarningMessage msg = new WarningMessage(result.getString("nickname"), result.getDouble("latitude"), result.getDouble("longitude"), result.getString("dangertype"), WarningMessage.setSent(result.getLong("sent")));

        while (result.next()) {
            jsonObject.put("sent", msg.getSent());
            jsonObject.put("nickname", msg.getNickname());
            jsonObject.put("latitude", msg.getLatitude());
            jsonObject.put("longitude", msg.getLongitude());
            jsonObject.put("dangertype", msg.getDangertype());
        }

        return jsonObject;
    }

    public boolean setUser(JSONObject user) throws SQLException {
        if (checkIfUserExists(user.getString("username"))) {
            System.out.println("Error: User already exists");
            return false;
        }

        if (user.getString("username") == null || user.getString("password") == null || user.getString("email") == null) {
            System.out.println("Error: Not all mandatory information received for creating new user");
            return false;
        }

        try {
            StringBuilder temp = new StringBuilder("insert into users ");
            temp.append("VALUES('");
            temp.append(user.getString("username"));
            temp.append("','");
            temp.append(user.getString("password"));
            temp.append("','");
            temp.append(user.getString("email"));
            temp.append("')");
            String setUserString = temp.toString();
            
            Statement createStatement;
            createStatement = this.dbConnection.createStatement();
            createStatement.executeUpdate(setUserString);
            createStatement.close();
            
            return true;       
        } catch (Exception e) {
            System.out.println("Error occured in MessageDatabase.setUser(): " + e.getMessage());
        }

        System.out.println("Error: setUser() failed");
        return false;
    }

    private boolean checkIfUserExists(String givenUsername) throws SQLException {
        Statement queryStatement = null;
        ResultSet result;
        
        String checkUser = "select username from users where username = '" + givenUsername + "'";
        System.out.println("Status: Checking user");

        try {
            queryStatement = this.dbConnection.createStatement();
            result = queryStatement.executeQuery(checkUser);   
            
            if (result.next()) {
                System.out.println("User with the given username found");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error occured while doing a check user exists query: " + e.getMessage());
        }

        return false;
    }

    public boolean authenticateUser(String givenUserName, String givenPassword) throws SQLException {
        Statement queryStatement = null;
        ResultSet result;
        
        String getMessagesString = "select username, password from users where username = '" + givenUserName + "'";
        
        queryStatement = this.dbConnection.createStatement();
        result = queryStatement.executeQuery(getMessagesString);
        
        if (result.next() == false) {
            System.out.println("Error: Could not find given username");
            return false;
        } else {
            String passw = result.getString("password");
            
            if (passw.equals(givenPassword)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
