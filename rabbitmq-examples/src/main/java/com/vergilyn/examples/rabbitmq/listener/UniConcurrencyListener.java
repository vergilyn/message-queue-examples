package com.vergilyn.examples.rabbitmq.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.rabbitmq.client.Channel;
import com.vergilyn.examples.util.RabbitMessageUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * <a href="https://blog.csdn.net/m0_37556444/article/details/82627723">使用@RabbitListener注解接受消息时如何进行手动ack</a>
 * @author VergiLyn
 * @date 2019-05-13
 */
@Slf4j
@Component
public class UniConcurrencyListener {
    /**
     * <pre>
     *   @RabbitHandler
     *   public void error(Message message, Channel channel) {
     *     // ...
     *   }
     * </pre>
     *  {@linkplain RabbitHandler @RabbitHandler} is intended only for processing message payloads after conversion,
     *  if you wish to receive the unconverted raw Message object,
     *  you must use {@linkplain RabbitListener @RabbitListener} on the method, not the class.
     * @param message
     * @param channel
     * @throws Exception
     */
    @UniConcurrencyRabbitListener
    public void onMessage(Message message, Channel channel) throws Exception {
        log.info("consumer-queue >>>> {}, body: {}", message.getMessageProperties().getConsumerQueue(), RabbitMessageUtils.parseObject(message));

        try {
            Thread.sleep( 60 * 1000L);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (InterruptedException e) {

        }
    }

}

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@RabbitListener(admin = "rabbitAdmin",
        concurrency = "1-1",
        containerFactory = "uniConcurrencyContainerFactory",
        bindings = @QueueBinding(
                declare = "true",
                value = @Queue(name = "queue.concurrency-uni", declare = "true", durable = "true"),
                exchange = @Exchange(name = "exchange.concurrency-uni", type = "direct", delayed = "false",declare = "true", durable = "true"),
                key = "routing.concurrency-uni"
        )
)
@interface UniConcurrencyRabbitListener{

}