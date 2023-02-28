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

/**
 * HttpHandler class that handles GET and POST requests.
 * HttpHandler object can receive JSON messages 
 * and store them into a database, it can also 
 * retrieve messages from the database and
 * send them to the client as a JSONArray
 */
public class WarningHandler implements HttpHandler {
    public WarningHandler() {
        /* Empty default constructor */
    }

    /**
     * Handle method for the custom HttpHandler class.
     * Designed to handle GET and POST requests regarding WarningMessages.
     * @param exchangeObject: Received from the client.
     */
    @Override
    public void handle(final HttpExchange exchangeObject) throws IOException {
        String content = "";
        JSONObject contentToJSON;
        int code = 0;
        byte [] bytes = null;
        final MessageDatabase messageDatabase = MessageDatabase.getInstance();
        final JsonChecker jsonChecker = new JsonChecker();

        System.out.println("Status: Request handled in thread " + Thread.currentThread().getId());

        /* Handle POST case */
        if (exchangeObject.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("Status: Got into POST handler branch");
            final InputStream inputStream = exchangeObject.getRequestBody();

            /* Parse content */
            try {
                content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                inputStream.close();
                System.out.println("Success: Warning message received");          
            } catch (final Exception e) {
                System.out.println("Error: Exception occured while processing input stream");
            }

            /* Make sure the warning is not empty */
            if (content == null || content.length() < 1) {
                System.out.println("Error: The content is invalid");
                code = 412;
            }

            /* Make sure the content is proper JSON */
            if (code == 0 && !jsonChecker.isJSONValid(content)) {
                code = 400 ;
            }

            /* Check if the content has all required information */
            if (code == 0) {
                if ((code = jsonChecker.checkContentIsValid(content)) == 200) {
                    contentToJSON = new JSONObject(content);
    
                    /* Check if the dangertype is in accepted format */
                    if ((code = jsonChecker.checkDangertype(contentToJSON)) == 200) {
                        try {
                            /* Parse the timestamp content, will throw if invalid */
                            final OffsetDateTime offsetTime = OffsetDateTime.parse(contentToJSON.getString("sent"));
                            final LocalDateTime time = offsetTime.toLocalDateTime();  
                            
                            /* Add new message to the database, use a correct constructor depending on the received parameters */
                            if (jsonChecker.checkJsonForAreaAndPhone(contentToJSON)) {
                                messageDatabase.setMessage(new WarningMessage(contentToJSON.getString("nickname"), contentToJSON.getDouble("latitude"), contentToJSON.getDouble("longitude"), contentToJSON.getString("dangertype"), time, contentToJSON.getString("areacode"), contentToJSON.getString("phonenumber")));
                            } else {
                                messageDatabase.setMessage(new WarningMessage(contentToJSON.getString("nickname"), contentToJSON.getDouble("latitude"), contentToJSON.getDouble("longitude"), contentToJSON.getString("dangertype"), time));
                            }
                        } catch (DateTimeException | JSONException | SQLException e) {
                            code = 413;
                            System.out.println("Error: Problem with message content: " + e.getMessage());
                        }
                    }
                }        
            }

            /* Done. Send response headers */
            System.out.println("Status: Got into end of POST; sending response");
            exchangeObject.sendResponseHeaders(code, -1);
            
        /* Handle GET case */
        } else if (exchangeObject.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                JSONArray responseArray;
                String responseString;
                responseArray = messageDatabase.getMessages();
                responseString = responseArray.toString();

                /* Send the response */
                code = 200;
                bytes = responseString.getBytes("UTF-8");
                System.out.println("Status: Sending GET response");
                exchangeObject.sendResponseHeaders(code, bytes.length);
                final OutputStream outputStream = exchangeObject.getResponseBody();
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            } catch (final Exception e) {
                System.out.println("Error occured while getting messages: " + e.getMessage());
            }

        /* Only POST and GET are supported, in any other case send a general error */
        } else {
            final String responseString = "Error: Requested function is not supported";
            bytes = responseString.getBytes("UTF-8");
            exchangeObject.sendResponseHeaders(400, bytes.length);
            final OutputStream outputStream = exchangeObject.getResponseBody();
            outputStream.write(bytes);

            /* Done, clean the output stream */
            outputStream.flush();
            outputStream.close();
        }
    }
}