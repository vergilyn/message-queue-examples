package com.vergilyn.examples.rocketmq.order;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vergilyn.examples.rocketmq.order.OrderConsumerConstants.*;

@Slf4j
public class OrderProducerTests {

	/**
	 * <p> 1. 如果需要顺序消费，生产者一定要先保证有序！
	 * <p> 2. {@link DefaultMQProducer#send(Message, MessageQueueSelector, Object)}
	 */
	@Test
	public void test(){
		List<Message> messages = Lists.newArrayList(
			createMsg(TAG_ORDER_1, "A"),

			createMsg(TAG_ORDER_1, "B"),
			createMsg(TAG_ORDER_2, "B"),

			createMsg(TAG_ORDER_1, "C"),
			createMsg(TAG_ORDER_2, "C"),
			createMsg(TAG_ORDER_3, "C"),

			createMsg(TAG_OTHER_1, "other_0001"),
			createMsg(TAG_IGNORE_1, "ignore_0001"),

			createMsg(TAG_ORDER_3, "B"),

			createMsg(TAG_ORDER_2, "A"),

			createMsg(TAG_OTHER_1, "other_0002"),
			createMsg(TAG_IGNORE_1, "ignore_0002"),

			createMsg(TAG_ORDER_3, "A")
		);

		sendMsg(messages);
	}

	private Message createMsg(String tag, String message){
		String key = MessageClientIDSetter.createUniqID();
		return new Message(TOPIC, tag, key, message.getBytes());
	}

	private void sendMsg(List<Message> messages){
		DefaultMQProducer producer = createProducer(OrderConsumerConstants.GROUP_PRODUCER);
		try {
			producer.start();

			// FIXME 2022-01-06，rocketMQ-4.9.1 批量发送存在bug。未正确保存`tags` https://github.com/apache/rocketmq/issues/3476
			// producer.send(messages);

			for (Message message : messages) {

				SendResult sendResult = producer.send(message, new MessageQueueSelector() {
					@Override
					public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
						String bodyString = getBodyString(msg);
						int index;

						if ("A".equalsIgnoreCase(bodyString)){
							index = 0;
						}else if ("B".equalsIgnoreCase(bodyString)){
							index = 1;
						}else if ("C".equalsIgnoreCase(bodyString)){
							index = 2;
						}
						else {
							index = RandomUtils.nextInt(0, mqs.size());
						}

						return mqs.get(index);
					}
				}, message.getKeys());

				String key = message.getKeys();
				String topic = message.getTopic();
				String tags = message.getTags();
				String body = new String(message.getBody(), StandardCharsets.UTF_8);

				int queueId = sendResult.getMessageQueue().getQueueId();

				System.out.printf("[vergilyn][rocketmq-producer][qid-%s] >>>> keys: %s, tags: %s, body: %s \n",
				                  queueId, key, tags, body);
				// 2022-07-06，
				TimeUnit.MILLISECONDS.sleep(100);
			}

		} catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
			log.error(e.getMessage(), e);
		}finally {
			producer.shutdown();
		}
	}

	protected DefaultMQProducer createProducer(String producerGoup) {
		DefaultMQProducer producer = new DefaultMQProducer(producerGoup);
		producer.setNamesrvAddr(OrderConsumerConstants.NAMESRV_ADDR);
		producer.setVipChannelEnabled(false);

		return producer;
	}

	protected String getBodyString(Message message){
		return new String(message.getBody(), StandardCharsets.UTF_8);
	}

}
