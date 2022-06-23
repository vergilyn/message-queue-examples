package com.vergilyn.examples.rocketmq.feature;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.rocketmq.AbstractRocketMQTests;
import com.vergilyn.examples.rocketmq.constants.RocketDefinedGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SuppressWarnings("JavadocReference")
public class HelloworldRocketMQTests extends AbstractRocketMQTests {
	private final RocketDefinedGenerator helloworld = new RocketDefinedGenerator("helloworld");

	/**
	 * 参考 rocketmq-spring-boot：<br/>
	 * producer 可以是单例对象，并且{@linkplain DefaultMQProducer#start()}后 一直使用，
	 * 直到 destroy-bean 时调用 {@linkplain DefaultMQProducer#shutdown()}
	 *
	 * @see org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration#defaultMQProducer(RocketMQProperties)
	 * @see org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration#rocketMQTemplate(RocketMQMessageConverter)
	 * @see RocketMQTemplate#afterPropertiesSet()
	 * @see RocketMQTemplate#destroy()
	 */
	@Test
	public void producer(){
		DefaultMQProducer producer = new DefaultMQProducer(helloworld.producerGroup());
		producer.setNamesrvAddr(NAMESRV_ADDR);
		producer.setVipChannelEnabled(false);

		try {
			producer.start();
			// producer.createTopic("t_p_key", MESSAGE_TOPIC, 5);

			final LocalDateTime now = LocalDateTime.now();
			Message message = new Message(helloworld.topic(),
			                              helloworld.tag(),
			                              generatorKey(now),
			                              JSON.toJSONBytes(String.format("{\"now\": \"%s\"}", now)));

			// producer.sendOneway();
			producer.send(message);
		} catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
			log.error(e.getMessage(), e);
		}finally {
			producer.shutdown();
		}
	}

	/**
	 * 监听消费 push 模式。<br/>
	 * 模糊记得，rocketmq 的push不是真正的由namesrv push，而只是SDK封装了pull过程，并且允许registry-listener，
	 * 实际还是consumer-side通过轮询去pull消息。
	 *
	 * @see AnnotationListenerTests
	 * @see org.apache.rocketmq.spring.annotation.RocketMQMessageListener
	 * @see org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration
	 * @see org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration#defaultLitePullConsumer(RocketMQProperties)
	 */
	@Test
	public void consumerListener(){
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(helloworld.consumerGroup());
		consumer.setNamesrvAddr(NAMESRV_ADDR);
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
			consumer.subscribe(helloworld.topic(), helloworld.tag());

			// 单线程消费
			consumer.setConsumeThreadMax(1);
			consumer.setConsumeThreadMin(1);

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

		preventExit();
	}
}
