package com.vergilyn.examples.rocketmq;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RocketClientUtils {

	public static DefaultMQProducer createProducer(String producerGroup) {
		return createProducer(producerGroup, RocketConstants.NAMESRV_ADDR);
	}

	public static DefaultMQProducer createProducer(String producerGroup, String namesrvAddr) {
		DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
		producer.setNamesrvAddr(namesrvAddr);
		producer.setVipChannelEnabled(false);

		return producer;
	}

	public static void sendMsg(String producerGroup, List<Message> messages){
		sendMsg(producerGroup, messages, DefaultMQProducer::send);
	}

	public static void sendMsg(String producerGroup, List<Message> messages, BiFunction<DefaultMQProducer, Message, SendResult> function){
		DefaultMQProducer producer = RocketClientUtils.createProducer(producerGroup);
		try {
			producer.start();

			for (Message message : messages) {

				SendResult sendResult = function.apply(producer, message);

				String key = message.getKeys();
				String topic = message.getTopic();
				String tags = message.getTags();
				String body = new String(message.getBody(), StandardCharsets.UTF_8);

				int queueId = sendResult.getMessageQueue().getQueueId();

				System.out.printf("[vergilyn][rocketmq-producer][qid-%s] >>>> keys: %s, tags: %s, body: %s \n",
				                  queueId, key, tags, body);
			}

		} catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
			e.printStackTrace();
		}finally {
			producer.shutdown();
		}
	}

	public static String getBodyString(Message message){
		return new String(message.getBody(), StandardCharsets.UTF_8);
	}

	public static void printMessage(MessageExt message){
		String key = message.getKeys();
		String topic = message.getTopic();
		String tags = message.getTags();
		String body = new String(message.getBody(), StandardCharsets.UTF_8);

		int queueId = message.getQueueId();
		Thread currentThread = Thread.currentThread();

		System.out.printf("[vergilyn][rocketmq-consumer][qid-%s][thread-%s] >>>> keys: %s, tags: %s, body: %s \n",
		                  queueId, currentThread.getName(), key, tags, body);
	}

	@FunctionalInterface
	public static interface BiFunction<A, B, R> {

		R apply(A a, B b) throws MQClientException, RemotingException, MQBrokerException, InterruptedException;

	}
}
