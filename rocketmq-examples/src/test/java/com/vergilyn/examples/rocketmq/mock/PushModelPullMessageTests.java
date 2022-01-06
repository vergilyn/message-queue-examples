package com.vergilyn.examples.rocketmq.mock;

import java.time.LocalTime;

import com.vergilyn.examples.rocketmq.AbstractRocketMQApplicationTests;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * <a href="https://docs.qq.com/doc/DWE1GWEhvTnhYVUhT">[rocketMQ] consumer PUSH模式拉取消息</a> <br/>
 * 消费端PUSH模式拉取消息，主要分为2个阶段：1) 拉取消息，将拉取到的消息缓存到本地缓冲队列中。 2) 将拉取到的消息提交给消费线程。
 *
 * 验证：
 *   push模式阶段一，如果
 *
 *
 *
 * @author vergilyn
 * @since 2022-01-06
 *
 * @see org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl
 */
@Import({PushModelPullMessageTests.TopicAListener.class,
		PushModelPullMessageTests.TopicBListener.class,
		PushModelPullMessageTests.TopicCListener.class
})
public class PushModelPullMessageTests extends AbstractRocketMQApplicationTests {
	private static final String CONSUMER_GROUP = "vergilyn_push_model_pull_msg_consumer";
	private static final String PRODUCER_GROUP = "vergilyn_push_model_pull_msg_producer";
	private static final String A_TOPIC = "vergilyn_push_model_topic_a";
	private static final String A_TAG = "vergilyn_push_model_tag_a";
	private static final String B_TOPIC = "vergilyn_push_model_topic_b";
	private static final String B_TAG = "vergilyn_push_model_tag_b";
	private static final String C_TOPIC = "vergilyn_push_model_topic_c";
	private static final String C_TAG = "vergilyn_push_model_tag_c";

	@Test
	public void consumer(){
		preventExit();
	}

	@Test
	public void producer(){
		sendDefaultMsg(10, A_TOPIC, A_TAG, PRODUCER_GROUP);
		sendDefaultMsg(10, B_TOPIC, B_TAG, PRODUCER_GROUP);
		sendDefaultMsg(10, C_TOPIC, C_TAG, PRODUCER_GROUP);

		preventExit();
	}

	public static void print(Message message){
		System.out.printf("[%s] >>>> topic: %s, tags: %s, body: %s \n",
		                  LocalTime.now(), message.getTopic(), message.getTags(),new String(message.getBody()));
	}

	@RocketMQMessageListener(consumerGroup = CONSUMER_GROUP, topic = A_TOPIC, selectorExpression = A_TAG)
	public static class TopicAListener implements RocketMQListener<Message> {

		@Override
		public void onMessage(Message message) {
			print(message);
		}
	}

	@RocketMQMessageListener(consumerGroup = CONSUMER_GROUP, topic = B_TOPIC, selectorExpression = B_TAG)
	public static class TopicBListener implements RocketMQListener<Message> {

		@Override
		public void onMessage(Message message) {
			print(message);
		}
	}

	@RocketMQMessageListener(consumerGroup = CONSUMER_GROUP, topic = C_TOPIC, selectorExpression = C_TAG)
	public static class TopicCListener implements RocketMQListener<Message> {

		@Override
		public void onMessage(Message message) {
			print(message);
		}
	}
}
