package com.vergilyn.examples.rocketmq.pull;

import com.google.common.collect.Lists;
import com.vergilyn.examples.rocketmq.AbstractRocketMQPullModeApplicationTests;
import com.vergilyn.examples.rocketmq.RocketClientUtils;
import com.vergilyn.examples.rocketmq.RocketConstants;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author vergilyn
 * @since 2022-10-19
 */
@Import({
		PullTagFilterTests.Tag1xListener.class
		// PullTagFilterTests.Tag2xListener.class
})
public class PullTagFilterTests extends AbstractRocketMQPullModeApplicationTests {
	private static final String TOPIC = "vergilyn_topic_tag_filter";
	private static final String TAG_10 = "tag_10";
	private static final String TAG_11 = "tag_11";
	private static final String TAG_20 = "tag_20";
	private static final String GROUP_CONSUMER = "vergilyn_consumer_group_tag_filter";
	private static final String GROUP_PRODUCER = "vergilyn_producer_group_tag_filter";

	/**
	 * 场景：
	 * <pre> 同一个 consumer 消费同一个 topic的 不同tag （默认 4个 read/write Queue）， 其中:
	 *     Tag1xListener: TAG_10、TAG_11
	 *     Tag2xListener: TAG_20
	 * </pre>
	 *
	 * 按之前的理解，因为有 2个 listener-class，所以会分别创建 2个 pull-task。
	 * <br/>疑问：那么在consumer端，是如何pull-message？
	 * <p> Q1. 是 2个pull-task，还是 1个pull-task？
	 * <p> Q2. 如何pull 并调用 listener？
	 */
	@Test
	public void consumer(){
		preventExit();
	}

	static class Producer {

		public static void main(String[] args) {
			List<Message> messages = Lists.newArrayList(
					createMsg(TAG_10, "10_001"),
					createMsg(TAG_20, "20_001"),
					createMsg(TAG_11, "11_001"),
					createMsg(TAG_20, "20_002"),
					createMsg(TAG_11, "11_002"),
					createMsg(TAG_10, "10_002"),
					createMsg(TAG_20, "20_003")
				);

			RocketClientUtils.sendMsg(GROUP_PRODUCER, messages);
		}

		private static Message createMsg(String tag, String message){
			message += ", " + LocalDateTime.now().toString();
			String key = MessageClientIDSetter.createUniqID();
			return new Message(TOPIC, tag, key, message.getBytes());
		}
	}

	@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
			nameServer = RocketConstants.NAMESRV_ADDR,
			consumerGroup = GROUP_CONSUMER,
			topic = TOPIC,
			selectorType = SelectorType.TAG,
			selectorExpression = TAG_10 + " || " + TAG_11,
			consumeMode = ConsumeMode.ORDERLY
	)
	static class Tag1xListener implements RocketMQListener<MessageExt> {
		@Override
		public void onMessage(MessageExt message) {
			RocketClientUtils.printMessage(message);
		}
	}

	@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
			nameServer = RocketConstants.NAMESRV_ADDR,
			consumerGroup = GROUP_CONSUMER,
			topic = TOPIC,
			selectorType = SelectorType.TAG,
			selectorExpression = TAG_20,
			consumeMode = ConsumeMode.ORDERLY
	)
	static class Tag2xListener implements RocketMQListener<MessageExt> {
		@Override
		public void onMessage(MessageExt message) {
			RocketClientUtils.printMessage(message);
		}
	}
}


