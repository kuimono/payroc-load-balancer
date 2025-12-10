package com.payroc.loadbalancer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class BackendSocketResolver {
    List<Integer> backendPorts;

    public BackendSocketResolver(List<Integer> backendPorts) {
        this.backendPorts = backendPorts;
    }

    public Socket resolveBackendSocket() throws UnknownHostException, IOException {
        // randomly select a backend port
        int randomIndex = (int) (Math.random() * backendPorts.size());
        return new Socket("localhost", backendPorts.get(randomIndex));
    }

}
