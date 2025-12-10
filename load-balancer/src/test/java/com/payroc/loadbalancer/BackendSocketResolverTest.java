package com.payroc.loadbalancer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BackendSocketResolverTest {
    private static final List<String> BACKEND_HOST_AND_PORTS = List.of(
        "localhost:20001",
        "localhost:20002",
        "localhost:20003",
        "localhost:20004"
    );
    private BackendSocketResolver resolver;

    @BeforeEach
    public void setup() {
        resolver = new BackendSocketResolver();
        resolver.updateHealthyBackends(BACKEND_HOST_AND_PORTS);
    }

    @Test
    public void testResolveBackendHostAndPort() throws Exception {
        Map<String, Integer> counter = new HashMap<>();

        int repetitions = 10000;
        for (int i = 0; i < repetitions; i++) {
            String hostAndPort = resolver.resolveBackendHostAndPort();
            assertTrue(BACKEND_HOST_AND_PORTS.contains(hostAndPort), "Resolved host and port not in the list");

            counter.put(hostAndPort, counter.getOrDefault(hostAndPort, 0) + 1);
        }

        for (String hostAndPort : BACKEND_HOST_AND_PORTS) {
            int count = counter.getOrDefault(hostAndPort, 0);
            double ratio = (double) count / repetitions;
            // each backend should be selected roughly equally often
            assertTrue(ratio > 0.2 && ratio < 0.3, "Backend " + hostAndPort + " selected too few or too many times: " + ratio);
        }
    }
}
