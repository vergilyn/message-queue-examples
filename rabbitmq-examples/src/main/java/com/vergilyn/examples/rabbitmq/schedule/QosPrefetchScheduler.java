package com.vergilyn.examples.rabbitmq.schedule;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.vergilyn.examples.rabbitmq.constants.RabbitAutoDeclareEnum.QOS_PREFETCH;

/**
 * <p><a href="https://www.rabbitmq.com/confirms.html#channel-qos-prefetch">channel-qos-prefetch</a>
 * <p><a href="https://www.rabbitmq.com/consumer-prefetch.html">Consumer Prefetch</a>
 *
 * <p>prefetch-count 并不是期望的consumer-side 通过1次TCP/AMQP 连接交互n条消息（consumer-side 将这n条消息保存在自己的memory中）
 * @author vergilyn
 * @date 2020-06-17
 */
@Component
@Slf4j
public class QosPrefetchScheduler {
    private static final AtomicInteger INDEX = new AtomicInteger(0);

    @Autowired
    private ConnectionFactory connectionFactory;

    // @Scheduled(cron = "0/5 * * * * *")
    public void task(){
        int index = INDEX.getAndIncrement();
        System.out.printf("[consumer][%02d]: %s begin >>>> \r\n", index, LocalTime.now().toString());

        /**
         * default {@link CachingConnectionFactory#createConnection()}, allow re-used connection
         */
        Connection connection = connectionFactory.createConnection();

        Channel channel = connection.createChannel(false);
        try{
            int prefetchCount = 10;
            channel.basicQos(prefetchCount, true);

            long deliveryTag = 0L;
            for (int i = 0; i < prefetchCount; i++){
                // 2020-06-17 通过 wireshark 抓包可知，每次get都会产生一次 TCP 和 AMQP 交互
                GetResponse response = channel.basicGet(QOS_PREFETCH.queue, false);
                if (response == null){
                    break;
                }
                deliveryTag = response.getEnvelope().getDeliveryTag();
                System.out.printf("[consumer][%02d]: count: %d, deliveryTag: %d, msg: %s \r\n", index, response.getMessageCount(), deliveryTag, new String(response.getBody()));
            }

            channel.basicAck(deliveryTag, true);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // XXX 2020-06-17 还是未理解是否需要close connection & channel
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                log.warn("close channel exception: {}", e.getMessage());
            }
        }

        System.out.printf("[consumer][%02d]: %s end <<<< \r\n", index, LocalTime.now().toString());
    }
}
