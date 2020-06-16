package com.vergilyn.examples.rabbitmq.listener;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author vergilyn
 * @date 2020-06-16
 *
 * @see com.vergilyn.examples.rabbitmq.producer.BatchSendMessageTest
 */
@Component("batchSendMessageListener")
@Slf4j
public class BatchSendMessageListener {
    private static final AtomicInteger INDEX = new AtomicInteger(0);
    private static final AtomicInteger TOTAL = new AtomicInteger(0);

    @RabbitListener(queues = "queue.batch-send-message", containerFactory = "batchSendMessageRabbitListenerContainerFactory")
    public void onMessageBatch(List<Message> messages, Channel channel) {

        System.out.printf("[consumer][%02d][%s] begin >>>> size: %d, total: %d \r\n",
                INDEX.incrementAndGet(), LocalTime.now().toString(),
                messages.size(), TOTAL.addAndGet(messages.size()));

        long deliveryTag = 0;
        for (Message message : messages){
            System.out.printf("[consumer][%02d]: %s\r\n", INDEX.get(), new String(message.getBody()));

            deliveryTag = message.getMessageProperties().getDeliveryTag();
        }

        try {
            channel.basicNack(deliveryTag, true, false);
        } catch (IOException e) {
            log.error("error >>>> {}", e.getMessage());
        }
    }
}
