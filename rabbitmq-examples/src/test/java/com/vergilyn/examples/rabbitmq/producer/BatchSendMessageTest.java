package com.vergilyn.examples.rabbitmq.producer;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vergilyn.examples.rabbitmq.AbstractSpringbootTest;
import com.vergilyn.examples.rabbitmq.listener.BatchSendMessageListener;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import static com.vergilyn.examples.rabbitmq.constants.RabbitDefinedEnum.BATCH_SEND_MSG;

/**
 * 测试 rabbitMQ 的批量发送。
 * 当provider-side满足 {@linkplain SimpleBatchingStrategy}时，会将N条message合并成1条发送到rabbitMQ server，
 * consumer-side 再根据strategy解析成N条数据。
 * @author vergilyn
 * @date 2020-06-15
 *
 * @see BatchSendMessageListener
 */
public class BatchSendMessageTest extends AbstractSpringbootTest {

    @Autowired
    public BatchingRabbitTemplate batchingRabbitTemplate;

    @Test
    public void batchSend(){
        AtomicInteger index = new AtomicInteger(0);

        HashedWheelTimer timer = new HashedWheelTimer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (index.getAndIncrement() < 20) {
                    sendMessage(index.get());
                    timer.newTimeout(this, RandomUtils.nextInt(1, 4), TimeUnit.SECONDS);
                }else {
                    System.out.println("[provider]: send message completed!");
                }
            }
        };

        timer.newTimeout(timerTask, 0, TimeUnit.SECONDS);

        preventExit();
    }

    private void sendMessage(int index){
        String msg = LocalTime.now().toString();
        batchingRabbitTemplate.convertAndSend(BATCH_SEND_MSG.exchange, BATCH_SEND_MSG.routing, msg);

        System.out.printf("[provider][%02d]: %s \r\n", index, msg);
    }
}
