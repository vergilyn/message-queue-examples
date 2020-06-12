package com.vergilyn.examples.rabbitmq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author VergiLyn
 * @date 2019-05-13
 */
@Configuration
public class ContainerFactoryConfiguration {

    @Bean("batchRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        // factory.setMessageConverter();

        factory.setDeBatchingEnabled(true);
        factory.setBatchListener(true); // configures a BatchMessageListenerAdapter
        factory.setConsumerBatchEnabled(true);
        factory.setBatchSize(10);

        return factory;
    }

    @Bean("uniConcurrencyContainerFactory")
    public RabbitListenerContainerFactory uniConcurrencyContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        // factory.setMessageConverter();

        return factory;
    }

    @Bean("multiConcurrencyContainerFactory")
    public RabbitListenerContainerFactory multiConcurrencyContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(2);

        return factory;
    }
}
