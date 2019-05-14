package com.vergilyn.examples.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.constants.MessageModeEnum;
import com.vergilyn.examples.javabean.MessageDto;
import com.vergilyn.examples.rabbitmq.constants.RabbitDefinedEnum;

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

        amqpTemplate.convertAndSend(RabbitDefinedEnum.ANNO.exchange, RabbitDefinedEnum.ANNO.routing, JSON.toJSONString(messageDto));
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
        MessageDto messageDto = MessageDto.Builder.newInstance()
                .id(4L)
                .str("uni-concurrency")
                .build();

        amqpTemplate.convertAndSend(RabbitDefinedEnum.CONCURRENCY_UNI.exchange, RabbitDefinedEnum.CONCURRENCY_UNI.routing, JSON.toJSONString(messageDto));

    }
}
