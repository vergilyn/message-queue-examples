package com.vergilyn.examples.rocketmq.constants;

/**
 *
 * @author vergilyn
 * @since 2021-09-30
 */
public class RocketDefinedGenerator {
	private final String topic;
	private final String tag;

	private final String producerGroup;
	private final String consumerGroup;

	public RocketDefinedGenerator(String flag) {
		this.topic = "vergilyn_topic_" + flag;
		this.tag = "vergilyn_tag_" + flag;
		this.producerGroup = "vergilyn_producer_group_" + flag;
		this.consumerGroup = "vergilyn_consumer_group_" + flag;
	}

	public String topic() {
		return topic;
	}

	public String tag() {
		return tag;
	}

	public String producerGroup() {
		return producerGroup;
	}

	public String consumerGroup() {
		return consumerGroup;
	}
}
