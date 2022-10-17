package com.vergilyn.examples.rocketmq.order;

import org.apache.rocketmq.client.ClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RocketMQOrderConsumerApplication {

    static {
        /**
         * @see ClientConfig#instanceName
         * @see ClientConfig#changeInstanceNameToPID()
         */
        System.setProperty("rocketmq.client.name", "vergilyn-rocketmq-client");
    }

    public static void main(String[] args) {
        System.setProperty("rocketmq.nameServer", OrderConsumerConstants.NAMESRV_ADDR);

        SpringApplication application = new SpringApplication(RocketMQOrderConsumerApplication.class);
        application.run(args);
    }
}
