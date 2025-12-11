package com.payroc.config;

import org.yaml.snakeyaml.Yaml;

import com.payroc.Main;

import lombok.Data;

@Data
public class Config {
    private LoadBalancerConfig loadBalancer;

    public static Config load(String resourceFileName) {
        Yaml yaml = new Yaml();
        try (var inputStream = Main.class.getClassLoader().getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Configuration file not found");
            }
            return yaml.loadAs(inputStream, Config.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}
