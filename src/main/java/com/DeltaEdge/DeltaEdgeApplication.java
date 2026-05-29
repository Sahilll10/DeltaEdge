package com.DeltaEdge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DeltaEdgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeltaEdgeApplication.class, args);
    }
}