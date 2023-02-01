package com.server;

import com.sun.net.httpserver.HttpsServer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsParameters;


public class Server implements HttpHandler {
    StringBuilder textStorage = new StringBuilder("");

    private Server() {
        // nothing here
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
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
                OutputStream outputStream = t.getResponseBody();
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            } else {
                String responseString = "Error: Not supported";
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
    }

    private static SSLContext serverSSLContext() throws Exception {
        /* Create a server context using a self-signed certificate */
        char[] passphrase = "password".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore.jks"), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

    public static void main(String[] args) throws Exception {
        try {
            //create the http server to port 8001 with default logger
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
            // use self-signed SSL sertificate
            SSLContext sslContext = serverSSLContext();
            // configuring the HttpsServer to use the sslContext
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure (HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
            }   
            });
            // Create User Authenticator instance
            UserAuthenticator userAuthenticator = new UserAuthenticator();
            //create context that defines path for the resource, in this case a "help"
            HttpContext httpContext = server.createContext("/warning", new Server());
            httpContext.setAuthenticator(userAuthenticator);
            // creates a default executor
            // create a context for registration
            server.createContext("/registration", new RegistrationHandler(userAuthenticator));
            server.setExecutor(null); 
            server.start();        
        } catch (FileNotFoundException e) {
            System.out.println("Error: Certificate not found");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error: Something went wrong while executing main function");
            e.printStackTrace();
        }
    }
}