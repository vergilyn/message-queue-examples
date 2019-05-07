package com.vergilyn.examples.rabbitmq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @date 2018/9/14
 */
@SpringBootApplication
public class RabbitMQApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RabbitMQApplication.class);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
