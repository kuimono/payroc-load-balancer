package com.payroc;

public class Main {
    public static void main(String[] args) {
        int port = 20006;
        LoadBalancer loadBalancer = new LoadBalancer(port);
        loadBalancer.start();
    }
}