package com.vergilyn.examples.rocketmq.feature.consumer;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 同一个ConsumerGroup 多实例消费 相同的tag。
 *
 * @author vergilyn
 * @since 2021-10-20
 */
@Slf4j
public class ClusteringSingleConsumerGroupMultiInstanceTests extends AbstractRocketMQTests {
	private final RocketDefinedGenerator msgDefined = new RocketDefinedGenerator(ClusteringSingleConsumerGroupMultiInstanceTests.class.getSimpleName());

	private final AtomicInteger msgCounter = new AtomicInteger(0);
	/**
	 *
	 * 一个ConsumerGroup中的Consumer实例平均分摊消费生产者发送的消息。
	 * 例如某个Topic有九条消息，其中一个Consumer Group有三个实例（可能是3个进程，或者3台机器），
	 * 那么每个实例只消费其中的3条消息（实际不会这么平均），Consumer不指定消费方式的话默认是集群消费的，适用于大部分消息的业务
	 */
	@Test
	public void test(){
		// 生产数据
		Integer msgSize = 9;
		producer(msgSize);

		ExecutorService threadPool = Executors.newFixedThreadPool(3);
		threadPool.submit(() -> startConsumerListener(0));
		threadPool.submit(() -> startConsumerListener(1));
		threadPool.submit(() -> startConsumerListener(2));

		Awaitility.await().atMost(1, TimeUnit.MINUTES)
				.untilAsserted(() -> Assertions.assertEquals(msgSize, msgCounter.get()));
	}

	/**
	 * <pre>
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[1], message: "{\"now\": \"2021-10-20T10:41:13.774\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[2], message: "{\"now\": \"2021-10-20T10:41:11.759\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[2], message: "{\"now\": \"2021-10-20T10:41:19.821\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[1], message: "{\"now\": \"2021-10-20T10:41:21.829\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[0], message: "{\"now\": \"2021-10-20T10:41:09.748\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[0], message: "{\"now\": \"2021-10-20T10:41:17.809\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[0], message: "{\"now\": \"2021-10-20T10:41:06.669\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[0], message: "{\"now\": \"2021-10-20T10:41:15.792\"}"
	 * consumer >>>> consumerGroup: vergilyn_consumer_group_clustering[0], message: "{\"now\": \"2021-10-20T10:41:23.839\"}"
	 * </pre>
	 */
	public void startConsumerListener(int index){
		try {
			DefaultMQPushConsumer consumer = createConsumer(msgDefined);
			consumer.setPullBatchSize(1);
			consumer.setMessageModel(MessageModel.CLUSTERING);

			consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

				for (MessageExt messageExt : msgs) {
					System.out.printf("consumer[num-%d] >>>> consumerGroup: %s[%d], message: %s \n",
					                  msgCounter.incrementAndGet(),
					                  consumer.getConsumerGroup(), index, new String(messageExt.getBody()));
				}

				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			});

			consumer.start();
		} catch (MQClientException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void producer(int size){
		DefaultMQProducer producer = createProducer(msgDefined);
		try {
			producer.start();

			AtomicInteger num = new AtomicInteger();
			for (int i = 0; i < size; i++) {
				final Message message = createMessage();
				producer.send(message);
				System.out.printf("producer[num-%d] >>>> body: %s \n", num.incrementAndGet(), new String(message.getBody()));
				TimeUnit.SECONDS.sleep(2);
			}

		} catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
			log.error(e.getMessage(), e);
		}finally {
			producer.shutdown();
		}
	}

	private Message createMessage(){
		final LocalDateTime now = LocalDateTime.now();
		Message message = new Message(msgDefined.topic(),
		                              msgDefined.tag(),
		                              generatorKey(now),
		                              JSON.toJSONBytes(String.format("{\"now\": \"%s\"}", now)));

		return message;
	}
}
