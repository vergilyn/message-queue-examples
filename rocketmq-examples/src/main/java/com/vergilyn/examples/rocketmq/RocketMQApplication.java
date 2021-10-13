package com.vergilyn.examples.rocketmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RocketMQApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RocketMQApplication.class);
        application.run(args);
    }
}
