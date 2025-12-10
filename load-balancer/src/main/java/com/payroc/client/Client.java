package com.payroc.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple client to send messages to the load balancer.
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void sendMessage(String clientName, int port, int repeats) {
        for (int i = 0; i < repeats; i++) {
            try(
                Socket clientSocket = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                out.println("Request body from " + clientName + " - " + i);
                String response = in.readLine();
                logger.info("Received response: " + response);
            } catch (Exception e) {
                logger.error("Error sending message", e);
            }
        }
    }
}
