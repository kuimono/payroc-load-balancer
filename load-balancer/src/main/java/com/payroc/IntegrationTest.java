package com.payroc;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payroc.backend.BackendServer;
import com.payroc.client.Client;
import com.payroc.loadbalancer.BackendHealthCheck;
import com.payroc.loadbalancer.BackendSocketResolver;
import com.payroc.loadbalancer.LoadBalancer;

/**
 * An integration test that starts multiple backend servers and a load balancer,
 * then sends test messages through the load balancer to verify end-to-end functionality.
 */
public class IntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

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
        List<BackendServer> backendServers = backendHostAndPorts.stream()
            .map(hostAndPort -> new BackendServer(hostAndPort))
            .toList();
        BackendSocketResolver backendSocketResolver = new BackendSocketResolver();
        BackendHealthCheck backendHealthCheck = new BackendHealthCheck(
            backendHostAndPorts,
            List.of(backendSocketResolver::updateHealthyBackends)
        );
        LoadBalancer loadBalancer = new LoadBalancer(lbPort, backendSocketResolver);

        // start services
        List<Thread> backendThreads = backendServers.stream()
            .map(backendServer -> Thread.ofVirtual()
                .name(backendServer.getServerId())
                .unstarted(() -> backendServer.start()))
            .toList();
        backendThreads.forEach(Thread::start);

        Thread lbThread = Thread.ofVirtual()
            .name("load-balancer")
            .unstarted(() -> loadBalancer.start());
        lbThread.start();
        Thread healthCheckThread = Thread.ofVirtual()
            .name("backend-health-check")
            .unstarted(() -> backendHealthCheck.startHealthCheck());
        healthCheckThread.start();

        // pause and send test messages
        pause(Duration.ofMillis(2000));
        testSendMessage(lbPort);

        // pause and kill one of the backend servers
        pause(Duration.ofMillis(60000));
        backendThreads.get(2).interrupt();

        // pause and send test messages again
        pause(Duration.ofMillis(30000));
        testSendMessage(lbPort);

        joinThread(healthCheckThread);
        joinThread(lbThread);
        backendThreads.forEach(IntegrationTest::joinThread);
    }

    private static void testSendMessage(int port) {
        var clientThreads = IntStream.range(0, 5)
            .mapToObj(i -> Thread.ofVirtual()
                .name("client-" + i)
                .unstarted(() -> Client.sendMessage("client-" + i, port, 3)))
            .toList();
        clientThreads.forEach(Thread::start);
        clientThreads.forEach(IntegrationTest::joinThread);
    }

    private static void pause(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            logger.error("Pause interrupted", e);
        }
    }   

    private static void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("Thread " + thread.getName() + " interrupted", e);
        }
    }
}