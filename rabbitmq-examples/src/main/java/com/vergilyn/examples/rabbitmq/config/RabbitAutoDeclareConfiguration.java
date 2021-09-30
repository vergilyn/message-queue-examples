package com.vergilyn.examples.rabbitmq.config;

import com.vergilyn.examples.rabbitmq.constants.RabbitAutoDeclareEnum;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import static com.vergilyn.examples.rabbitmq.constants.RabbitAutoDeclareEnum.EXCHANGE_MODE_FANOUT;
import static com.vergilyn.examples.rabbitmq.constants.RabbitAutoDeclareEnum.EXCHANGE_MODE_TOPIC;

/**
 * @author vergilyn
 * @date 2020-06-17
 */
// @Configuration
public class RabbitAutoDeclareConfiguration implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        for (RabbitAutoDeclareEnum e : RabbitAutoDeclareEnum.values()){

            Queue queue = new Queue(e.queue);
            beanFactory.registerSingleton(queue.getName(), queue);

            String exchangeMode = e.exchangeMode;
            Exchange exchange;
            switch (exchangeMode){
                case EXCHANGE_MODE_FANOUT:
                    exchange = new FanoutExchange(e.exchange);
                    break;
                case EXCHANGE_MODE_TOPIC:
                    exchange = new TopicExchange(e.exchange);
                    break;
                default:
                    exchange = new DirectExchange(e.exchange);
            }
            beanFactory.registerSingleton(exchange.getName(), exchange);

            Binding binding = BindingBuilder
                    .bind(queue)
                    .to(exchange)
                    .with(e.routing)
                    .noargs();
            beanFactory.registerSingleton(e.routing, binding);

        }
    }
}
