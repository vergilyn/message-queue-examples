package com.vergilyn.examples.rocketmq;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.rocketmq.constants.RocketDefinedGenerator;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;

/**
 * @author vergilyn
 * @date 2020-06-15
 */
public abstract class AbstractRocketMQTests {
    protected static final String NAMESRV_ADDR = "localhost:9876";

    protected DefaultMQProducer createProducer(RocketDefinedGenerator defined) {
        DefaultMQProducer producer = new DefaultMQProducer(defined.producerGroup());
        producer.setNamesrvAddr(NAMESRV_ADDR);
        producer.setVipChannelEnabled(false);

        return producer;
    }

    protected DefaultMQPushConsumer createConsumer(RocketDefinedGenerator defined) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(defined.consumerGroup());
        consumer.setNamesrvAddr(NAMESRV_ADDR);

        /*
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.subscribe(defined.topic(), defined.tag());

        return consumer;
    }


    protected static Message createMessage(RocketDefinedGenerator defined, LocalDateTime now){
        Message message = new Message(defined.topic(),
                                      defined.tag(),
                                      generatorKey(now),
                                      JSON.toJSONBytes(String.format("{\"now\": \"%s\"}", now)));

        return message;
    }

    protected static String generatorKey(LocalDateTime time){
        return "key-" + time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    protected void sleep(TimeUnit unit, long timeout){
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    protected void preventExit(){
        try {
            new Semaphore(0).acquire();
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
