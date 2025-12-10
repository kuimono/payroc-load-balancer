package com.payroc;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payroc.backend.BackendServer;
import com.payroc.client.Client;
import com.payroc.loadbalancer.LoadBalancer;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // configurations
        List<Integer> backendPorts = List.of(20001, 20002, 20003, 20004);
        int lbPort = 20005;

        // create service instances
        List<BackendServer> backendServers = backendPorts.stream()
            .map(port -> new BackendServer("backend-" + port, port))
            .toList();
        LoadBalancer loadBalancer = new LoadBalancer(lbPort, backendPorts);

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

        // pause and send test messages
        try {
            Thread.sleep(Duration.ofMillis(2000));
        } catch (InterruptedException e) {
            logger.error("Main thread interrupted", e);
        }
        testSendMessage(lbPort);

        joinThread(lbThread);
        backendThreads.forEach(Main::joinThread);
    }

    private static void testSendMessage(int port) {
        var clientThreads = IntStream.range(0, 5)
            .mapToObj(i -> Thread.ofVirtual()
                .name("client-" + i)
                .unstarted(() -> Client.sendMessage("client-" + i, port, 3)))
            .toList();
        clientThreads.forEach(Thread::start);
        clientThreads.forEach(Main::joinThread);
    }

    private static void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("Thread " + thread.getName() + " interrupted", e);
        }
    }
}