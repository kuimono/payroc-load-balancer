package com.payroc.loadbalancer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class BackendSocketResolverTest {
    @Test
    public void testResolveBackendHostAndPort() throws Exception {
        var backendHostAndPorts = List.of(
            "localhost:20001",
            "localhost:20002",
            "localhost:20003",
            "localhost:20004"
        );
        var resolver = new BackendSocketResolver(backendHostAndPorts);

        Map<String, Integer> counter = new HashMap<>();

        int repetitions = 10000;
        for (int i = 0; i < repetitions; i++) {
            String hostAndPort = resolver.resolveBackendHostAndPort();
            assertTrue(backendHostAndPorts.contains(hostAndPort), "Resolved host and port not in the list");

            counter.put(hostAndPort, counter.getOrDefault(hostAndPort, 0) + 1);
        }

        for (String hostAndPort : backendHostAndPorts) {
            int count = counter.getOrDefault(hostAndPort, 0);
            double ratio = (double) count / repetitions;
            // each backend should be selected roughly equally often
            assertTrue(ratio > 0.2 && ratio < 0.3, "Backend " + hostAndPort + " selected too few or too many times: " + ratio);
        }
    }
}
