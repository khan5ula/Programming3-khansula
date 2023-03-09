package com.server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class for WarningHandler to check that the
 * content it receives is valid
 */
public class JsonChecker {
    public JsonChecker() {}

    /**
     * Method that checks if the given String can properly
     * by converted to JSONObject.
     * <p>This method should be used to ensure the incoming content is a 
     * JSONObject instead of only resembling JSON.
     * @param content The message received from client as String
     * @return boolean, true if content is proper JSON, false if not
     */
    public boolean isJSONValid(final String content) {
        try {
            new JSONObject(content);
        } catch (final JSONException e) {
            System.out.println("Error: The received message was not proper JSON");
            return false;
        }
        System.out.println("Success: The received message was proper JSON");
        return true;
    }

    /**
     * Method that iterates through received content 
     * and checks if it contains all required information.
     * <p>The return value should be used as HTTP response code.
     * @param content String received from the client with InputStream
     * @return int, 200 if all fields are OK, 413 if not
     */
    public int checkContentIsValid(final String content) {
        System.out.println("Status: Checking if the content has nickname, latitude, longitude, dangertype and sent");
        System.out.println("Status: Also checking that latitude and longitude are double");
        
        final JSONObject contentToJSON = new JSONObject(content);

        if (contentToJSON.has("nickname") && !contentToJSON.isNull("nickname")) {
            if (contentToJSON.has("latitude") && !contentToJSON.isNull("latitude")) {
                if (contentToJSON.has("longitude") && !contentToJSON.isNull("longitude")) {
                    if (contentToJSON.has("dangertype") && !contentToJSON.isNull("dangertype")) {
                        if (contentToJSON.has("sent") && !contentToJSON.isNull(("sent"))) {
                            System.out.println("Success: The content contains all required information");
                            System.out.println("Status: Checking if latitude and longitude are in double format");
                            
                            /* Check that latitude and longitude are double */
                            try {
                                contentToJSON.getDouble("latitude");
                                contentToJSON.getDouble("longitude");
                            } catch (final JSONException e) {
                                System.out.println("Failure: Latitude and/or longitude were not double");
                                return 413;
                            }

                            System.out.println("Success: Latitude and longitude are double");
                            System.out.println("Success: Content is accepted");
                            return 200;            
                        }
                    }
                }
            }
        }

        System.out.println("Error: The content does not have all required information");  
        return 413;
    }

    /**
     * Method that checks if the content has supported danger type.
     * <p>Accepted danger types are:
     * <ul>
     * <li>moose</li>
     * <li>reindeer</li>
     * </ul>
     * <p>The return value should be used as HTTP response code.
     * @param content JSONObject, from client
     * @return int, 200 if dangertype is OK, 413 if not
     */
    public int checkDangertype(final JSONObject content) {
        final String dangertype = content.getString("dangertype").toLowerCase();

        /* List of accepted danger types */
        switch(dangertype) {
            case "moose": case "reindeer":
                System.out.println("Success: Proper danger type detected");
                return 200;
        }
        System.out.println("Error: Invalid danger type detected");
        return 413;
    }

    /**
     * Method that checks if the JSONObject has 
     * two additional fields: areacode and phonenumber.
     * @param content JSONObject, contains the information required for WarningMessage, 
     * originally received from the client.
     * @return boolean, return true if content has areacode and phonenumber, false otherwise.
     */
    public boolean checkJsonForAreaAndPhone(final JSONObject content) {
        System.out.println("Status: Checking if the WarningMessage has area code and phone number");
        if (content.has("areacode")) {
            if (content.has("phonenumber")) {
                System.out.println("Status: The message has areacode and phone number");
                return true;
            }
        }
        System.out.println("Status: The message does not have both areacode and phone number");
        return false;
    }

    /**
     * Method that checks is the JSON contains a field "weather".
     * @param content JSONObject, contains the information required for WarningMessage, 
     * originally received from the client.
     * @return boolean, true if "weather" was found, false if not.
     */
    public boolean checkJsonForWeather(final JSONObject content) {
        System.out.println("Status: Checking if the WarningMessage requests weather");
        if (content.has("weather")) {
            System.out.println("Status: Weather request found");
            return true;
        }
        System.out.println("Status: Weather request not found");
        return false;
    }
}
