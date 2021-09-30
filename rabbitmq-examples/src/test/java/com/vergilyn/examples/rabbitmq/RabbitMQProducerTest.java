package com.vergilyn.examples.rabbitmq;

import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.constants.MessageModeEnum;
import com.vergilyn.examples.javabean.MessageDto;
import com.vergilyn.examples.rabbitmq.constants.RabbitDefinedEnum;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author VergiLyn
 * @date 2019-05-06
 */
@RunWith(SpringRunner.class)
// @SpringBootTest(classes = RabbitMQApplication.class)
public class RabbitMQProducerTest {
    private AmqpTemplate amqpTemplate;

    @Before
    public void before(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("127.0.0.1", 5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("123456");

        amqpTemplate = new RabbitTemplate(connectionFactory);
    }


    @Test
    public void anno(){
        MessageDto messageDto = MessageDto.Builder.newInstance()
                .id(1L)
                .rabbitMode(MessageModeEnum.RABBIT_ACK)
                .build();

        amqpTemplate.convertAndSend(RabbitDefinedEnum.ANNO.exchange, RabbitDefinedEnum.ANNO.routing,
                                    JSON.toJSONString(messageDto));
    }

    @Test
    public void xml(){
        MessageDto messageDto = MessageDto.Builder.newInstance()
                .id(2L)
                .rabbitMode(MessageModeEnum.RABBIT_ACK)
                .rabbitRequeue(true)
                .build();

        amqpTemplate.convertAndSend(RabbitDefinedEnum.XML.exchange, RabbitDefinedEnum.XML.routing, JSON.toJSONString(messageDto));
    }

    @Test
    public void delayMessage(){
        MessageDto messageDto = MessageDto.Builder.newInstance()
                .id(3L)
                .rabbitConsumerError(true)
                .build();

        amqpTemplate.convertAndSend(RabbitDefinedEnum.DELAY.exchange, RabbitDefinedEnum.DELAY.routing, JSON.toJSONString(messageDto));

    }

    @Test
    public void uniConcurrency(){
        int[] index = {0};

        Stream.generate(() -> {
                MessageDto body = MessageDto.Builder.newInstance()
                        .id(4L)
                        .integer(index[0])
                        .build();
                index[0] = index[0] + 1;

            return body;
        }).limit(10)
        .forEach(e -> {
            String body = JSON.toJSONString(e);
            System.out.println(body);
            amqpTemplate.convertAndSend(RabbitDefinedEnum.CONCURRENCY_UNI.exchange, RabbitDefinedEnum.CONCURRENCY_UNI.routing, body);
        });

    }
}
