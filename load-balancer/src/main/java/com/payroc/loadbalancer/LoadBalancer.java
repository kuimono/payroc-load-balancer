package com.payroc.loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalancer {
    private final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);
    private final int port;
    private final List<Integer> backendPorts;

    public LoadBalancer(int port, List<Integer> backendPorts) {
        this.port = port;
        this.backendPorts = backendPorts;
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

    private void handleClientSocket(Socket clientSocket){
        int backendPort = resolveBackendPort();
        redirectToBackend(clientSocket, backendPort);
    }

    private int resolveBackendPort() {
        // TODO implement load balancing algorithm to select backend port
        return backendPorts.get(0);
    }

    private void redirectToBackend(Socket clientSocket, int backendPort) {
        try(clientSocket;
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Socket newSocket = new Socket("localhost", backendPort);
            PrintWriter newOut = new PrintWriter(newSocket.getOutputStream(), true);
            BufferedReader newIn = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
        ) {
            // TODO allow parallel transfer of data between clientSocket and newSocket
            String request = in.readLine();
            newOut.println(request);
            String response = newIn.readLine();
            out.println(response);
        } catch (Exception e) {
            logger.error("Error in redirectToBackend", e);
            return;
        }
    }
}
