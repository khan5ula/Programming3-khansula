package com.server;

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;


public class Server implements HttpHandler {
    StringBuilder textStorage = new StringBuilder("");

    private Server() {
        // nothing here
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream inputStream = t.getRequestBody();
            String requestBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            this.textStorage.append(requestBody);
            inputStream.close();
            t.sendResponseHeaders(200, -1);
        } else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            String responseString = textStorage.toString();
            byte [] bytes = responseString.getBytes("UTF-8");
            t.sendResponseHeaders(200, bytes.length);
        } else {
            String responseString = "Error: Not supported";
            byte [] bytes = responseString.getBytes("UTF-8");
            t.sendResponseHeaders(200, bytes.length);
        }
    }


    public static void main(String[] args) throws Exception {
        //create the http server to port 8001 with default logger
        HttpServer server = HttpServer.create(new InetSocketAddress(8001),0);
        //create context that defines path for the resource, in this case a "help"
        server.createContext("/help", new Server());
        // creates a default executor
        server.setExecutor(null); 
        server.start(); 
    }
}