package com.vergilyn.examples.rabbitmq.config;

import com.vergilyn.examples.rabbitmq.listener.ConsumerListener;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author VergiLyn
 * @date 2018/9/14
 */
@Configuration
@Profile("anno")
public class AnnoConfig {
    public static final String ANNO_QUEUE = "queue.anno-hello";
    public static final String ANNO_EXCHANGE = "exchange.anno-hello";
    public static final String ANNO_ROUTING = "routing.anno-hello";

    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private ConsumerListener consumerListener;

    @Bean
    public Queue queue(){
        return new Queue(ANNO_QUEUE);
    }

    @Bean
    public Exchange exchange(){
        return new DirectExchange(ANNO_EXCHANGE);
    }

    @Bean
    public Binding binding(){
        BindingBuilder.GenericArgumentsConfigurer configurer = BindingBuilder
                .bind(this.queue())
                .to(this.exchange())
                .with(ANNO_ROUTING);
        // configurer.and(Collections.emptyMap());
        return configurer.noargs();
    }

    /**
     * consumer-listener：ack-manual模式.
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener
     */
    @Bean
    public MessageListenerContainer bindMessageListener(){
        SimpleMessageListenerContainer listener = new SimpleMessageListenerContainer(connectionFactory);
        listener.setQueues(this.queue());   // 需要监听的队列
        listener.setAcknowledgeMode(AcknowledgeMode.MANUAL);  // 手动应答模式
        // listener.setMaxConcurrentConsumers();
        // listener.setConcurrentConsumers();
        // listener.setMessagePropertiesConverter();

        listener.setMessageListener(consumerListener);  // FIXME 不清楚原理，这么写不一定线程安全。
        // listener.setMessageListener(new ConsumerListener());    // 可能需要这么写，或用@RabbitListener实现

        return listener;
    }
}
