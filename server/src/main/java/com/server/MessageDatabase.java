package com.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.concurrent.Executors;

import java.security.SecureRandom;
import org.apache.commons.codec.digest.Crypt;

import org.json.JSONArray;
import org.json.JSONObject;

/* Class that wraps all relevant database methods */
public class MessageDatabase {
    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private SecureRandom secureRandom = null;

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
        System.out.println("Status: Initializing database connection");

        if (this.dbConnection != null) {
            createMessageTable();
            createUserTable();
            this.secureRandom = new SecureRandom();
            return true;
        }

        return false;
    }

    /* Method for the server, opens a database connection with a DB of given path */
    public void open(String dbName) throws SQLException {
        System.out.println("Status: Opening the database file");
        boolean exists = false;
        File file = new File(dbName);
        System.out.println("Status: Checking the given database file: " + file.toString());

        /* Check if the given file can be found. Also make sure that it is not a directory */
        if (!file.isDirectory()) {
            System.out.println("Status: " + file.toString() + " is not a directory, good");
            if (file.isFile()) {
                System.out.println("Status: " + file.toString() + " is a file. Database item found");
                exists = true;
            }
        }

        try {
            String address = "jdbc:sqlite:" + dbName;
            this.dbConnection = DriverManager.getConnection(address);
        } catch (Exception e) {
            System.out.println("Error while estabilishing dbConnection: " + e.getMessage());
        }

        /* If the given file could not be found, initialize a new database using that name */
        if (!exists) {
            System.out.println("Status: Database was not found, initializing a new one");
            initialize();
        }
    }

    /* Method that handles the message table creation */
    private void createMessageTable() throws SQLException {
        System.out.println("Status: Creating message table");
        
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

            System.out.println("Success: Message table initialized");
        } catch (SQLException e) {
            System.out.println("SQLException occured while creating message table: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error occured while creating message table: " + e.getMessage());
        }
    }

    /* Method that handles the user table creation */
    private void createUserTable() throws SQLException {
        System.out.println("Status: Creating user table");
        try {
            String createPrompt =
            "CREATE TABLE users (" +
            "username VARCHAR (50) PRIMARY KEY," +
            "password VARCHAR (50) NOT NULL," +
            "email VARCHAR (50) NOT NULL)";

            Statement createStatement = this.dbConnection.createStatement();
            createStatement.executeUpdate(createPrompt);
            createStatement.close();

            System.out.println("Success: User table initialized");
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
            System.out.println("Status: Closing database connection");
            this.dbConnection = null;
        }
    }

    /* Method for inserting a message to database */
    public void setMessage(WarningMessage message) throws SQLException {
        System.out.println("Status: Setting a new WarningMessage to database");

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

    /*  Method that checks if there are any messages in the database,
        breaks the loop after 1 message instance has been found
     */
    public int checkIfThereAreMessages() throws SQLException {
        int count = 0;
        Statement queryStatement = this.dbConnection.createStatement();
        ResultSet result = queryStatement.executeQuery("SELECT * FROM messages ORDER BY rowid");

        while (result.next()) {
            count++;
            if (count > 0) {
                break;
            }
        }

        return count;
    }

    /* Method for getting messages from the database */
    public JSONArray getMessages() throws SQLException {
        System.out.println("Status: Getting messages from database");

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if (checkIfThereAreMessages() > 0) {
            Statement queryStatement = this.dbConnection.createStatement();
            ResultSet result = queryStatement.executeQuery("SELECT * FROM messages ORDER BY rowid");
    
            while (result.next()) {
                WarningMessage msg = new WarningMessage(result.getString("nickname"), result.getDouble("latitude"), result.getDouble("longitude"), result.getString("dangertype"), WarningMessage.setSent(result.getLong("sent")));
                jsonObject.put("sent", msg.getSent(ZoneOffset.UTC));
                jsonObject.put("nickname", msg.getNickname());
                jsonObject.put("latitude", msg.getLatitude());
                jsonObject.put("longitude", msg.getLongitude());
                jsonObject.put("dangertype", msg.getDangertype());
                jsonArray.put(jsonObject);
                // Initialize empty object for next round
                jsonObject = new JSONObject();
            }
        }

        return jsonArray;
    }

    /* Method that puts a new user to database */
    public boolean setUser(JSONObject user) throws SQLException {
        System.out.println("Status: Setting new user to database");

        if (checkIfUserExists(user.getString("username"))) {
            System.out.println("Error: User already exists");
            return false;
        }

        if (user.getString("username") == null || user.getString("password") == null || user.getString("email") == null) {
            System.out.println("Error: Not all mandatory information received for creating new user");
            return false;
        }

        /* Hash the password with salt */
        byte bytes[] = new byte[13];
        this.secureRandom.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;
        String hashedPassword = Crypt.crypt(user.getString("password"), salt);
        

        try {
            StringBuilder temp = new StringBuilder("insert into users ");
            temp.append("VALUES('");
            temp.append(user.getString("username"));
            temp.append("','");
            temp.append(hashedPassword);
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

    /* Method that checks if given username is in the database */
    private boolean checkIfUserExists(String givenUsername) throws SQLException {
        System.out.println("Status: Checking from database if given username exists");

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

    /* Method that checks if the given username/password combination can be found from database */
    public boolean authenticateUser(String givenUserName, String givenPassword) throws SQLException {
        System.out.println("Status: Authenticating user from database");

        Statement queryStatement = null;
        ResultSet result;
        
        String getMessagesString = "select username, password from users where username = '" + givenUserName + "'";
        
        queryStatement = this.dbConnection.createStatement();
        result = queryStatement.executeQuery(getMessagesString);
        
        if (result.next() == false) {
            System.out.println("Error: Could not find given username");
            return false;
        } else {
            /* Fetch the hashed password from database */
            String hashedPassword = result.getString("password");
            /* Check if the given plaintext password matches with the hashed one */
            if (hashedPassword.equals(Crypt.crypt(givenPassword, hashedPassword))) {
                System.out.println("Status: User credentials are correct");
                return true;
            } else {
                System.out.println("Status: User credentials are incorrect");
                return false;
            }
        }
    }
}
