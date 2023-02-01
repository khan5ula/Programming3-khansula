package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class RegistrationHandler implements HttpHandler {
    StringBuilder inputStorage = new StringBuilder("");
    StringBuilder errorStorage = new StringBuilder("");
    UserAuthenticator userAuthenticator = null;

    public RegistrationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                InputStream inputStream = t.getRequestBody();
                String requestBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                this.inputStorage.append(requestBody);
                inputStream.close();
                if (parseUserInformation(this.inputStorage)) {
                    String responseString = "New user added successfully";
                    byte [] bytes = responseString.getBytes("UTF-8");
                    t.sendResponseHeaders(200, bytes.length);
                    OutputStream outputStream = t.getResponseBody();
                    outputStream.write(bytes);
                    outputStream.flush();
                    outputStream.close();
                } else {
                    String responseString = errorStorage.append("Adding new user failed: Username not available").toString();
                    byte [] bytes = responseString.getBytes("UTF-8");
                    t.sendResponseHeaders(403, bytes.length);
                    OutputStream outputStream = t.getResponseBody();
                    outputStream.write(bytes);
                    outputStream.flush();
                    outputStream.close();

                }
            } else {
                String responseString = errorStorage.append("Error: Not supported").toString();
                byte [] bytes = responseString.getBytes("UTF-8");
                t.sendResponseHeaders(400, bytes.length);
                OutputStream outputStream = t.getResponseBody();
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            System.out.println("Error: IOEXception occured when handling the client's request");
            e.printStackTrace();
        }
        inputStorage.setLength(0);
        errorStorage.setLength(0);
    }

    private boolean parseUserInformation(StringBuilder input) {
        String userNameAndPassword = input.toString();
        try {
            String [] userInformation = userNameAndPassword.split(":", 2);
            if (userInformation[0].length() > 1 && userInformation[1].length() > 1) {
                if (this.userAuthenticator.addUser(userInformation[0], userInformation[1])) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid username or password data");
            System.out.println(e.getMessage());
        }
        return false;
    }
}
