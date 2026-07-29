package com.flowforge.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.flowforge")
@EnableScheduling
public class FlowForgeWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowForgeWorkerApplication.class, args);
    }
}
