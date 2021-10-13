package com.vergilyn.examples.rabbitmq.feature;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import com.rabbitmq.client.Channel;
import com.vergilyn.examples.rabbitmq.AbstractRabbitMQApplicationTests;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import static com.vergilyn.examples.rabbitmq.constants.RabbitAutoDeclareEnum.BATCH_GET_ACK;

@Import(BatchGetAckTests.MessageProducer.class)
public class BatchGetAckTests extends AbstractRabbitMQApplicationTests {
	public static final int BATCH_CONTAINER_BATCH_SIZE = 10;
	public static final long BATCH_CONTAINER_RECEIVE_TIMEOUT = TimeUnit.SECONDS.toMillis(3);

	@Test
	public void test(){

	}

	/**
	 * @author vergilyn
	 * @date 2020-06-08
	 *
	 * @see BatchMessageListener
	 * @see org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener
	 */
	@Component("batchGetAckListener")
	@Slf4j
	public static class BatchGetAckListener{
		private static final AtomicInteger INDEX = new AtomicInteger(0);
		private static final AtomicInteger TOTAL = new AtomicInteger(0);

		@RabbitListener(queues = "queue.batch-get-ack", containerFactory = "batchGetAckRabbitListenerContainerFactory")
		public void onMessageBatch(List<Message> messages, Channel channel) {
			System.out.printf("[%s][%d] begin >>>> size: %d, total: %d \r\n", LocalTime.now().toString(), INDEX.incrementAndGet(),
			                  messages.size(), TOTAL.addAndGet(messages.size()));

			long deliveryTag = 0;
			for (Message message : messages){
				System.out.println(new String(message.getBody()));

				deliveryTag = message.getMessageProperties().getDeliveryTag();
			}

			try {
				channel.basicNack(deliveryTag, true, false);
			} catch (IOException e) {
				log.error("error >>>> {}", e.getMessage());
			}
		}

	}

	/**
	 * 观察 consumer-side 对批量消息的处理。
	 * <p>1. 注意provider-side并未使用{@link BatchingRabbitTemplate}，而是普通的{@link RabbitTemplate}单条发送。
	 *
	 * <p>2. {@link BatchSendMessageTest} 中用的是 {@link BatchingRabbitTemplate}。
	 * @author vergilyn
	 * @date 2020-06-15
	 *
	 * @see BatchGetAckListener
	 */
	@Slf4j
	public static class MessageProducer {
		@Autowired
		private RabbitTemplate rabbitTemplate;

		@SneakyThrows
		@PostConstruct
		public void producer(){
			AtomicInteger total = new AtomicInteger(33);
			AtomicInteger index = new AtomicInteger(0);

			// send messages continuously
			for(int i = 0, len = BATCH_CONTAINER_BATCH_SIZE + 3; i < len; i++){
				send();
				index.incrementAndGet();
			}

			// +1s for ensure receive-timeout
			TimeUnit.MILLISECONDS.sleep(BATCH_CONTAINER_RECEIVE_TIMEOUT + 1000L);

			// send messages at random intervals (1s ~ 4s). when grander than 3s(receive-timeout) execute consume-handler
			HashedWheelTimer timer = new HashedWheelTimer();
			timer.newTimeout(new TimerTask() {
				@Override
				public void run(Timeout timeout) throws Exception {
					send();
					if (index.incrementAndGet() < total.get()){
						timer.newTimeout(this, RandomUtils.nextInt(1, 4), TimeUnit.SECONDS);
					}else {
						System.out.printf("[%s]provider sends message completed. total: %d \r\n", LocalTime.now().toString(), total.get());
					}
				}
			}, 0, TimeUnit.SECONDS);
		}

		private void send(){
			final String message = LocalTime.now().toString();
			rabbitTemplate.convertAndSend(BATCH_GET_ACK.exchange, BATCH_GET_ACK.routing, message);
			log.info("[vergilyn] send message success. exchange: {}, routing: {}, message: {}",
			                    BATCH_GET_ACK.exchange, BATCH_GET_ACK.routing, message);
		}
	}
}
