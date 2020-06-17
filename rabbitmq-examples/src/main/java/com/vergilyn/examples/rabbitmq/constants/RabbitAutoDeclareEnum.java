package com.vergilyn.examples.rabbitmq.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * @author vergilyn
 * @date 2020-06-17
 */
public enum RabbitAutoDeclareEnum {
    BATCH_SEND_MSG("batch-send-message",  ""),
    BATCH_GET_ACK("batch-get-ack", ""),
    QOS_PREFETCH("qos-prefetch", "");

    public static final String EXCHANGE_MODE_DIRECT = "direct";
    public static final String EXCHANGE_MODE_FANOUT = "fanout";
    public static final String EXCHANGE_MODE_TOPIC= "topic";

    public final String queue;
    public final String exchange;
    public final String routing;
    public final String exchangeMode;

    RabbitAutoDeclareEnum(String flag, String exchangeMode) {
        this.queue = "queue." + flag;
        this.exchange = "exchange." + flag;
        this.routing = "routing." + flag;
        this.exchangeMode = StringUtils.isBlank(exchangeMode) ? EXCHANGE_MODE_DIRECT : exchangeMode;
    }
}
