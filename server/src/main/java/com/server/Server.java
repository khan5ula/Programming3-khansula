package com.server;

import com.sun.net.httpserver.HttpsServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsParameters;

import java.util.concurrent.Executors;


public class Server {
    StringBuilder textStorage = new StringBuilder("");

    private Server() {
        // nothing here
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
            /* Create a database instance */
            MessageDatabase messageDatabase = MessageDatabase.getInstance();

            /* Create HTTP server to port 8001 with default logger */
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);

            /* Use self-signed SSL certificate */
            SSLContext sslContext = serverSSLContext();

            /* Configure HttpsServer to use sslContext */
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure (HttpsParameters params) {
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
            }   
            });

            /* Create User Authenticator instance */
            UserAuthenticator userAuthenticator = new UserAuthenticator();

            /* Create context for warning and registration services*/
            HttpContext httpContext = server.createContext("/warning", new WarningHandler());
            httpContext.setAuthenticator(userAuthenticator);
            server.createContext("/registration", new RegistrationHandler());

            /* Enable support for multi-threading and start the server */
            server.setExecutor(Executors.newCachedThreadPool()); 
            server.start();

            /* Initialize database connection */
            try {
                messageDatabase.open("messages.db");
            } catch (Exception e) {
                System.out.println("Error occured while the server tried to open a database connection: " + e.getMessage());
            } finally {
                messageDatabase.closeDB();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: Certificate not found");
        } catch (Exception e) {
            System.out.println("Error: Something went wrong while executing main function");
        }
    }
}
