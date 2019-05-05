package com.vergilyn.examples.rocketmq;

/**
 * @date 2019/1/31
 */
public interface RocketConstants {
    String NAMESRV_ADDR = "localhost:9876";
    String MESSAGE_TOPIC = "test_topic";
    String MESSAGE_TAG = MESSAGE_TOPIC + "@tags";

    String GROUP_PRODUCER = "test_producer_group";
    String GROUP_PUSH_CONSUMER = "test_push_consumer";
    String GROUP_PULL_CONSUMER = "test_pull_consumer";
}
