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

public class WarningHandler implements HttpHandler {
    StringBuilder inputStorage = new StringBuilder("");
    StringBuilder errorStorage = new StringBuilder("");

    public WarningHandler() {
        // nothing here
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            /* If the user wants to POST an error, read the error message from HttpExchange request body and store it to the class variable inputStorage */
            if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                /* If previous error exists, parse the new error to a new line */
                if (inputStorage.toString().length() > 0) {
                    inputStorage.append("\n");
                }
                InputStream inputStream = t.getRequestBody();
                String requestBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                this.inputStorage.append(requestBody);
                /* Job done, clean the input stream and send success code */
                inputStream.close();
                t.sendResponseHeaders(200, -1);
            } else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
                /* If the user wants to GET posted errors, read the stored messages from inputStorage class variable and post them to the output stream */
                String responseString = inputStorage.toString();
                byte [] bytes = responseString.getBytes("UTF-8");
                t.sendResponseHeaders(200, bytes.length);
                OutputStream outputStream = t.getResponseBody();
                outputStream.write(bytes);
                /* Job done, clean the output stream */
                outputStream.flush();
                outputStream.close();
            } else {
                /* Only POST and GET are supported, in any other case send a general error */
                String responseString = errorStorage.append("Error: Requested function is not supported").toString();
                byte [] bytes = responseString.getBytes("UTF-8");
                t.sendResponseHeaders(400, bytes.length);
                OutputStream outputStream = t.getResponseBody();
                outputStream.write(bytes);
                /* Job done, clean the output stream */
                outputStream.flush();
                outputStream.close();
            }
            /* Flush Error storage so old error messages won't flood the future output to the user */
            errorStorage.setLength(0);
        } catch (IOException e) {
            System.out.println("Error: IOEXception occured when handling the client's request");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error: Error occured during warning handling");
            e.printStackTrace();
        }
    }
}
