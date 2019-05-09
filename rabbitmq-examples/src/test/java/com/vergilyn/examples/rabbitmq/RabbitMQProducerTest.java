package com.vergilyn.examples.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.constants.MessageModeEnum;
import com.vergilyn.examples.javabean.MessageDto;
import com.vergilyn.examples.rabbitmq.constants.RabbitMQConstants;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author VergiLyn
 * @date 2019-05-06
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RabbitMQApplication.class)
public class RabbitMQProducerTest {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void anno(){
        MessageDto messageDto = MessageDto.Builder.newInstance()
                .id(1L)
                .rabbitMode(MessageModeEnum.RABBIT_ACK)
                .build();

        amqpTemplate.convertAndSend(RabbitMQConstants.ANNO.exchange, RabbitMQConstants.ANNO.routing, JSON.toJSONString(messageDto));
    }

    @Test
    public void xml(){
        MessageDto messageDto = MessageDto.Builder.newInstance()
                .id(2L)
                .rabbitMode(MessageModeEnum.RABBIT_ACK)
                .rabbitRequeue(true)
                .build();

        amqpTemplate.convertAndSend(RabbitMQConstants.XML.exchange, RabbitMQConstants.XML.routing, JSON.toJSONString(messageDto));
    }

    @Test
    public void delayMessage(){
        MessageDto messageDto = MessageDto.Builder.newInstance()
                .id(3L)
                .rabbitConsumerError(true)
                .build();

        amqpTemplate.convertAndSend(RabbitMQConstants.DELAY.exchange, RabbitMQConstants.DELAY.routing, JSON.toJSONString(messageDto));

    }
}
