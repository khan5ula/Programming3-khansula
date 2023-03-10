package com.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* Class that wraps all database related methods */
public class MessageDatabase {
    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private SecureRandom secureRandom = null;

    /**
     * Public getter for the singleton instance. Other classes must use 
     * this method instead of the actual constructor.
     * <p>Ensures that only one instance of the class object
     * exists.
     * <p>If the class member variable instance it null, calls the
     * constructor to create one instance
     * @return Messagedatabase object
     */
    public static synchronized MessageDatabase getInstance() {
        if (dbInstance == null) {
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    /* Private constructor for Singleton implementation */
    private MessageDatabase() {
        try {
            System.out.println("Status: MessageDatabase constructor calls initialize");
            initialize();
        } catch (final SQLException e) {
            System.out.println("Error: Something went wrong in the DB constructor: " + e.getMessage());
        }
    }

    /* Method that creates a SQL connection */

    /**
     * Method that is used to initialize database tables.
     * <p>Calls createMessageTable() and createUserTable() to create
     * two tables for the database.
     * <p>Also initializes SecureRandom class variable required for
     * secure passwords.
     * @return boolean, true if SQL database connection exists, false otherwise
     * @throws SQLException
     */
    private boolean initialize() throws SQLException {
        System.out.println("Status: Initializing database");

        if (this.dbConnection != null) {
            System.out.println("Status: Calling method for message table creation");
            createMessageTable();
            System.out.println("Status: Calling method for user table creation");
            createUserTable();
            this.secureRandom = new SecureRandom();
            return true;
        }

        return false;
    }

    /**
     * Method that opens an existing database file or creates a new one.
     * <p>Uses the parameter value to look for a database file. 
     * If a file with the given name is not found, creates a new file.
     * <p>Combines the given name with "jdbc:sqlite:" to create a
     * database connection.
     * 
     * @param dbName, String that provides a name for the database file
     * @throws SQLException
     */
    public void open(final String dbName) throws SQLException {
        System.out.println("Status: Opening the database file");
        boolean exists = false;
        final File file = new File(dbName);
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
            final String address = "jdbc:sqlite:" + dbName;
            this.dbConnection = DriverManager.getConnection(address);
        } catch (final Exception e) {
            System.out.println("Error while estabilishing dbConnection: " + e.getMessage());
        }

        /* If the given file could not be found, initialize a new database using that name */
        if (!exists) {
            System.out.println("Status: Database was not found, initializing a new one");
            initialize();
        }
    }

    /**
     * Method that creates a message table to the database.
     * <p>The table will contain following attributes:
     * <ul>
     * <li>sent (int)</li>
     * <li>nickname (varchar)</li>
     * <li>latitude (double)</li>
     * <li>longitude (double)</li>
     * <li>dangertype (varchar)</li>
     * <li>areacode (int)</li>
     * <li>phonenumber (varchar)</li>
     * </ul>
     * <p>Primary key is the combination of sent and nickname.
     * @throws SQLException
     */
    private void createMessageTable() throws SQLException {
        System.out.println("Status: Creating message table");
        
        try {
            final String createPrompt = 
            "CREATE TABLE messages (" +
            "sent INT NOT NULL," +
            "nickname VARCHAR (50) NOT NULL," +
            "latitude DOUBLE NOT NULL," +
            "longitude DOUBLE NOT NULL," +
            "dangertype VARCHAR(255) NOT NULL," +
            "areacode INT," +
            "phonenumber VARCHAR (50)," +
            "weather INT," +
            "PRIMARY KEY (sent, nickname))";

            final Statement createStatement = this.dbConnection.createStatement();
            createStatement.executeUpdate(createPrompt);
            createStatement.close();

            System.out.println("Success: Message table initialized");
        } catch (final SQLException e) {
            System.out.println("SQLException occured while creating message table: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error occured while creating message table: " + e.getMessage());
        }
    }

    /**
     * Method that creates a user table to the database.
     * <p>The table will contain following attributes:
     * <ul>
     * <li>username (varchar)</li>
     * <li>password (varchar)</li>
     * <li>email (varchar)</li>
     * </ul>
     * <p>Primary key is the username since it is required to be unique
     * @throws SQLException
     */
    private void createUserTable() throws SQLException {
        System.out.println("Status: Creating user table");
        try {
            final String createPrompt =
            "CREATE TABLE users (" +
            "username VARCHAR (50) PRIMARY KEY," +
            "password VARCHAR (50) NOT NULL," +
            "email VARCHAR (50) NOT NULL)";

            final Statement createStatement = this.dbConnection.createStatement();
            createStatement.executeUpdate(createPrompt);
            createStatement.close();

            System.out.println("Success: User table initialized");
        } catch (final SQLException e) {
            System.out.println("SQLException occured while creating user table: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error occured while creating user table: " + e.getMessage());
        }
    }

    /**
     * Method that closes the database connection.
     * <p>Calls the Connection.close() method and 
     * sets the Connection class member variable to null.
     * @throws SQLException
     */
    public void closeDB() throws SQLException {
        if (this.dbConnection == null) {
            this.dbConnection.close();
            System.out.println("Status: Closing database connection");
            this.dbConnection = null;
        }
    }

    /**
     * Method that inserts a new WarningMessage to the database.
     * <p>Creates a Insert into messages statement.
     * @param message WarningMessage that WarningHandler.handle() passes to the database
     * @throws SQLException
     */
    public void setMessage(final WarningMessage message) throws SQLException {
        System.out.println("Status: Setting a new WarningMessage to database");

        final StringBuilder temp = new StringBuilder("insert into messages ");

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
        temp.append("','");
        temp.append(message.getAreacode());
        temp.append("','");
        temp.append(message.getPhonenumber());
        temp.append("','");
        temp.append(message.getWeather());
        temp.append("')");

        final String setMessageString = temp.toString();
        Statement createStatement;
        createStatement = this.dbConnection.createStatement();
        createStatement.executeUpdate(setMessageString);
        createStatement.close();
    }

     /**
      * Method that checks if there are any messages in the database.
      * <p>Breaks the loop if one message instance has been found.
      * @return int, 0 if there were not messages, 1 if any were found
      * @throws SQLException
      */
    public int checkIfThereAreMessages() throws SQLException {
        int count = 0;
        final Statement queryStatement = this.dbConnection.createStatement();
        final ResultSet result = queryStatement.executeQuery("SELECT * FROM messages ORDER BY rowid");

        while (result.next()) {
            count++;
            if (count > 0) {
                break;
            }
        }

        return count;
    }

    /**
     * Method for getting messages from the database.
     * <p>Creates a Select from messages statement.
     * <p>Selects all messages from the messages table.
     * @return JSONArray of all WarningMessages stored in the database.
     * @throws SQLException
     */
    public JSONArray getMessages() throws SQLException {
        System.out.println("Status: Getting messages from database");

        JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        if (checkIfThereAreMessages() > 0) {
            final Statement queryStatement = this.dbConnection.createStatement();
            final ResultSet result = queryStatement.executeQuery("SELECT * FROM messages ORDER BY rowid");
    
            while (result.next()) {
                WarningMessage msg = new WarningMessage(result.getString("nickname"), result.getDouble("latitude"), result.getDouble("longitude"), result.getString("dangertype"), WarningMessage.setSent(result.getLong("sent")));
                jsonObject.put("sent", msg.getSent(ZoneOffset.UTC));
                jsonObject.put("nickname", msg.getNickname());
                jsonObject.put("latitude", msg.getLatitude());
                jsonObject.put("longitude", msg.getLongitude());
                jsonObject.put("dangertype", msg.getDangertype());

                /* Check if the warningmessage has areacode and phonenumber */
                if (result.getInt("areacode") > 0 && result.getString("phonenumber") != null) {
                    msg.setAreacode(result.getString("areacode"));
                    msg.setPhonenumber(result.getString("phonenumber"));
                    jsonObject.put("areacode", msg.getAreacode());
                    jsonObject.put("phonenumber", msg.getPhonenumber());
                }

                /* Check if the warningmessage has weather information */
                if (result.getInt("weather") > -999) {
                    msg.setWeather(result.getInt("weather"));
                    jsonObject.put("weather", msg.getWeather() + " Celsius");
                }

                jsonArray.put(jsonObject);
                // Initialize empty object for next round
                jsonObject = new JSONObject();
            }
        }

        return jsonArray;
    }

    public JSONArray getMessagesByUser(String nickname) throws JSONException, SQLException {
        System.out.println("Status: Getting messages with nickname: " + nickname);

        JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        if (checkIfThereAreMessages() > 0) {
            final String queryPrompt =
            "SELECT * FROM messages WHERE nickname = ?";
            final PreparedStatement queryStatement = this.dbConnection.prepareStatement(queryPrompt);
            queryStatement.setString(1, nickname);
            final ResultSet result = queryStatement.executeQuery(); 

            while (result.next()) {
                WarningMessage msg = new WarningMessage(result.getString("nickname"), result.getDouble("latitude"), result.getDouble("longitude"), result.getString("dangertype"), WarningMessage.setSent(result.getLong("sent")));
                jsonObject.put("sent", msg.getSent(ZoneOffset.UTC));
                jsonObject.put("nickname", msg.getNickname());
                jsonObject.put("latitude", msg.getLatitude());
                jsonObject.put("longitude", msg.getLongitude());
                jsonObject.put("dangertype", msg.getDangertype());

                /* Check if the warningmessage has areacode and phonenumber */
                if (result.getInt("areacode") > 0 && result.getString("phonenumber") != null) {
                    msg.setAreacode(result.getString("areacode"));
                    msg.setPhonenumber(result.getString("phonenumber"));
                    jsonObject.put("areacode", msg.getAreacode());
                    jsonObject.put("phonenumber", msg.getPhonenumber());
                }

                /* Check if the warningmessage has weather information */
                if (result.getInt("weather") > -999) {
                    msg.setWeather(result.getInt("weather"));
                    jsonObject.put("weather", msg.getWeather() + " Celsius");
                }

                jsonArray.put(jsonObject);
                // Initialize empty object for next round
                jsonObject = new JSONObject();
            }
        }

        return jsonArray;
    }

    public JSONArray getMessagesByTimeInterval(long timeStart, long timeEnd) throws JSONException, SQLException {
        System.out.println("Status: Getting messages with a time interval");

        JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        if (checkIfThereAreMessages() > 0) {
            final String queryPrompt =
            "SELECT * FROM messages WHERE sent >= ? AND sent <= ?";
            final PreparedStatement queryStatement = this.dbConnection.prepareStatement(queryPrompt);
            queryStatement.setLong(1, timeStart);
            queryStatement.setLong(2, timeEnd);
            final ResultSet result = queryStatement.executeQuery();

            while (result.next()) {
                WarningMessage msg = new WarningMessage(result.getString("nickname"), result.getDouble("latitude"), result.getDouble("longitude"), result.getString("dangertype"), WarningMessage.setSent(result.getLong("sent")));
                jsonObject.put("sent", msg.getSent(ZoneOffset.UTC));
                jsonObject.put("nickname", msg.getNickname());
                jsonObject.put("latitude", msg.getLatitude());
                jsonObject.put("longitude", msg.getLongitude());
                jsonObject.put("dangertype", msg.getDangertype());

                /* Check if the warningmessage has areacode and phonenumber */
                if (result.getInt("areacode") > 0 && result.getString("phonenumber") != null) {
                    msg.setAreacode(result.getString("areacode"));
                    msg.setPhonenumber(result.getString("phonenumber"));
                    jsonObject.put("areacode", msg.getAreacode());
                    jsonObject.put("phonenumber", msg.getPhonenumber());
                }

                /* Check if the warningmessage has weather information */
                if (result.getInt("weather") > -999) {
                    msg.setWeather(result.getInt("weather"));
                    jsonObject.put("weather", msg.getWeather() + " Celsius");
                }

                jsonArray.put(jsonObject);
                // Initialize empty object for next round
                jsonObject = new JSONObject();
            }
        }

        return jsonArray;
    }
    /**
     * Method that puts a new user to the database.
     * <p>Creates Insert into users statement.
     * <p>Calls checkIfUserExists() method to check if the username is available.
     * <p>Checks if the user passed as parameter has proper username and password information.
     * <p>Stores the password securely using hash and salt.
     * @param user JSONObject that contains username and password
     * @return boolean, true if the user was added succesfully, false if not
     * @throws SQLException
     */
    public synchronized boolean setUser(final JSONObject user) throws SQLException {
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
        try {
            final byte bytes[] = new byte[13];
            this.secureRandom.nextBytes(bytes);
            final String saltBytes = new String(Base64.getEncoder().encode(bytes));
            final String salt = "$6$" + saltBytes;
            final String hashedPassword = Crypt.crypt(user.getString("password"), salt);      

            final StringBuilder temp = new StringBuilder("insert into users ");
            temp.append("VALUES('");
            temp.append(user.getString("username"));
            temp.append("','");
            temp.append(hashedPassword);
            temp.append("','");
            temp.append(user.getString("email"));
            temp.append("')");
            final String setUserString = temp.toString();
            
            Statement createStatement;
            createStatement = this.dbConnection.createStatement();
            createStatement.executeUpdate(setUserString);
            createStatement.close();
            
            return true;       
        } catch (final Exception e) {
            System.out.println("Error: MessageDatabase.setUser failed: " + e.getMessage());
        }

        System.out.println("Error: setUser() failed");
        return false;
    }

    /**
     * Method that checks if the given username is in the database.
     * <p>Creates a select username from users query.
     * @param givenUsername, the username to be queried as a String
     * @return boolean, true if the username was found, false if not
     * @throws SQLException
     */
    private synchronized boolean checkIfUserExists(final String givenUsername) throws SQLException {
        System.out.println("Status: Checking from database if given username exists");

        Statement queryStatement = null;
        ResultSet result;
        
        final String checkUser = "select username from users where username = '" + givenUsername + "'";
        System.out.println("Status: Checking user");

        try {
            queryStatement = this.dbConnection.createStatement();
            result = queryStatement.executeQuery(checkUser);   
            
            if (result.next()) {
                System.out.println("User with the given username found");
                return true;
            }
        } catch (final Exception e) {
            System.out.println("Error occured while doing a check user exists query: " + e.getMessage());
        }

        return false;
    }

    /**
     * Method that authenticates the user by checking if the given credentials are proper.
     * <p>Creates a select username, password from users query.
     * <p>This method is required by UserAuthenticator.checkCredentials().
     * @param givenUserName as String
     * @param givenPassword as String
     * @return boolean, true if the user credentials were proper, false if not/username was not found
     * @throws SQLException
     */
    public synchronized boolean authenticateUser(final String givenUserName, final String givenPassword) throws SQLException {
        System.out.println("Status: Authenticating user from database");

        Statement queryStatement = null;
        ResultSet result;
        
        final String getMessagesString = "select username, password from users where username = '" + givenUserName + "'";
        
        queryStatement = this.dbConnection.createStatement();
        result = queryStatement.executeQuery(getMessagesString);
        
        if (result.next() == false) {
            System.out.println("Error: Could not find given username");
            return false;
        } else {
            /* Fetch the hashed password from database */
            final String hashedPassword = result.getString("password");
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
