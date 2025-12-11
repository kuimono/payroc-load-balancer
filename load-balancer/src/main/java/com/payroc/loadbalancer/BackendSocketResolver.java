package com.payroc.loadbalancer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves backend sockets for the load balancer.
 */
public class BackendSocketResolver {
    private final Logger logger = LoggerFactory.getLogger(BackendSocketResolver.class);

    private List<String> healthyHostAndPorts;

    public BackendSocketResolver() {
        this.healthyHostAndPorts = List.of();
    }

    /**
     * Updates the list of healthy backend host and ports.
     * This method is typically called by the health check component.
     * @param healthyHostAndPorts the list of healthy backend host and ports
     */
    public void updateHealthyBackends(List<String> healthyHostAndPorts) {
        logger.info("Updating healthy backends: " + healthyHostAndPorts);
        this.healthyHostAndPorts = healthyHostAndPorts;
    }

    /**
     * Resolves a backend host and port from the list of healthy backends.
     * @return a backend host and port in the format "host:port"
     */
    public String resolveBackendHostAndPort() {
        // since healthyHostAndPorts can be updated asynchronously, we take a snapshot of the reference
        var healthyHostAndPorts = this.healthyHostAndPorts;

        if (healthyHostAndPorts.isEmpty()) {
            throw new IllegalStateException("No healthy backends available");
        }

        int randomIndex = (int) (Math.random() * healthyHostAndPorts.size());
        return healthyHostAndPorts.get(randomIndex);
    }

}
