package com.vergilyn.examples.rabbitmq.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author VergiLyn
 * @date 2019-05-07
 */
@Configuration
public class RabbitConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
        return new RabbitTemplate(connectionFactory);
    }
}
