package com.vergilyn.examples.rabbitmq.config;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.vergilyn.examples.rabbitmq.constants.RabbitDefinedEnum.BATCH_SEND_MSG;

/**
 * @author vergilyn
 * @date 2020-06-15
 */
@Configuration
public class BatchSendMessageConfiguration {
    public static final int BATCH_SEND_MSG_SIZE = 10;

    /** 1024 b = 1 kb */
    public static final int BATCH_SEND_MSG_BUFFER_LIMIT = 1024;

    /** 10s */
    public static final long BATCH_SEND_MSG_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    @Bean("batchTaskScheduler")
    public TaskScheduler batchTaskScheduler(){
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("rabbitmq-threadpool-scheduler-");
        scheduler.setThreadGroupName("batch-send-msg");
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors() + 1);

        return scheduler;
    }

    @Bean("batchingRabbitTemplate")
    public BatchingRabbitTemplate batchingRabbitTemplate(ConnectionFactory connectionFactory,
            @Qualifier("batchTaskScheduler") TaskScheduler taskScheduler){

        SimpleBatchingStrategy batchingStrategy = new SimpleBatchingStrategy(BATCH_SEND_MSG_SIZE,
                                                    BATCH_SEND_MSG_BUFFER_LIMIT,
                                                    BATCH_SEND_MSG_TIMEOUT);

        return new BatchingRabbitTemplate(connectionFactory, batchingStrategy,
                taskScheduler);
    }


    @Bean("batchSendMessageRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory batchSendMessageRabbitListenerContainerFactory(ConnectionFactory connectionFactory){
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
        factory.setBatchSize(BATCH_SEND_MSG_SIZE);
        factory.setReceiveTimeout(BATCH_SEND_MSG_TIMEOUT);

        factory.setBatchingStrategy(new SimpleBatchingStrategy(BATCH_SEND_MSG_SIZE, BATCH_SEND_MSG_BUFFER_LIMIT, BATCH_SEND_MSG_TIMEOUT));

        return factory;
    }

    @Bean("queue.batch-send-message")
    public Queue queue(){
        return new Queue(BATCH_SEND_MSG.queue);
    }

    @Bean("exchange.batch-send-message")
    public Exchange exchange(){
        return new DirectExchange(BATCH_SEND_MSG.exchange);
    }

    @Bean()
    public Binding binding(@Qualifier("queue.batch-send-message") Queue queue,
            @Qualifier("exchange.batch-send-message") Exchange exchange){

        return BindingBuilder.bind(queue).to(exchange).with(BATCH_SEND_MSG.routing).noargs();
    }

}
