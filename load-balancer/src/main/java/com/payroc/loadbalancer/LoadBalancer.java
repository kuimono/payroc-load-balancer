package com.payroc.loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalancer {
    private final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);
    private final int port;
    private final BackendSocketResolver backendSocketResolver;

    public LoadBalancer(int port, BackendSocketResolver backendSocketResolver) {
        this.port = port;
        this.backendSocketResolver = backendSocketResolver;
    }

    public void start() {
        logger.info("Load Balancer started.");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread.ofVirtual().name("client-handler").start(() -> handleClientSocket(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Load balancer failed at port " + port, e);
        }
    }

    private void handleClientSocket(Socket clientSocket) {
        try {
            String backendHostAndPort = backendSocketResolver.resolveBackendHostAndPort();
            String[] parts = backendHostAndPort.split(":");
            Socket backendSocket = new Socket(parts[0], Integer.parseInt(parts[1]));
            redirectToBackend(clientSocket, backendSocket);
        } catch (IOException e) {
            logger.error("Error handling client socket", e);
        }
    }

    private void redirectToBackend(Socket clientSocket, Socket backendSocket) throws IOException {
        try(clientSocket;
            backendSocket;
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter newOut = new PrintWriter(backendSocket.getOutputStream(), true);
            BufferedReader newIn = new BufferedReader(new InputStreamReader(backendSocket.getInputStream()));
        ) {
            // here we assume the request and response are single-line messages
            // TODO allow parallel transfer of data between clientSocket and newSocket
            String request = in.readLine();
            newOut.println(request);
            String response = newIn.readLine();
            out.println(response);
        }
    }
}
