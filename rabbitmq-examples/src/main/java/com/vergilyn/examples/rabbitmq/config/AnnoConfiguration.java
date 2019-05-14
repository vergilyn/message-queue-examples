package com.vergilyn.examples.rabbitmq.config;

import java.util.Map;

import com.google.common.collect.Maps;
import com.vergilyn.examples.rabbitmq.constants.RabbitDefinedEnum;
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

/**
 * @author VergiLyn
 * @date 2018/9/14
 */
@Configuration
public class AnnoConfiguration {
    @Autowired
    private ConsumerListener consumerListener;

    @Bean("annoQueue")
    public Queue annoQueue(){
        return new Queue(RabbitDefinedEnum.ANNO.queue);
    }

    /* type: x-delayed-message
     * DM: message-delayed:0
     * args: x-delayed-type:direct
     *
     * 最终rabbitmq的type: x-delayed-message
     */
    @Bean("annoExchange")
    public Exchange annoExchange(){
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("x-delayed-type", "direct");

        DirectExchange exchange = new DirectExchange(RabbitDefinedEnum.ANNO.exchange, true, false, arguments);
        exchange.setDelayed(true);  // false: 最终并不会是x-delayed-message，而是direct

        return exchange;
    }

    @Bean
    public Binding binding(){
        BindingBuilder.GenericArgumentsConfigurer configurer = BindingBuilder
                .bind(this.annoQueue())
                .to(this.annoExchange())
                .with(RabbitDefinedEnum.ANNO.routing);
        // configurer.and(Collections.emptyMap());
        return configurer.noargs();
    }

    /**
     * consumer-listener：ack-manual模式.
     * @see org.springframework.amqp.rabbit.annotation.RabbitListener
     */
    @Bean
    public MessageListenerContainer annoMessageListener(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer listener = new SimpleMessageListenerContainer(connectionFactory);
        listener.setQueues(this.annoQueue());   // 需要监听的队列
        listener.setAcknowledgeMode(AcknowledgeMode.MANUAL);  // 手动应答模式
        // listener.setMaxConcurrentConsumers();
        // listener.setConcurrentConsumers();
        // listener.setMessagePropertiesConverter();

        listener.setMessageListener(consumerListener);  // FIXME 不清楚原理，这么写不一定线程安全。
        // listener.setMessageListener(new ConsumerListener());    // 可能需要这么写，或用@RabbitListener实现

        return listener;
    }
}
