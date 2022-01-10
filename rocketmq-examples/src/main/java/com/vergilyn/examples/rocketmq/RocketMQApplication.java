package com.vergilyn.examples.rocketmq;

import org.apache.rocketmq.client.ClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RocketMQApplication {

    static {
        /**
         * @see ClientConfig#instanceName
         * @see ClientConfig#changeInstanceNameToPID()
         */
        System.setProperty("rocketmq.client.name", "vergilyn-rocketmq-client");
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RocketMQApplication.class);
        application.run(args);
    }
}
