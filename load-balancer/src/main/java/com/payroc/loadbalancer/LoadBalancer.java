package com.payroc.loadbalancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        } catch (IOException | InterruptedException e) {
            logger.error("Error handling client socket", e);
        }
    }

    private void redirectToBackend(Socket clientSocket, Socket backendSocket) throws IOException, InterruptedException {
        try(clientSocket;
            backendSocket;
            InputStream clientInput = clientSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream();
            InputStream backendInput = backendSocket.getInputStream();
            OutputStream backendOutput = backendSocket.getOutputStream();
        ) {
            Thread requestThread = Thread.ofVirtual().name("client-to-backend").start(() -> {
                try {
                    clientInput.transferTo(backendOutput);
                } catch (IOException e) {
                    logger.error("Error redirecting from client to backend", e);
                }
            });
            Thread responseThread = Thread.ofVirtual().name("backend-to-client").start(() -> {
                try {
                    backendInput.transferTo(clientOutput);
                } catch (IOException e) {
                    logger.error("Error redirecting from backend to client", e);
                }
            });
            requestThread.join();
            responseThread.join();
        }
    }
}
