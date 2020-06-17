package com.vergilyn.examples.rabbitmq.constants;

/**
 * @author VergiLyn
 * @date 2019-05-07
 */
public enum RabbitDefinedEnum {
    ANNO("anno-hello"),
    XML("xml-hello"),
    DELAY("delay-message"),
    DELAY_RETRY("delay-message@retry"),
    DELAY_FAILED("delay-message@failed"),
    CONCURRENCY_UNI("concurrency-uni"),
    CONCURRENCY_MULTI("concurrency-multi");

    public final String queue;
    public final String exchange;
    public final String routing;

    RabbitDefinedEnum(String flag) {
        this.queue = "queue." + flag;
        this.exchange = "exchange." + flag;
        this.routing = "routing." + flag;
    }

    public String getQueue() {
        return queue;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRouting() {
        return routing;
    }
}
