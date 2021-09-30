package com.vergilyn.examples.rabbitmq.producer;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vergilyn.examples.rabbitmq.AbstractRabbitMQApplicationTests;
import com.vergilyn.examples.rabbitmq.listener.BatchGetAckListener;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import static com.vergilyn.examples.rabbitmq.config.BatchGetAckConfiguration.BATCH_CONTAINER_BATCH_SIZE;
import static com.vergilyn.examples.rabbitmq.config.BatchGetAckConfiguration.BATCH_CONTAINER_RECEIVE_TIMEOUT;
import static com.vergilyn.examples.rabbitmq.constants.RabbitAutoDeclareEnum.BATCH_GET_ACK;

/**
 * 观察 consumer-side 对批量消息的处理。
 * <p>1. 注意provider-side并未使用{@link BatchingRabbitTemplate}，而是普通的{@link RabbitTemplate}单条发送。
 *
 * <p>2. {@link BatchSendMessageTest} 中用的是 {@link BatchingRabbitTemplate}。
 * @author vergilyn
 * @date 2020-06-15
 *
 * @see BatchGetAckListener
 */
@Slf4j
public class BatchGetAckTest extends AbstractRabbitMQApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void producer(){
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

        preventExit();
    }

    private void send(){
        rabbitTemplate.convertAndSend(BATCH_GET_ACK.exchange, BATCH_GET_ACK.routing, LocalTime.now().toString());
    }
}
