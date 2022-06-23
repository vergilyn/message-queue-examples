package com.vergilyn.examples.rocketmq.producer;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.vergilyn.examples.javabean.MessageDto;
import com.vergilyn.examples.rocketmq.constants.RocketConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vergilyn.examples.rocketmq.constants.RocketConstants.BatchConstants.*;
import static com.vergilyn.examples.rocketmq.constants.RocketConstants.NAMESRV_ADDR;

/**
 * @author vergilyn
 * @date 2020-06-29
 *
 * @see <a href="https://rocketmq.apache.org/docs/batch-example/">batch-example</a>
 */
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Slf4j
public class BatchMsgHandlerTest {
    private static final Map<MessageQueue, Long> OFFSET_TABLE = new HashMap<MessageQueue, Long>();

    @Test
    @Order(1)
    public void producer(){
        List<Message> messages = createMsg(100);

        DefaultMQProducer producer = new DefaultMQProducer(GROUP_BATCH);
        producer.setNamesrvAddr(NAMESRV_ADDR);
        producer.setVipChannelEnabled(false);
        try {
            producer.start();
            //  producer.createTopic("t_p_key", MESSAGE_TOPIC, 5);
            producer.send(messages);
        } catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
            log.error(e.getMessage(), e);
        }finally {
            producer.shutdown();
        }
    }

    /**
     * pushConsumer实际就是监听
     */
    @Test
    @Order(2)
    public void pushConsumer(){
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketConstants.GROUP_PUSH_CONSUMER);
        consumer.setNamesrvAddr(RocketConstants.NAMESRV_ADDR);
        /*
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        try {
            /*
             * @param topic
             *      topic to subscribe.
             * @param subExpression
             *      subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br>
             *      if null or * expression,meaning subscribe all
             */
            consumer.subscribe(TOPIC_BATCH_SEND_MSG, TAGS_BATCH_SEND_MSG);

            // 单线程消费
            consumer.setConsumeThreadMax(1);
            consumer.setConsumeThreadMin(1);

            consumer.registerMessageListener((MessageListenerConcurrently) (list, context) -> {
                MessageExt msg = list.get(0);
                long offset = msg.getQueueOffset();
                System.out.printf("size: %s, offset: %s ", list.size(), offset).println();

                // msg.getProperty(MessageConst.PROPERTY_MAX_OFFSET);

                list.forEach(e -> {
                    System.out.println(new String(e.getBody(), StandardCharsets.UTF_8));
                });

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(2)
    public void pullConsumer() {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(RocketConstants.GROUP_PULL_CONSUMER);
        consumer.setNamesrvAddr(RocketConstants.NAMESRV_ADDR);
        try {
            consumer.start();

            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(TOPIC_BATCH_SEND_MSG);
            for (MessageQueue mq : mqs) {
                System.out.printf("Consume from the queue: %s%n", mq);
                SINGLE_MQ:
                while (true) {
                    try {
                        PullResult pullResult = consumer.pullBlockIfNotFound(mq, TAGS_BATCH_SEND_MSG, getMessageQueueOffset(mq), 32);
                        System.out.printf("%s%n", pullResult);
                        putMessageQueueOffset(mq, pullResult.getNextBeginOffset());
                        switch (pullResult.getPullStatus()) {
                        case FOUND:
                            List<MessageExt> messageExtList = pullResult.getMsgFoundList();
                            for (MessageExt m : messageExtList) {
                                System.out.println(new String(m.getBody()));
                            }
                            break;
                        case NO_MATCHED_MSG: break;
                        case NO_NEW_MSG: break SINGLE_MQ;
                        case OFFSET_ILLEGAL: break;
                        default: break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    private long getMessageQueueOffset(MessageQueue mq) {
        Long offset = OFFSET_TABLE.get(mq);
        if (offset != null){
            return offset;
        }

        return 0;
    }

    private void putMessageQueueOffset(MessageQueue mq, long offset) {
        OFFSET_TABLE.put(mq, offset);
    }

    private List<Message> createMsg(int size){
        List<Message> messages = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++){
            MessageDto messageDto = MessageDto.builder()
                    .id((long) i)
                    .str("user-" + i)
                    .build();

            Message message = new Message(TOPIC_BATCH_SEND_MSG,
                    TAGS_BATCH_SEND_MSG,
                    "key-" + i,
                    JSON.toJSONBytes(messageDto));

            messages.add(message);
        }

        return messages;
    }
}
