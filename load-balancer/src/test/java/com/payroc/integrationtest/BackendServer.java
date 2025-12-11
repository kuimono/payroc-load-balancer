package com.payroc.integrationtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple backend server to handle requests.
 */
public class BackendServer {
    private final Logger logger = LoggerFactory.getLogger(BackendServer.class);
    private final String serverId;
    private final int port;

    public BackendServer(String hostAndPort) {
        String[] parts = hostAndPort.split(":");
        if (!"localhost".equals(parts[0])) {
            throw new IllegalArgumentException("Only localhost is supported");
        }
        this.port = Integer.parseInt(parts[1]);
        this.serverId = "backend-" + parts[1];
    }

    public String getServerId() {
        return serverId;
    }

    public void start() {
        logger.info("BackendServer " + serverId + " started.");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread.ofVirtual().name("client-handler-" + serverId).start(() -> handleClientSocket(clientSocket));
            }
        } catch (IOException e) {
            logger.error("BackendServer " + serverId + " failed at port " + port, e);
        }
    }

    private void handleClientSocket(Socket clientSocket){
        try(clientSocket;
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String request = in.readLine();
            logger.info("Received request: " + request);
            String response = "Response body from " + serverId;
            out.println(response);
        } catch (IOException e) {
            logger.error("Error handling request on BackendServer " + serverId, e);
            return;
        }
    }   
}
