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

/**
 * Custom HttpHandler class that only covers POST requests,
 */
public class RegistrationHandler implements HttpHandler {
    public RegistrationHandler() {}

    /**
     * Method that checks whether the http request is POST.
     * Checks the request contents and tries to add a new
     * user to the database using the provided user information.
     * <p>Uses the database with a singleton object.
     * @param exchangeObject received from the client.
     */
    @Override
    public void handle(HttpExchange exchangeObject) {
        Headers headers = exchangeObject.getRequestHeaders();
        String newUser = "";
        int code = 0;
        JSONObject usertoJSON;
        MessageDatabase messageDatabase = MessageDatabase.getInstance();

        System.out.println("Status: Request handled in thread " + Thread.currentThread().getId());

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
                    System.out.println("Error: User JSON parsing failed: " + e.getMessage());
                    code = 500;
                }  
            }
        }

        /* handle() method is now finished. Response code is ready. Time to send it. */
        sendResponse(code, exchangeObject);
        System.out.println("Status: Response " + code + " has been sent, registration handle ends");
    }

    /** 
     * Method that checks if the exchange object contains POST request .
     * <p>The return value should be used as HTTP response code.
     * @param exchangeObject is received from the client
     * @return int, 0 if the request is POST, 400 if not
     */
    private int checkRequestForPost(HttpExchange exchangeObject) {
        if (exchangeObject.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("Success: The request is POST");
            return 0;
        }
        System.out.println("Error 400: The request was not POST");
        return 400;
    }

    /**
     * Method that checks if the exchange header has a content type 
     * @param headers HttpHeaders from the client
     * @return int, 0 if headers contains Content-Type, 411 if not
     * the result is used as Http response code
     */
    private int checkContentTypeAvailability(Headers headers) {
        if (headers.containsKey("Content-Type")) {
            System.out.println("Success: Content-Type found from header");
            return 0;
        }
        System.out.println("Error 411: No content type found from the header");
        return 411;
    }

    /**
     * Method that checks if the requested content type is supported.
     * <p>The content-type must be "application/json".
     * <p>The return value should be used as HTTP response code.
     * @param headers HttpHeaders from the client
     * @return int, 0 if supported Content-Type was found, 415 if not
     */
    private int checkContentTypeContents(Headers headers) {
        String contentType = headers.get("Content-Type").get(0);
        if (contentType.equalsIgnoreCase("application/json")) {
            System.out.println("Success: Supported Content-Type found");
            return 0;
        }
        System.out.println("Error 415: Requested Content-Type is unsupported");
        return 415;
    }
    
    /** 
     * Method that checks if the given input is null.
     * <p>The return value should be used as HTTP response code.
     * @param input received from client with InputStream
     * @return int, 200 if content is OK, 412 if not
     */
    private int checkUserFormat(String input) {
        if (input != null && input.length() > 0) {
            System.out.println("Success: User information is in expected format");
            return 200;
        }
        System.out.println("Error: User information is in invalid format");
        return 412;
    }

    /**
     * Method that checks if the given JSON is valid
     * by checking that all required fields are included
     * and they are not null.
     * @param input received from client and converted to JSONObject
     * @return int, 200 if content is ok, 413 if not
     * the result is used as Http response code
     */
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

    /**
     * Method that sends a response to the output stream 
     * depending on the what is the HTML status code is.
     * <p>Sends the status code and a proper message to the client
     * as output stream.
     * @param code is the HTML status code received from handle() method
     * @param exchangeObject is the HTML exchange object received from the client
     */
    public void sendResponse(int code, HttpExchange exchangeObject) {
        System.out.println("Status: Preparing the response message for the client");
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
                System.out.println("Error: Registration was not succesful");
                responseBuilder.append("The server faced an error while processing the request");
                code = 500;
        }

        String responseString = responseBuilder.toString();
        byte[] bytes;
        System.out.println("Status: The response string is now ready");

        try {
            bytes = responseString.getBytes("UTF-8");
            try {
                System.out.println("Status: Sending a response code: " + code);
                exchangeObject.sendResponseHeaders(code, bytes.length);
                OutputStream outputStream = exchangeObject.getResponseBody();
                outputStream.write(bytes);
                System.out.println("Status: The response has been sent");
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
