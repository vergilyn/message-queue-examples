package com.vergilyn.examples.rocketmq.consumer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vergilyn.examples.constants.RocketConstants;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.context.annotation.Configuration;

/**
 * @date 2019/1/31
 */
@Configuration
public class RocketConsumerConfiguration {
    private static final Map<MessageQueue, Long> OFFSET_TABLE = new HashMap<MessageQueue, Long>();

    /**
     * pushConsumer实际就是监听
     */
    public void pushConsumer(){
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketConstants.GROUP_PUSH_CONSUMER);
        consumer.setNamesrvAddr(RocketConstants.NAMESRV_ADDR);
        /*
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        try {
            // 订阅MESSAGE_TOPIC下MESSAGE_TAGS的消息
            consumer.subscribe(RocketConstants.MESSAGE_TOPIC, RocketConstants.MESSAGE_TAG);
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

    /**
     *
     */
    public void pullConsumer() {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(RocketConstants.GROUP_PULL_CONSUMER);
        consumer.setNamesrvAddr(RocketConstants.NAMESRV_ADDR);
        try {
            consumer.start();

            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(RocketConstants.MESSAGE_TOPIC);
            for (MessageQueue mq : mqs) {
                System.out.printf("Consume from the queue: %s%n", mq);
                SINGLE_MQ:
                while (true) {
                    try {
                        PullResult pullResult = consumer.pullBlockIfNotFound(mq, RocketConstants.MESSAGE_TAG, getMessageQueueOffset(mq), 32);
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

    private static long getMessageQueueOffset(MessageQueue mq) {
        Long offset = OFFSET_TABLE.get(mq);
        if (offset != null)
            return offset;

        return 0;
    }

    private static void putMessageQueueOffset(MessageQueue mq, long offset) {
        OFFSET_TABLE.put(mq, offset);
    }

}
