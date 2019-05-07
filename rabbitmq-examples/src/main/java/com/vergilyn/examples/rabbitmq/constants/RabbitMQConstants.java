package com.vergilyn.examples.rabbitmq.constants;

/**
 * @author VergiLyn
 * @date 2019-05-07
 */
public interface RabbitMQConstants {

     class RabbitDefined {
        public final String queue;
        public final String exchange;
        public final String routing;

        public RabbitDefined(String queue, String exchange, String routing) {
            this.queue = queue;
            this.exchange = exchange;
            this.routing = routing;
        }
    }

    RabbitDefined ANNO = new RabbitDefined("queue.anno-hello", "exchange.anno-hello", "routing.anno-hello");
    RabbitDefined XML = new RabbitDefined("queue.xml-hello", "exchange.xml-hello", "routing.xml-hello");
    RabbitDefined DELAY = new RabbitDefined("queue.delay-message", "exchange.delay-message", "routing.delay-message");
    RabbitDefined DELAY_RETRY = new RabbitDefined("queue.delay-message@retry", "exchange.delay-message@retry", "routing.delay-message@retry");
    RabbitDefined DELAY_FAILED = new RabbitDefined("queue.delay-message@failed", "exchange.delay-message@failed", "routing.delay-message@failed");
}
