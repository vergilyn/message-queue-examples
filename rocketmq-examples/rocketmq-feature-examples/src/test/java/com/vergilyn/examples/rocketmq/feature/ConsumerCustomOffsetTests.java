package com.vergilyn.examples.rocketmq.feature;

import com.vergilyn.examples.rocketmq.AbstractRocketMQApplicationTests;
import lombok.SneakyThrows;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.RebalancePushImpl;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.vergilyn.examples.rocketmq.RocketDefinedGenerator.*;

/**
 *
 * @author vergilyn
 * @since 2022-07-07
 *
 * @see org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration#registerContainer(String, Object)
 */
@SuppressWarnings("JavadocReference")
public class ConsumerCustomOffsetTests extends AbstractRocketMQApplicationTests {

	private static final String POSTFIX = "consumer_custom_offset";
	private static final String GROUP_CONSUMER = PREFIX_GROUP_CONSUMER + POSTFIX + "_n004";
	private static final String GROUP_PROVIDER = PREFIX_GROUP_PRODUCER + POSTFIX;
	private static final String TOPIC = PREFIX_TOPIC + POSTFIX;
	private static final String TAG = PREFIX_TAG + POSTFIX;

	/**
	 * 消费者端 通过代码自定义消费位点。
	 * 例如，期望指定 timestamp之后的消息开始消费。或者指定 offset之后的数据开始消费。
	 *
	 * <b>结论：</b>
	 * <p> 1. 最简单的方式，是consumer消费历史消息。但根据消息的{@link MessageExt#bornTimestamp} 或者 {@link MessageExt#storeTimestamp}
	 *   在消费代码中判断是否执行后续消费逻辑。
	 *
	 * <p> 2. 通过{@link RocketMQPushConsumerLifecycleListener#prepareStart(Object)} 指定。
	 *   <br/> 需要深入理解 ConsumeFromWhere、offset 之类的，否则设置成功也不一定有效。
	 */
	@SneakyThrows
	@Test
	public void annotationListener(){
		// 1.
		sendDefaultMsg(3, TOPIC, TAG, GROUP_PROVIDER);

		TimeUnit.SECONDS.sleep(2);

		registryAnnoRocketMQListener(ConsumerCustomOffsetTests.ConsumerCustomOffsetListener.class);

		preventExit();
	}

	@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
			consumerGroup = GROUP_CONSUMER,
			topic = TOPIC,
			selectorType = SelectorType.TAG,
			selectorExpression = TAG,
			consumeMode = ConsumeMode.ORDERLY,
			consumeThreadMax = 1
	)
	public static class ConsumerCustomOffsetListener implements RocketMQListener<MessageExt>,
			RocketMQPushConsumerLifecycleListener {
		private AtomicInteger index = new AtomicInteger(0);

		/**
		 * 示例代码：topic已存在且已有消息，consumer不管是全新的，还是已经存在的。
		 *   通过下面代码可以成功设置`ConsumeFromWhere & ConsumeTimestamp`，但实际还是会消费历史消息。
		 *
		 * <p> consumer启动时获取offset：{@link RebalancePushImpl#computePullFromWhereWithException(MessageQueue)}，
		 *   然后内部会特殊处理（参考`文章1`），强制返回`offset = 0`，所以导致实际上这个consumer还是会消费历史消息。
		 *
		 * <p><h3>无效原因参考</h3>
		 * <p> 1. <a href="https://zhuanlan.zhihu.com/p/163248992">消费者起始点管理，为什么我的CONSUME_FROM_LAST不生效？</a>
		 * <br/> <b>强烈推荐。</b>
		 * <pre>
		 *  可以查看 `\broker\store\config\consumerOffset.json`
		 *      例如 "vergilyn_topic_consumer_custom_offset@vergilyn_consumer_group_consumer_custom_offset":{0:5,1:8,2:8,3:6}
		 *      格式 `topic@consumer_group_name:{queue-id:offset...}`
		 *
		 *  由上可知，新消费者指：`consumer + topic + queue`。 并不是只有 全新的consumer-group 才是新消费者。
		 *  （例如 扩容broker，增加queue，已存在的consumer-group监听一个新的topic等）
		 * </pre>
		 *
		 * <p> 1.1 <a href="https://zhuanlan.zhihu.com/p/25265380">RocketMQ原理（4）——消息ACK机制及消费进度管理</a>
		 *
		 * <p> 2. <a href="https://blog.csdn.net/xmcy001122/article/details/107062441/">ConsumeFromTimestamp的注意事项</a>
		 * <br/> 这文章也没有完全解释，即使是new-consumer-group第1次启动也不一定会遵循“CONSUME_FROM_TIMESTAMP”，详见上面篇文章中的解释。
		 *
		 */
		@Override
		public void prepareStart(DefaultMQPushConsumer consumer) {
			consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);

			/**
			 * Backtracking consumption time with second precision. Time format is
			 * 20131223171201<br>
			 * Implying Seventeen twelve and 01 seconds on December 23, 2013 year<br>
			 * Default backtracking consumption time Half an hour ago.
			 */
			// private String consumeTimestamp = UtilAll.timeMillisToHumanString3(System.currentTimeMillis() - (1000 * 60 * 30));
			consumer.setConsumeTimestamp(UtilAll.timeMillisToHumanString3(System.currentTimeMillis()));

		}

		@Override
		public void onMessage(MessageExt message) {
			System.out.printf("[%02d][rocketMQ-consumer] >>>> ", index.getAndIncrement());

			// "{\"now\": \"2022-07-06T16:40:53.196\", \"index\": 0}"
			String body = new String(message.getBody(), StandardCharsets.UTF_8);
			int reconsumeTimes = message.getReconsumeTimes();
			String keys = message.getKeys();
			System.out.printf("keys: %s, reconsumeTimes: %s, body: %s \n", keys, reconsumeTimes, body);

			// JSONObject jsonObject = JSON.parseObject(body);
			// Integer index = jsonObject.getInteger("index");
			//
			// if (index == 1){
			// 	throw new RuntimeException("主动消费异常，期望reconsume");
			// }
		}
	}
}
