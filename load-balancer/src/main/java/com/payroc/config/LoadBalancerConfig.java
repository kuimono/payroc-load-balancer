package com.payroc.config;

import java.util.List;

import lombok.Data;

@Data
public class LoadBalancerConfig {
    private int port;
    private List<String> backendHostAndPorts;
}
