package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class RegistrationHandler implements HttpHandler {
    private MessageDatabase messageDatabase;

    public RegistrationHandler() {
        messageDatabase = MessageDatabase.getInstance();
    }

    @Override
    public void handle(HttpExchange exchangeObject) {
        Headers headers = exchangeObject.getRequestHeaders();
        String newUser = "";
        int code = 0;
        JSONObject usertoJSON;

        /* Check if the request is POST */
        code = checkRequestForPost(exchangeObject);

        /* Check if the header has a content type */
        if (code == 0) {
            code = checkContentTypeAvailability(headers);
        }

        /* Check if the content type is "aplication/json" */
        if (code == 0) {
            code = checkContentTypeContents(headers);
        }

        /* Read the input stream */
        if (code == 0) {
            try {
                InputStream inputStream = exchangeObject.getRequestBody();
                newUser = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                inputStream.close();
                System.out.println("Success: User information data received");
            } catch (Exception e) {
                System.out.println("Error: Exception occured while processing input stream");
            }
        }

        /* Check the validity of the content, add a new user if OK */
        if (code == 0) {
            if ((code = checkUserFormat(newUser.toString())) == 200) {
                try {
                    usertoJSON = new JSONObject(newUser);
                    code = checkUserContent(usertoJSON);
                    if (code == 200) {
                        if (messageDatabase.setUser(usertoJSON)) {
                            System.out.println("Success: RegistrationHandler added the new user successfully");
                        } else {
                            System.out.println("Error 409: User could not be added");
                            code = 409;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error during JSON parsing");
                    e.getMessage();
                }  
            }
        }

        /* We've got a response code. Let's send it. Godspeed. */
        sendResponse(code, exchangeObject);
        System.out.println("The response code is: " + code);
    }

    /* Method that checks if the exchange object contains POST request */
    private int checkRequestForPost(HttpExchange exchangeObject) {
        if (exchangeObject.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("Success: The request is POST");
            return 0;
        }
        System.out.println("Error 400: The request was not POST");
        return 400;
    }

    /* Method that checks if the exchange header has a content type */
    private int checkContentTypeAvailability(Headers headers) {
        if (headers.containsKey("Content-Type")) {
            System.out.println("Success: Content-Type found from header");
            return 0;
        }
        System.out.println("Error 411: No content type found from the header");
        return 411;
    }

    /* Method that checks if the requested content type is supported */
    private int checkContentTypeContents(Headers headers) {
        String contentType = headers.get("Content-Type").get(0);
        if (contentType.equalsIgnoreCase("application/json")) {
            System.out.println("Success: Supported Content-Type found");
            return 0;
        }
        System.out.println("Error 415: Requested Content-Type is unsupported");
        return 415;
    }
    
    /* Method that checks if the given user information format is valid */
    private int checkUserFormat(String input) {
        if (input != null && input.length() > 0) {
            System.out.println("Success: User information is in expected format");
            return 200;
        }
        System.out.println("Error: User information is in invalid format");
        return 412;
    }

    /* Method that checks if the given JSON is valid */
    private int checkUserContent(JSONObject input) {
        if (input.has("username") && input.getString("username").length() > 0) {
            if (input.has("password") && input.getString("password").length() > 0) {
                if (input.has("email") && input.getString(("email")).length() > 0) {
                    System.out.println("Success: The given JSON is valid");
                    return 200;   
                }
            }
        }
        System.out.println("Error: Given JSON is not valid");
        System.out.println("No proper user credentials");
        return 413;
    }

    /* Method that sends a response to the output stream depending on the html status code */
    public void sendResponse(int code, HttpExchange exchangeObject) {
        StringBuilder responseBuilder = new StringBuilder("");

        switch(code) {
            case 200:
                responseBuilder.append("New user added successfully");
                break;
            case 400:
                responseBuilder.append("Only Post requests accepted with registration service");
                break;
            case 409:
                responseBuilder.append("Conflict: The username is already taken");
                break;
            case 411:
                responseBuilder.append("Error: No content type provided");
                break;
            case 412:
                responseBuilder.append("Registration failed: The given information was invalid");
                break;
            case 413:
                responseBuilder.append("Registration failed: The given user credentials were not correct");
                break;
            case 415:
                responseBuilder.append("Error: Requested content type is not supported");
                break;
            default:
                System.out.println("Error: Cannot send response without status code");
                responseBuilder.append("The system logic failed miserably");
                code = 400;
        }

        String responseString = responseBuilder.toString();
        byte[] bytes;

        try {
            bytes = responseString.getBytes("UTF-8");
            try {
                System.out.println("Status: Sending a response code: " + code);
                exchangeObject.sendResponseHeaders(code, bytes.length);
                OutputStream outputStream = exchangeObject.getResponseBody();
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                System.out.println("Error: IO Exception occured during registration process");
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error: Unsupported Encoding occured during registration process");
            e.printStackTrace();
        }
    }
}
