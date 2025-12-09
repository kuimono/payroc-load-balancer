package com.payroc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.stream.IntStream;

public class ClientMain {
    public static void main(String[] args) {
        int port = 20006;
        var threads = IntStream.range(0, 5)
            .mapToObj(i -> Thread.ofVirtual()
                .name("client-" + i)
                .unstarted(() -> sendMessage("client-" + i, port)))
            .toList();
        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    private static void sendMessage(String threadName, int port) {
        for (int i = 0; i < 3; i++) {
            try(
                Socket clientSocket = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                out.println("Knock Knock from " + threadName + " - " + i);
                String response = in.readLine();
                System.out.println("Received response: " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
