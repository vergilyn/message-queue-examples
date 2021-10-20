package com.vergilyn.examples.rocketmq.feature.consumer;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vergilyn.examples.rocketmq.AbstractRocketMQTests;
import com.vergilyn.examples.rocketmq.constants.RocketDefinedGenerator;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 多个ConsumerGroup 集群消费
 *
 * @author vergilyn
 * @since 2021-10-20
 */
@Slf4j
public class ClusteringDiffConsumerSingleInstanceGroupTests extends AbstractRocketMQTests {
	private final RocketDefinedGenerator msgDefined = new RocketDefinedGenerator(
			ClusteringDiffConsumerSingleInstanceGroupTests.class.getSimpleName());

	private final AtomicInteger msgCounter = new AtomicInteger(0);

	/**
	 * 每个ConsumerGroup都会消费1次 （broadcasting 和 clustering 效果一样）
	 */
	@Test
	public void test(){
		producer();

		ExecutorService threadPool = Executors.newFixedThreadPool(3);
		threadPool.submit(() -> startConsumerListener(0));
		threadPool.submit(() -> startConsumerListener(1));
		threadPool.submit(() -> startConsumerListener(2));

		Awaitility.await().atMost(1, TimeUnit.MINUTES)
				.untilAsserted(() -> Assertions.assertEquals(3, msgCounter.get()));
	}

	public void startConsumerListener(int index){
		try {
			DefaultMQPushConsumer consumer = createConsumer(msgDefined);
			consumer.setConsumerGroup(consumer.getConsumerGroup() + "_" + index);
			consumer.setPullBatchSize(1);
			consumer.setMessageModel(MessageModel.CLUSTERING);

			consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

				for (MessageExt messageExt : msgs) {
					System.out.printf("consumer[num-%d] >>>> consumerGroup: %s, messageModel: %s, message: %s \n",
					                  msgCounter.incrementAndGet(),
									  consumer.getMessageModel(),
					                  consumer.getConsumerGroup(),
					                  new String(messageExt.getBody()));
				}

				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			});

			consumer.start();
		} catch (MQClientException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void producer(){
		DefaultMQProducer producer = createProducer(msgDefined);
		try {
			producer.start();

			final Message message = createMessage(msgDefined, LocalDateTime.now());
			producer.send(message);
			System.out.printf("producer >>>> body: %s \n", new String(message.getBody()));

		} catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
			log.error(e.getMessage(), e);
		}finally {
			producer.shutdown();
		}
	}
}
