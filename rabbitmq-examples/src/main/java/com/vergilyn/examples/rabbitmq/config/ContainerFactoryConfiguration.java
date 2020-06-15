package com.vergilyn.examples.rabbitmq.config;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author VergiLyn
 * @date 2019-05-13
 */
@Configuration
public class ContainerFactoryConfiguration {
    public static final int BATCH_CONTAINER_BATCH_SIZE = 10;
    public static final long BATCH_CONTAINER_RECEIVE_TIMEOUT = TimeUnit.SECONDS.toMillis(3);

    @Bean("batchRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        // factory.setMessageConverter();

        factory.setBatchListener(true); // configures a BatchMessageListenerAdapter

        /**
         * when {@link #setConsumerBatchEnabled(boolean)} is true, `batch-size` determines how
         * many records to include in the batch as long as sufficient messages arrive within
         * {@link #setReceiveTimeout(long)}.
         *
         * @see SimpleMessageListenerContainer#setBatchSize(int)
         * @see SimpleMessageListenerContainer#doReceiveAndExecute(org.springframework.amqp.rabbit.listener.BlockingQueueConsumer)
         */
        factory.setConsumerBatchEnabled(true);
        factory.setDeBatchingEnabled(true);
        factory.setBatchSize(BATCH_CONTAINER_BATCH_SIZE);
        factory.setReceiveTimeout(BATCH_CONTAINER_RECEIVE_TIMEOUT);

        // factory.setBatchingStrategy(new SimpleBatchingStrategy(10, 10 * 1024, 10_000L));

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
