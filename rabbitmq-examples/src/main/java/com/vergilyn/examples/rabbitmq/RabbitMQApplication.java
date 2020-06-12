package com.vergilyn.examples.rabbitmq;

import java.time.LocalTime;
import java.util.TimerTask;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.vergilyn.examples.rabbitmq.constants.RabbitDefinedEnum.BATCH_GET_ACK;

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

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String now = LocalTime.now().toString();
                rabbitTemplate.convertAndSend(
                        BATCH_GET_ACK.exchange,
                        BATCH_GET_ACK.routing,
                        now);
                System.out.println(BATCH_GET_ACK.exchange + " send >>>> " + now);
            }
        };

        // new Timer().schedule(task, 0, 4_000);
    }
}
