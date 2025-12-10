package com.payroc.loadbalancer;

import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendHealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(BackendHealthCheck.class);

    private final List<String> backendHostAndPorts;
    private final Function<String, Boolean> healthCheckFunction;
    private final List<Consumer<List<String>>> healthyBackendsCallbacks;

    public BackendHealthCheck(
        List<String> backendHostAndPorts,
        List<Consumer<List<String>>> healthyBackendsCallbacks
    ) {
        this(backendHostAndPorts, BackendHealthCheck::isBackendHealthy, healthyBackendsCallbacks);
    }

    protected BackendHealthCheck(
        List<String> backendHostAndPorts,
        Function<String, Boolean> healthCheckFunction,
        List<Consumer<List<String>>> healthyBackendsCallbacks   // for testing purposes
    ) {
        this.backendHostAndPorts = backendHostAndPorts;
        this.healthCheckFunction = healthCheckFunction;
        this.healthyBackendsCallbacks = healthyBackendsCallbacks;
    }

    public void startHealthCheck() {
        while(true) {
            performHealthCheck();
            try {
                Thread.sleep(20000); // wait for 20 seconds before next check
            } catch (InterruptedException e) {
                logger.error("Health check interrupted", e);
                break;
            }
        }
    }

    protected void performHealthCheck() {
        List<String> healthyBackends = backendHostAndPorts.stream()
            .filter(hostAndPort -> healthCheckFunction.apply(hostAndPort))
            .toList();
        healthyBackendsCallbacks.forEach(callback -> callback.accept(healthyBackends));
    }

    private static boolean isBackendHealthy(String backend) {
        String[] parts = backend.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        try (var socket = new Socket(host, port)) {
            logger.info("Backend " + backend + " is healthy.");
            return true;
        } catch (Exception e) {
            logger.warn("Backend " + backend + " is unhealthy: " + e.getMessage());
            return false;
        }
    }
}
