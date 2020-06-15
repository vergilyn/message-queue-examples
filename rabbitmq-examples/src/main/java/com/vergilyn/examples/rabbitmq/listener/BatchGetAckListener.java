package com.vergilyn.examples.rabbitmq.listener;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.Channel;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static com.vergilyn.examples.rabbitmq.config.ContainerFactoryConfiguration.BATCH_CONTAINER_BATCH_SIZE;
import static com.vergilyn.examples.rabbitmq.config.ContainerFactoryConfiguration.BATCH_CONTAINER_RECEIVE_TIMEOUT;
import static com.vergilyn.examples.rabbitmq.constants.RabbitDefinedEnum.BATCH_GET_ACK;

/**
 * @author vergilyn
 * @date 2020-06-08
 *
 * @see BatchMessageListener
 * @see org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener
 */
@Component("batchGetAckListener")
@Slf4j
public class BatchGetAckListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final AtomicInteger INDEX = new AtomicInteger(0);
    private static final AtomicInteger TOTAL = new AtomicInteger(0);
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "queue.batch-get-ack", containerFactory = "batchRabbitListenerContainerFactory")
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

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        AtomicInteger total = new AtomicInteger(33);
        AtomicInteger index = new AtomicInteger(0);

        // send messages continuously
        for(int i = 0, len = BATCH_CONTAINER_BATCH_SIZE + 3; i < len; i++){
            send();
            index.incrementAndGet();
        }

        // +1s for ensure receive-timeout
        sleep(TimeUnit.MILLISECONDS, BATCH_CONTAINER_RECEIVE_TIMEOUT + 1000L);

        // send messages at random intervals (1s ~ 4s). when grand than 3s(receive-timeout) execute consume-handler
        HashedWheelTimer timer = new HashedWheelTimer();
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                send();
                if (index.incrementAndGet() < total.get()){
                    timer.newTimeout(this, RandomUtils.nextInt(1, 4), TimeUnit.SECONDS);
                }else {
                    System.out.printf("[%s]provider sends message completed. total: %d \r\n", LocalTime.now().toString(), total.get());
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    private void send(){
        rabbitTemplate.convertAndSend(BATCH_GET_ACK.exchange, BATCH_GET_ACK.routing, LocalTime.now().toString());
    }

    private void sleep(TimeUnit unit, long timeout){
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

}
