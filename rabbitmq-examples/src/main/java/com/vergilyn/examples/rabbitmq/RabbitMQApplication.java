package com.vergilyn.examples.rabbitmq;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @date 2018/9/14
 */
@SpringBootApplication
@EnableRabbit
public class RabbitMQApplication implements CommandLineRunner {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RabbitMQApplication.class);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
