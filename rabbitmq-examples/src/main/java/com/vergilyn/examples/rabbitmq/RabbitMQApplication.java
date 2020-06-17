package com.vergilyn.examples.rabbitmq;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author VergiLyn
 * @date 2018/9/14
 */
@SpringBootApplication
@EnableRabbit
@EnableScheduling
public class RabbitMQApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RabbitMQApplication.class);
        app.run(args);
    }
}
