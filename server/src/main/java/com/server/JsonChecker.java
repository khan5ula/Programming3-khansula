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
     * by converted to JSONObject
     * @param content the message received from client
     * @return true if content is proper JSON, false if not
     */
    public boolean isJSONValid(String content) {
        try {
            new JSONObject(content);
        } catch (JSONException e) {
            System.out.println("Error: The received message was not proper JSON");
            return false;
        }
        System.out.println("Success: The received message was proper JSON");
        return true;
    }

    /**
     * Method that iterates through received content 
     * and checks if it contains all required information 
     * @param content String extracted from the client with InputStream
     * @return int 200 if all fields are OK, if not, return 413
     */
    public int checkContentIsValid(final String content) {
        System.out.println("Status: Checking if the content has nickname, latitude, longitude, dangertype and sent");
        System.out.println("Status: Also checking that latitude ang longitude are double");
        
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
                            } catch (JSONException e) {
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
     * Method that checks if the content's danger type contains accepted information
     * Accepted danger types are:
     * moose, reindeer, meteorite
     * @param content the client's message in JSONObject format
     * @return 200 if dangertype is OK, 413 if not
     */
    public int checkDangertype(final JSONObject content) {
        String dangertype = content.getString("dangertype").toLowerCase();

        /* List of accepted danger types */
        switch(dangertype) {
            case "moose": case "reindeer": case "meteorite":
                System.out.println("Success: Proper danger type detected");
                return 200;
        }
        System.out.println("Error: Invalid danger type detected");
        return 413;
    }

    /**
     * Method that checks if the JSONObject has fields: areacode and phonenumber
     * @param content JSONObject that contains the information for WarningMessage
     * @return true if content has areacode and phonenumber, false otherwise
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
}
