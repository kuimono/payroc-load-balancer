package com.payroc.loadbalancer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class BackendSocketResolver {
    List<String> backendHostAndPorts;

    public BackendSocketResolver(List<String> backendHostAndPorts) {
        this.backendHostAndPorts = backendHostAndPorts;
    }

    public Socket resolveBackendSocket() throws UnknownHostException, IOException {
        // randomly select a backend port
        String backendHostAndPort = resolveBackendHostAndPort();
        String[] parts = backendHostAndPort.split(":");
        return new Socket(parts[0], Integer.parseInt(parts[1]));
    }

    protected String resolveBackendHostAndPort() {
        int randomIndex = (int) (Math.random() * backendHostAndPorts.size());
        return backendHostAndPorts.get(randomIndex);
    }

}
