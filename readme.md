# Simple layer-4 load balancer

## Features

- Accept traffic from multiple clients
- Balance traffic across multiple backend services
- Health checks on backend services

## Assumptions

- Although the problem statement suggests it's 1999, the Java version during the time (J2SE 1.2) was too old and broken. Therefore we are using Java v21 for coding.

## Major components

- `LoadBalancer.java`: The "server" class to receive connections from clients and redirect the traffic to backend services.
- `BackendSocketResolver.java`: The class to determine which backend service the traffic should redirect to.
  - Using random allocation to balance the traffic
- `BackendHealthCheck.java`: The class to proivde health checks of backend services and update their health status.
- `Main.java`: The entrypoint of the system, which will start the corresponding processes (using virtual threads) of load balancer and health check.

## Testing

- JUnit tests of `BackendSocketResolver.java` and `BackendHealthCheck.java`
- Integration test
  - Create dummy backend servers to receive the traffic
  - Start load balancer and health check logics
  - Send messages thru clients and see if it can reach to backends properly.
  - Kill one of the backend service
  - Send messages thru clients again, and check if all requests are routed to healthy backends properly.
- For the result logs please refer to `integrationTest_result.log`

