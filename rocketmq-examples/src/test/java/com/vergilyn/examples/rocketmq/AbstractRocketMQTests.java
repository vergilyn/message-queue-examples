package com.vergilyn.examples.rocketmq;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.vergilyn.examples.rocketmq.constants.RocketDefinedGenerator;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;

/**
 * @author vergilyn
 * @date 2020-06-15
 */
@Slf4j
public abstract class AbstractRocketMQTests {
    protected static final String NAMESRV_ADDR = "localhost:9876";

    protected DefaultMQProducer createProducer(RocketDefinedGenerator defined) {
        return createProducer(defined.producerGroup());
    }

    protected DefaultMQProducer createProducer(String producerGoup) {
        DefaultMQProducer producer = new DefaultMQProducer(producerGoup);
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

    protected void sendDefaultMsg(int size, RocketDefinedGenerator defined){
        sendDefaultMsg(size, defined.topic(), defined.tag(), defined.producerGroup());
    }

    protected void sendDefaultMsg(int size, String topic, String tags, String producerGroup){
        List<Message> messages = Lists.newArrayListWithCapacity(size);

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < size; i++) {
            Message message = new Message(topic,
                                          tags,
                                          generatorKey(now) + "-" + i,
                                          JSON.toJSONBytes(String.format("{\"now\": \"%s\", \"index\": %d}", now, i)));;


            messages.add(message);
        }

        sendMsg(messages, producerGroup);
    }

    /**
     * 参考 rocketmq-spring-boot：<br/>
     * producer 可以是单例对象，并且{@linkplain DefaultMQProducer#start()}后 一直使用，
     * 直到 destroy-bean 时调用 {@linkplain DefaultMQProducer#shutdown()}
     *
     * @see org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration#defaultMQProducer(RocketMQProperties)
     * @see org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration#rocketMQTemplate(RocketMQMessageConverter)
     * @see RocketMQTemplate#afterPropertiesSet()
     * @see RocketMQTemplate#destroy()
     */
    protected void sendMsg(List<Message> messages, String producerGroup){
        DefaultMQProducer producer = createProducer(producerGroup);
        try {
            producer.start();

            // FIXME 2022-01-06，rocketMQ-4.9.1 批量发送存在bug。未正确保存`tags` https://github.com/apache/rocketmq/issues/3476
            // producer.send(messages);

            for (Message message : messages) {
                producer.send(message);
            }

        } catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
            log.error(e.getMessage(), e);
        }finally {
            producer.shutdown();
        }
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
            // new Semaphore(0).acquire();
            TimeUnit.HOURS.sleep(1);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
