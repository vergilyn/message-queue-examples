package com.vergilyn.examples.rabbitmq.listener;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author vergilyn
 * @date 2020-06-08
 *
 * @see BatchMessageListener
 * @see org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener
 */
@Component("batchGetAckListener")
@Slf4j
public class BatchGetAckListener{
    private static final AtomicInteger INDEX = new AtomicInteger(0);
    private static final AtomicInteger TOTAL = new AtomicInteger(0);

    @RabbitListener(queues = "queue.batch-get-ack", containerFactory = "batchGetAckRabbitListenerContainerFactory")
    public void onMessageBatch(List<Message> messages, Channel channel) {
        System.out.printf("[%s][%d] begin >>>> size: %d, total: %d \r\n", LocalTime.now().toString(), INDEX.incrementAndGet(),
                                                messages.size(), TOTAL.addAndGet(messages.size()));

        long deliveryTag = 0;
        for (Message message : messages){
            System.out.println(new String(message.getBody()));

            deliveryTag = message.getMessageProperties().getDeliveryTag();
        }

        try {
            channel.basicNack(deliveryTag, true, false);
        } catch (IOException e) {
            log.error("error >>>> {}", e.getMessage());
        }
    }

}
