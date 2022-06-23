package com.vergilyn.examples.rocketmq.overstock;

import com.vergilyn.examples.rocketmq.AbstractRocketMQApplicationTests;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.MQConsumerInner;
import org.apache.rocketmq.client.impl.consumer.PullRequest;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalTime;

/**
 * <a href="https://docs.qq.com/doc/DWE1GWEhvTnhYVUhT">[rocketMQ] consumer PUSH模式拉取消息</a> <br/>
 * 消费端PUSH模式拉取消息，主要分为2个阶段：1) 拉取消息，将拉取到的消息缓存到本地缓冲队列中。 2) 将拉取到的消息提交给消费线程。
 * <p>
 *
 * <p> <b>验证：</b>push模式阶段一，什么情况下会造成 消息阻塞，导致消费消息延迟。
 * <p> <b>结论：</b><a href="https://docs.qq.com/doc/DWE1GWEhvTnhYVUhT">[rocketMQ] consumer PUSH模式拉取消息</a>
 * <pre>
 *   假设我们的broker集群包含3个broker节点，在这个集群上我们创建了一个topic并且topic指定的队列个数为6，
 *   那么在这个场景下我们总共有3*6=18个队列(queue)，其中3是3个broker，6是每个topic的队列数。
 *
 *   由于每个queue的拉取都是由单独任务在驱动的，所以总共有18个拉取任务，
 *   <b>由一个线程的串行进行拉取</b>，拉取完后再次重复提交进行二次拉取过程，循环往复持续拉取数据。
 * </pre>
 *
 * @author vergilyn
 * @since 2022-01-06
 *
 * @see org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl#pullMessage(PullRequest)
 */
@Import({ PushModelPullMessageTests.AbstractTopicA01Listener.class, PushModelPullMessageTests.AbstractTopicA02Listener.class,
		PushModelPullMessageTests.AbstractTopicB01Listener.class, PushModelPullMessageTests.AbstractTopicC01Listener.class })
public class PushModelPullMessageTests extends AbstractRocketMQApplicationTests {
	private static final String COMMON_PREFIX = "vergilyn_push_model";

	private static final String CONSUMER_GROUP = COMMON_PREFIX + "_consumer";

	private static final String PRODUCER_GROUP = COMMON_PREFIX + "_producer";

	private static final String TOPIC_A = COMMON_PREFIX + "_topic_a";

	private static final String TOPIC_A_TAG_01 = COMMON_PREFIX + "_tag_a_01";

	private static final String TOPIC_A_TAG_02 = COMMON_PREFIX + "_tag_a_02";

	private static final String TOPIC_B = COMMON_PREFIX + "_topic_b";

	private static final String TOPIC_B_TAG_01 = COMMON_PREFIX + "_tag_b_01";

	private static final String TOPIC_C = COMMON_PREFIX + "_topic_c";

	private static final String TOPIC_C_TAG_01 = COMMON_PREFIX + "_tag_c_01";

	/**
	 * {@linkplain org.apache.rocketmq.client.impl.factory.MQClientInstance#registerConsumer(String, MQConsumerInner)}
	 *
	 * <pre>
	 * +------------------+----------------------+------------------------------+
	 * | MQClientInstance | messageListenerInner | tag                          |
	 * +------------------+----------------------+------------------------------+
	 * | @5955            | @7650                | vergilyn_push_model_tag_c_01 |
	 * | @7748            | @7775                | vergilyn_push_model_tag_b_01 |
	 * | @7824            | @7851                | vergilyn_push_model_tag_a_02 |
	 * | @7882            | @7909                | vergilyn_push_model_tag_a_01 |
	 * +------------------+----------------------+------------------------------+
	 * </pre>
	 *
	 * 表示，每个 listener-class，都会新建自己对应的 MQClientInstance 和 DefaultPushConsumer。
	 * 所以，不存在因为 1个Topic 包含多个 Tags，而导致pull-message相互影响。
	 *
	 * <p> <a href="https://help.aliyun.com/document_detail/95837.html">Topic与Tag最佳实践</a> <br/>
	 *  <blockquote>
	 *  消息优先级是否一致：如同样是物流消息，盒马必须小时内送达，天猫超市24小时内送达，淘宝物流则相对会慢一些，
	 *      不同优先级的消息用不同的Topic进行区分。 <br/>
	 *  <br/>
	 *  消息量级是否相当：有些业务消息虽然量小但是实时性要求高，如果跟某些万亿量级的消息使用同一个Topic，则有可能会因为过长的等待时间而“饿死”，
	 *      此时需要将不同量级的消息进行拆分，使用不同的Topic。<br/>
	 *  </blockquote>
	 * </p>
	 */
	@Test
	public void consumer() {
		preventExit();
	}

	@Test
	public void producer() {
		sendDefaultMsg(10, TOPIC_A, TOPIC_A_TAG_01, PRODUCER_GROUP);
		sendDefaultMsg(10, TOPIC_A, TOPIC_A_TAG_02, PRODUCER_GROUP);
		sendDefaultMsg(10, TOPIC_B, TOPIC_B_TAG_01, PRODUCER_GROUP);
		sendDefaultMsg(10, TOPIC_C, TOPIC_C_TAG_01, PRODUCER_GROUP);

		preventExit();
	}

	public static void preProcessMQConsumer(DefaultMQPushConsumer consumer) {
		// 流控，限制每个Topic 最多缓存 2条 messages。 (模拟消息 堆积)
		// 假设 `queue-nums = 4`，那么 `PullThresholdForQueue = Math.max(1, 2 / 4)`
		consumer.setPullThresholdForTopic(2);

		// consumer.setPullThresholdForQueue();
	}

	public static void print(Message message) {
		System.out.printf("[%s] >>>> topic: %s, tags: %s, body: %s \n", LocalTime.now(), message.getTopic(),
		                  message.getTags(), new String(message.getBody()));
	}

	protected static abstract class AbstractTopicListener
			implements RocketMQListener<Message>, RocketMQPushConsumerLifecycleListener{
		@Override
		public void onMessage(Message message) {
			print(message);
		}

		@Override
		public void prepareStart(DefaultMQPushConsumer consumer) {
			preProcessMQConsumer(consumer);
		}
	}

	@RocketMQMessageListener(consumerGroup = CONSUMER_GROUP, topic = TOPIC_A, selectorExpression = TOPIC_A_TAG_01)
	public static class AbstractTopicA01Listener extends AbstractTopicListener { }

	@RocketMQMessageListener(consumerGroup = CONSUMER_GROUP, topic = TOPIC_A, selectorExpression = TOPIC_A_TAG_02)
	public static class AbstractTopicA02Listener extends AbstractTopicListener { }

	@RocketMQMessageListener(consumerGroup = CONSUMER_GROUP, topic = TOPIC_B, selectorExpression = TOPIC_B_TAG_01)
	public static class AbstractTopicB01Listener extends AbstractTopicListener { }

	@RocketMQMessageListener(consumerGroup = CONSUMER_GROUP, topic = TOPIC_C, selectorExpression = TOPIC_C_TAG_01)
	public static class AbstractTopicC01Listener extends AbstractTopicListener { }
}
