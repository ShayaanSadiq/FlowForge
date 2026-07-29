package com.flowforge.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.flowforge")
public class FlowForgeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowForgeApiApplication.class, args);
    }
}
