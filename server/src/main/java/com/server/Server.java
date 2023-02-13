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
            HttpContext httpContext = server.createContext("/warning", new WarningHandler());
            httpContext.setAuthenticator(userAuthenticator);
            // creates a default executor
            // create a context for registration
            server.createContext("/registration", new RegistrationHandler(userAuthenticator));
            server.setExecutor(null); 
            server.start();        
        } catch (FileNotFoundException e) {
            System.out.println("Error: Certificate not found");
        } catch (Exception e) {
            System.out.println("Error: Something went wrong while executing main function");
        }
    }
}