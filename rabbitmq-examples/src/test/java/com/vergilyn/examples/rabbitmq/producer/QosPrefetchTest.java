package com.vergilyn.examples.rabbitmq.producer;

import java.time.LocalTime;

import com.vergilyn.examples.rabbitmq.AbstractSpringbootTest;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import static com.vergilyn.examples.rabbitmq.constants.RabbitAutoDeclareEnum.QOS_PREFETCH;

/**
 * @author vergilyn
 * @date 2020-06-17
 */
public class QosPrefetchTest extends AbstractSpringbootTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void send(){
        for (int i = 0; i < 15; i++){
            String msg = LocalTime.now().toString();
            rabbitTemplate.convertAndSend(QOS_PREFETCH.exchange, QOS_PREFETCH.routing, msg);
            System.out.printf("[provider][%02d]: %s \r\n", i, msg);
        }

        preventExit();
    }

}
