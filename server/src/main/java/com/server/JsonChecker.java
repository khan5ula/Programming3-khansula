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
            return false;
        }
        return true;
    }

    /**
     * Method that iterates through received content 
     * and checks if it contains all required information 
     * @param content String extracted from the client with InputStream
     * @return 200 if all fields are OK, if not, return 413
     */
    public int checkContentIsValid(final String content) {
        final JSONObject contentToJSON = new JSONObject(content);

        if (contentToJSON.has("nickname") && !contentToJSON.isNull("nickname")) {
            if (contentToJSON.has("latitude") && !contentToJSON.isNull("latitude") && contentToJSON.getDouble("latitude") > 0) {
                if (contentToJSON.has("longitude") && !contentToJSON.isNull("longitude") && contentToJSON.getDouble("longitude") > 0) {
                    if (contentToJSON.has("dangertype") && !contentToJSON.isNull("dangertype")) {
                        if (contentToJSON.has("sent") && !contentToJSON.isNull(("sent"))) {
                            System.out.println("Success: The content contains all required information");
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
            case "moose":
                return 200;
            case "reindeer":
                return 200;
            case "meteorite":
                return 200;
        }

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
                return true;
            }
        }
        return false;
    }
}
