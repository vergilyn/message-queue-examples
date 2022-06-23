package com.vergilyn.examples.rocketmq.feature;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.rocketmq.AbstractRocketMQApplicationTests;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.PullAPIWrapper;
import org.apache.rocketmq.client.impl.consumer.PullMessageService;
import org.apache.rocketmq.client.impl.consumer.RebalanceService;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("JavadocReference")
@Import(AnnotationListenerTests.HelloworldAnnotationListener.class)
public class AnnotationListenerTests extends AbstractRocketMQApplicationTests {

	@Test
	public void annotationListener(){
		preventExit();
	}

	/**
	 * 监听消费 push 模式。<br/>
	 * 模糊记得，rocketmq 的push不是真正的由namesrv push，而只是SDK封装了pull过程，并且允许registry-listener，
	 * 实际还是consumer-side通过轮询去pull消息。
	 *
	 * <p>
	 *   consumer-side相关push源码，证明最终都是pull模式
	 *   <pre>
	 *     - {@linkplain ListenerContainerConfiguration#registerContainer(java.lang.String, java.lang.Object)}
	 *     - {@linkplain DefaultRocketMQListenerContainer#start()}
	 *     - {@linkplain DefaultMQPushConsumer#start()}
	 *     - {@linkplain DefaultMQPushConsumerImpl#start()}
	 *     - {@linkplain MQClientInstance#start()}
	 *       - {@linkplain PullMessageService#run()}
	 *       - {@linkplain RebalanceService#run()} 主要是这里面的`doRebalance()`在启动时会`queue.put(...)`
	 *     - {@linkplain DefaultMQPushConsumerImpl#pullMessage(org.apache.rocketmq.client.impl.consumer.PullRequest)} 注意匿名内部类`PullCallback`
	 *     - {@linkplain PullAPIWrapper#pullKernelImpl(org.apache.rocketmq.common.message.MessageQueue, java.lang.String, java.lang.String, long, long, int, int, long, long, long, org.apache.rocketmq.client.impl.CommunicationMode, org.apache.rocketmq.client.consumer.PullCallback)}
	 *   </pre>
	 * </p>
	 */
	@org.apache.rocketmq.spring.annotation.RocketMQMessageListener(
			consumerGroup = HelloworldAnnotationListener.ANNOTATION_CONSUMER_GROUP,
			topic = "vergilyn_topic_helloworld",
			selectorType = SelectorType.TAG,
			selectorExpression = "*",
			consumeMode = ConsumeMode.CONCURRENTLY
	)
	public static class HelloworldAnnotationListener implements RocketMQListener<Message> {
		public static final String ANNOTATION_CONSUMER_GROUP = "vergilyn_consumer_group_helloworld_anno";
		private static final AtomicInteger _index = new AtomicInteger(0);

		/**
		 * 可以通过debug，查看dump-stack
		 *
		 * <pre>
		 *   - {@linkplain DefaultMQPushConsumerImpl#pullMessage(org.apache.rocketmq.client.impl.consumer.PullRequest)}
		 *   - {@linkplain PullAPIWrapper#processPullResult(org.apache.rocketmq.common.message.MessageQueue, org.apache.rocketmq.client.consumer.PullResult, org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData)}
		 *   - {@linkplain org.apache.rocketmq.remoting.netty.RequestTask}
		 *   - ...
		 *   - {@linkplain org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer.DefaultMessageListenerConcurrently#consumeMessage(List, ConsumeConcurrentlyContext)}
		 *   - {@linkplain DefaultRocketMQListenerContainer#handleMessage(org.apache.rocketmq.common.message.MessageExt)}
		 * </pre>
		 *  由上可知，`@RocketMQMessageListener` 监听方法返回 void，
		 *  如果异常会返回{@linkplain ConsumeOrderlyStatus#SUSPEND_CURRENT_QUEUE_A_MOMENT} 或 {@linkplain ConsumeConcurrentlyStatus#RECONSUME_LATER}。
		 *  否则返回`CONSUME_SUCCESS`。（注意区分 orderly 和 concurrently）
		 *
		 * @see org.apache.rocketmq.common.message.Message
		 * @see org.apache.rocketmq.common.message.MessageExt
		 * @see org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer.DefaultMessageListenerConcurrently
		 * @see org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer.DefaultMessageListenerOrderly
		 */
		@Override
		public void onMessage(Message message) {
			int index = _index.incrementAndGet();
			System.out.printf("[%d][%s] message.class >>>> %s \n",
			                  index, ANNOTATION_CONSUMER_GROUP, message.getClass().getName());

			System.out.printf("[%d][%s] message >>>> %s \n",
			                  index, ANNOTATION_CONSUMER_GROUP, JSON.toJSONString(message, true));

			System.out.printf("[%d][%s] body >>>> %s \n",
			                  index, ANNOTATION_CONSUMER_GROUP, new String(message.getBody()));

			System.out.println();
		}

	}
}
