package com.vergilyn.examples.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.constants.RabbitMQConstants;
import com.vergilyn.examples.javabean.MessageDto;

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
@SpringBootTest(classes = RabbitMQApplication.class, properties = {"spring.profiles.active=rabbit,anno"})
public class RabbitMQProducerTest {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void producer(){
        MessageDto messageDto = MessageDto.newInstance(2L, null);
        messageDto.setMode(null);

        amqpTemplate.convertAndSend(RabbitMQConstants.ANNO_EXCHANGE, RabbitMQConstants.ANNO_ROUTING, JSON.toJSONString(messageDto));
    }

}
