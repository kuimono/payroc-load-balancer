package com.payroc.loadbalancer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class BackendSocketResolver {
    private List<String> healthyHostAndPorts;

    public BackendSocketResolver() {
        this.healthyHostAndPorts = List.of();
    }

    public void updateHealthyBackends(List<String> healthyHostAndPorts) {
        this.healthyHostAndPorts = healthyHostAndPorts;
    }

    public Socket resolveBackendSocket() throws UnknownHostException, IOException {
        // randomly select a backend port
        String backendHostAndPort = resolveBackendHostAndPort();
        String[] parts = backendHostAndPort.split(":");
        return new Socket(parts[0], Integer.parseInt(parts[1]));
    }

    protected String resolveBackendHostAndPort() {
        if (healthyHostAndPorts.isEmpty()) {
            throw new IllegalStateException("No healthy backends available");
        }

        int randomIndex = (int) (Math.random() * healthyHostAndPorts.size());
        return healthyHostAndPorts.get(randomIndex);
    }

}
