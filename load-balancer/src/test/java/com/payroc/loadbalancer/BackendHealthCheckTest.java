package com.payroc.loadbalancer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

public class BackendHealthCheckTest {
    private static final List<String> BACKEND_HOST_AND_PORTS = List.of(
        "localhost:20001",
        "localhost:20002",
        "localhost:20003",
        "localhost:20004"
    );

    private List<String> tmpHealthyBackends;
    private Consumer<List<String>> healthyBackendsCallback = (healthyBackends) -> {
        tmpHealthyBackends = healthyBackends;
    };

    @Test
    public void testPerformHealthCheck() {
        BackendHealthCheck backendHealthCheck = new BackendHealthCheck(
            BACKEND_HOST_AND_PORTS,
            (backendHostAndPort) -> true,
            List.of(healthyBackendsCallback)
        );

        backendHealthCheck.performHealthCheck();
        assertEquals(BACKEND_HOST_AND_PORTS, tmpHealthyBackends);
    }

    @Test
    public void testPerformHealthCheck_SomeUnhealthy() {
        BackendHealthCheck backendHealthCheck = new BackendHealthCheck(
            BACKEND_HOST_AND_PORTS,
            (backendHostAndPort) -> backendHostAndPort.endsWith("20002") || backendHostAndPort.endsWith("20004"),
            List.of(healthyBackendsCallback)
        );
        backendHealthCheck.performHealthCheck();
        assertEquals(List.of("localhost:20002", "localhost:20004"), tmpHealthyBackends);
    }
}
