package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class WarningHandler implements HttpHandler {
    StringBuilder inputStorage;
    StringBuilder errorStorage;
    MessageDatabase messageDatabase;

    public WarningHandler() {
        inputStorage = new StringBuilder("");
        messageDatabase = MessageDatabase.getInstance();
    }

    @Override
    public void handle(HttpExchange exchangeObject) throws IOException {
        String content = "";
        JSONObject contentToJSON;
        int code = 0;
        byte [] bytes = null;

        /* Handle POST case */
        if (exchangeObject.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("Got into POST handler branch");
            InputStream inputStream = exchangeObject.getRequestBody();

            /* Parse content */
            try {
                content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                inputStream.close();
                System.out.println("Success: Warning message received");          
            } catch (Exception e) {
                System.out.println("Error: Exception occured while processing input stream");
            }

            /* Make sure the warning is not empty */
            if (content == null || content.length() < 1) {
                System.out.println("Error: The content is invalid");
                code = 412;
            }

            /* Check the content validity and parse the timestamp, if OK, add to database */
            if (code == 0)
            if ((code = checkContentIsValid(content)) == 200) {
                contentToJSON = new JSONObject(content);

                try {
                    /* Parse the timestamp content, will throw if invalid */
                    OffsetDateTime offsetTime = OffsetDateTime.parse(contentToJSON.getString("sent"));
                    LocalDateTime time = offsetTime.toLocalDateTime();  
                    
                    /* Add new message to the database */
                    messageDatabase.setMessage(new WarningMessage(contentToJSON.getString("nickname"), contentToJSON.getDouble("latitude"), contentToJSON.getDouble("longitude"), contentToJSON.getString("dangertype"), time));
                } catch (DateTimeException | JSONException | SQLException e) {
                    code = 413;
                    System.out.println("Error: Problem with message content: " + e.getMessage());
                }
            }

            /* Done. Send response headers */
            System.out.println("Status: Got into end of POST; sending response");
            exchangeObject.sendResponseHeaders(code, -1);
            
        /* Handle GET case */
        // TODO: Implement get from database
        } else if (exchangeObject.getRequestMethod().equalsIgnoreCase("GET")) {
            System.out.println("Status: Got into GET handler branch");

            /* Check if there are any stored error messages */
            // TODO: Implement this checker

            /* Iterate through all stored messages */
            code = 200;
            //JSONArray responseMessages = new JSONArray();
            JSONObject responseObject;

            try {
                responseObject = messageDatabase.getMessages();
                /* Send the response */
                //String responseString = responseMessages.toString();
                String responseString = responseObject.toString();
                
                bytes = responseString.getBytes("UTF-8");
                System.out.println("Status: Sending GET response");
                exchangeObject.sendResponseHeaders(code, bytes.length);
                OutputStream outputStream = exchangeObject.getResponseBody();
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                System.out.println("Error occured while getting messages: " + e.getMessage());
            }

        /* Only POST and GET are supported, in any other case send a general error */
        } else {
            String responseString = errorStorage.append("Error: Requested function is not supported").toString();
            bytes = responseString.getBytes("UTF-8");
            exchangeObject.sendResponseHeaders(400, bytes.length);
            OutputStream outputStream = exchangeObject.getResponseBody();
            outputStream.write(bytes);
            /* Job done, clean the output stream */
            outputStream.flush();
            outputStream.close();
        }
    }

    private int checkContentIsValid(String content) {
        JSONObject contentToJSON = new JSONObject(content);

        if (contentToJSON.has("nickname") && !contentToJSON.isNull("nickname")) {
            if (contentToJSON.has("latitude") && !contentToJSON.isNull("latitude")) {
                if (contentToJSON.has("longitude") && !contentToJSON.isNull("longitude")) {
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

    /* Convert a WarninMessage to JSON Object */
    private JSONObject convertWarningToJSON(WarningMessage warningMessage) {
        StringBuilder body = new StringBuilder();

        body.append("{ \"nickname\":\"");
        body.append(warningMessage.getNickname());
        body.append("\", \"longitude\":\"");
        body.append(warningMessage.getLongitude());
        body.append("\", \"latitude\":\"");
        body.append(warningMessage.getLatitude());
        body.append("\", \"dangertype\":\"");
        body.append(warningMessage.getDangertype());
        body.append("\" }");

        JSONObject warning = new JSONObject(body.toString());
        return warning;
    }
}
