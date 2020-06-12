package com.vergilyn.examples.rabbitmq.listener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author vergilyn
 * @date 2020-06-08
 */
@Component("batchGetAckListener")
@Slf4j
public class BatchGetAckListener implements BatchMessageListener {
    private static final AtomicInteger INDEX = new AtomicInteger(0);

    @Override
    @RabbitListener(queues = "queue.batch-get-ack", containerFactory = "batchRabbitListenerContainerFactory")
    public void onMessageBatch(List<Message> messages) {
        int i = INDEX.incrementAndGet();
        System.out.println(i + " >>>> begin " + messages.size());

        long deliveryTag = 0;
        for (Message message : messages){
            System.out.println(new String(message.getBody()));

            deliveryTag = message.getMessageProperties().getDeliveryTag();
        }

        /*try {
            channel.basicNack(deliveryTag, true, false);
        } catch (IOException e) {
            log.error("error >>>> {}", e.getMessage());
        }*/

        System.out.println(i + " >>>> end");
    }
}
