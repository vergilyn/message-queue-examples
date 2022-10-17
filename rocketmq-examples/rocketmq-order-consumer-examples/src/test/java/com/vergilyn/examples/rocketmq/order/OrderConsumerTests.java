package com.vergilyn.examples.rocketmq.order;

import lombok.SneakyThrows;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

import static com.vergilyn.examples.rocketmq.order.OrderConsumerConstants.*;

@SpringBootTest(classes = RocketMQOrderConsumerApplication.class)
@Import(OrderConsumerTests.OrderConsumerListener.class)
public class OrderConsumerTests  {
	public static final String TAG_SEPARATOR = " || ";

	@SneakyThrows
	@Test
	public void annotationListener(){
		new Semaphore(0).acquire();
	}

	/**
	 * <p> 1. 包含多余的tag，也可以保证有序。 TODO 2022-10-17 待看源码如何排除`tag = "ignore_1"`？。
	 */
	@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
			nameServer = NAMESRV_ADDR,
			consumerGroup = GROUP_CONSUMER,
			topic = TOPIC,
			selectorType = SelectorType.TAG,
			// Only support or operation such as "tag1 || tag2 || tag3", If null or * expression,meaning subscribe all.
			selectorExpression = TAG_ORDER_1 + TAG_SEPARATOR
					+ TAG_ORDER_2 + TAG_SEPARATOR
					+ TAG_ORDER_3 + TAG_SEPARATOR
					+ TAG_OTHER_1 + TAG_SEPARATOR
					+ TAG_OTHER_2 + TAG_SEPARATOR
					// + TAG_IGNORE_1 + TAG_SEPARATOR  // topic中包含多余的tag，也可以保证有序
			,
			consumeMode = ConsumeMode.ORDERLY
	)
	public static class OrderConsumerListener implements RocketMQListener<MessageExt> {


		@Override
		public void onMessage(MessageExt message) {
			String key = message.getKeys();
			String topic = message.getTopic();
			String tags = message.getTags();
			String body = new String(message.getBody(), StandardCharsets.UTF_8);

			int queueId = message.getQueueId();
			Thread currentThread = Thread.currentThread();

			System.out.printf("[vergilyn][rocketmq-consumer][qid-%s][thread-%s] >>>> keys: %s, tags: %s, body: %s \n",
			                  queueId, currentThread.getName(), key, tags, body);
		}

	}
}
