package com.vergilyn.examples.rocketmq.feature;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vergilyn.examples.rocketmq.AbstractRocketMQApplicationTests;
import com.vergilyn.examples.rocketmq.constants.RocketDefinedGenerator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * 期望：限制消费频率，例如 100qps。 <br/>
 * 场景：消费者消费消息，请求dingtalk接口。 dingtalk接口限制 每秒钟最多调用 100次。 <br/>
 *
 * @author vergilyn
 * @since 2021-11-10
 */
@Slf4j
@Import(ConsumerFrequencyTests.Listener.class)
public class ConsumerFrequencyTests extends AbstractRocketMQApplicationTests {
	private static final String CONSUMER_GROUP = "ConsumerFrequencyTests";
	private final RocketDefinedGenerator defined = new RocketDefinedGenerator("consumer-frequency");

	@SneakyThrows
	@Test
	public void test(){
		sendDefaultMsg(10, defined);

		TimeUnit.SECONDS.sleep(30);
	}

	@RocketMQMessageListener(
			consumerGroup = ConsumerFrequencyTests.CONSUMER_GROUP,
			topic = "vergilyn_topic_consumer-frequency",
			consumeThreadMax = 4
	)
	public static class Listener implements RocketMQListener<Message> {
		private static final AtomicInteger _index = new AtomicInteger(0);

		@Override
		public void onMessage(Message message) {

			System.out.printf("[%02d][%s][%s] body >>>> %s \n",
			                  _index.incrementAndGet(), LocalTime.now(), Thread.currentThread().getName(), new String(message.getBody()));

			System.out.println();
		}

	}

	public void consumer() throws MQClientException {
		DefaultMQPushConsumer consumer = createConsumer(defined);
		/*
		 * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
		 * 如果非第一次启动，那么按照上次消费的位置继续消费
		 */
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		try {
			/*
			 * @param topic
			 *      topic to subscribe.
			 * @param subExpression
			 *      subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br>
			 *      if null or * expression,meaning subscribe all
			 */
			consumer.subscribe(defined.topic(), defined.tag());

			// 单线程消费
			consumer.setConsumeThreadMax(64);
			consumer.setConsumeThreadMin(1);

			// 每次拉取数量 和 间隔
			consumer.setPullBatchSize(100);
			consumer.setPullInterval(1000);

			final AtomicInteger index = new AtomicInteger(0);
			consumer.registerMessageListener((MessageListenerConcurrently) (list, context) -> {
				String logPrefix = "index-" + index.incrementAndGet() + " >>>>";
				MessageExt msg = list.get(0);
				long offset = msg.getQueueOffset();
				System.out.printf("%s size: %s, offset: %s \n", logPrefix, list.size(), offset);

				// msg.getProperty(MessageConst.PROPERTY_MAX_OFFSET);

				list.forEach(e -> {
					System.out.printf("%s message-body: %s \n", logPrefix, new String(e.getBody(), StandardCharsets.UTF_8));
				});

				System.out.println();
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			});
			consumer.start();
		} catch (MQClientException e) {
			e.printStackTrace();
		}

	}
}
