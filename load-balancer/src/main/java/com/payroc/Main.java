package com.payroc;

import java.time.Duration;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payroc.client.Client;
import com.payroc.loadbalancer.LoadBalancer;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        int port = 20006;
        Thread lbThread = Thread.ofVirtual()
            .name("load-balancer")
            .unstarted(() -> startLoadBalancer(port));
        lbThread.start();

        // pause for test
        try {
            Thread.sleep(Duration.ofMillis(2000));
        } catch (InterruptedException e) {
            logger.error("Main thread interrupted", e);
        }

        testSendMessage(port);

        joinThread(lbThread);
    }

    private static void startLoadBalancer(int port) {
        LoadBalancer loadBalancer = new LoadBalancer(port);
        loadBalancer.start();
    }

    private static void testSendMessage(int port) {
        var clientThreads = IntStream.range(0, 5)
            .mapToObj(i -> Thread.ofVirtual()
                .name("client-" + i)
                .unstarted(() -> Client.sendMessage("client-" + i, port)))
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