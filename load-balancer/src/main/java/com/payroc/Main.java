package com.payroc;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payroc.loadbalancer.BackendHealthCheck;
import com.payroc.loadbalancer.BackendSocketResolver;
import com.payroc.loadbalancer.LoadBalancer;

/**
 * The main entry point for the load balancer application.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // configurations
        List<String> backendHostAndPorts = List.of(
            "localhost:20001",
            "localhost:20002",
            "localhost:20003",
            "localhost:20004"
        );
        int lbPort = 20005;

        // create service instances
        BackendSocketResolver backendSocketResolver = new BackendSocketResolver();
        BackendHealthCheck backendHealthCheck = new BackendHealthCheck(
            backendHostAndPorts,
            List.of(backendSocketResolver::updateHealthyBackends)
        );
        LoadBalancer loadBalancer = new LoadBalancer(lbPort, backendSocketResolver);

        // start services
        Thread lbThread = Thread.ofVirtual()
            .name("load-balancer")
            .unstarted(() -> loadBalancer.start());
        lbThread.start();
        Thread healthCheckThread = Thread.ofVirtual()
            .name("backend-health-check")
            .unstarted(() -> backendHealthCheck.startHealthCheck());
        healthCheckThread.start();

        joinThread(healthCheckThread);
        joinThread(lbThread);
    }

    private static void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("Thread " + thread.getName() + " interrupted", e);
        }
    }
}