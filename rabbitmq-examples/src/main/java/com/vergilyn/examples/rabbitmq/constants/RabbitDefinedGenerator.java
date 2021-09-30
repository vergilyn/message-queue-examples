package com.vergilyn.examples.rabbitmq.constants;

/**
 *
 * @author vergilyn
 * @since 2021-09-30
 */
public class RabbitDefinedGenerator {
	private final String queue;
	private final String exchange;
	private final String routing;

	public RabbitDefinedGenerator(String flag) {
		this.queue = "vergilyn.queue." + flag;
		this.exchange = "vergilyn.exchange." + flag;
		this.routing = "vergilyn.routing." + flag;
	}

	public String queue() {
		return queue;
	}

	public String exchange() {
		return exchange;
	}

	public String routing() {
		return routing;
	}
}
