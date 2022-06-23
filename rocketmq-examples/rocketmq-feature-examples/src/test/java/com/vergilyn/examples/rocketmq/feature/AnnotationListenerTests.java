package com.vergilyn.examples.rocketmq.feature;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.rocketmq.AbstractRocketMQApplicationTests;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.concurrent.atomic.AtomicInteger;

@Import(AnnotationListenerTests.HelloworldAnnotationListener.class)
public class AnnotationListenerTests extends AbstractRocketMQApplicationTests {
	public static final String CONSUMER_GROUP_ANNOTATION = "vergilyn_consumer_group_helloworld_anno";
	public static final String TOPIC = "vergilyn_topic_helloworld";

	@Test
	public void annotationListener(){
		preventExit();
	}

	@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
			consumerGroup = CONSUMER_GROUP_ANNOTATION,
			topic = TOPIC,
			selectorType = SelectorType.TAG,
			// Only support or operation such as "tag1 || tag2 || tag3", If null or * expression,meaning subscribe all.
			selectorExpression = "*",
			consumeMode = ConsumeMode.CONCURRENTLY
	)
	public static class HelloworldAnnotationListener implements RocketMQListener<Message> {
		private static final AtomicInteger _index = new AtomicInteger(0);

		@Override
		public void onMessage(Message message) {
			int index = _index.incrementAndGet();
			System.out.printf("[%d][%s] message.class >>>> %s \n",
			                  index, CONSUMER_GROUP_ANNOTATION, message.getClass().getName());

			System.out.printf("[%d][%s] message >>>> %s \n",
			                  index, CONSUMER_GROUP_ANNOTATION, JSON.toJSONString(message, true));

			System.out.printf("[%d][%s] body >>>> %s \n",
			                  index, CONSUMER_GROUP_ANNOTATION, new String(message.getBody()));

			System.out.println();
		}

	}
}
