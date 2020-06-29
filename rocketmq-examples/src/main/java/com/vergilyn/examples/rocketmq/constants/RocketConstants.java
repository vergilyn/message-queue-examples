package com.vergilyn.examples.rocketmq.constants;

/**
 * @date 2019/1/31
 */
public interface RocketConstants {
    String NAMESRV_ADDR = "localhost:9876";

    String GROUP_PUSH_CONSUMER = "test_push_consumer";
    String GROUP_PULL_CONSUMER = "test_pull_consumer";

    interface BatchConstants{
        String GROUP_BATCH = "group%batch";

        String TOPIC_BATCH_SEND_MSG = "topic%batch-send-msg";
        String TAGS_BATCH_SEND_MSG = "tags%batch-send-msg";
    }
}
