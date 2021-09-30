package com.vergilyn.examples.rabbitmq.feature;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.rabbitmq.client.Channel;
import com.vergilyn.examples.constants.MessageModeEnum;
import com.vergilyn.examples.javabean.MessageDto;
import com.vergilyn.examples.javabean.RabbitMode;
import com.vergilyn.examples.rabbitmq.AbstractRabbitMQApplicationTests;
import com.vergilyn.examples.rabbitmq.constants.RabbitDefinedGenerator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Import({AnnotationModeTests.AnnotationConfiguration.class, AnnotationModeTests.ConsumerListener.class })
public class AnnotationModeTests extends AbstractRabbitMQApplicationTests {
	public static final RabbitDefinedGenerator ANNOTATION_MODE = new RabbitDefinedGenerator("annotation-mode");

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Test
	public void producer(){
		MessageDto messageDto = MessageDto.builder()
				.date(LocalDateTime.now())
				.str(JSON.toJSONString(ANNOTATION_MODE))
				.build();

		rabbitTemplate.convertAndSend(ANNOTATION_MODE.exchange(), ANNOTATION_MODE.routing(), messageDto.toString());

		preventExit();
	}

	/*
	*/

	/**
	 *
	 * <pre>
	 * {@code
	 * @RabbitListener(
	 *         admin = "",
	 *         containerFactory = "",
	 *         bindings = @QueueBinding(
	 *                 value = @Queue(value = "vergilyn.queue.xml-mode"),
	 *                 exchange = @Exchange(value = "vergilyn.queue.xml-mode"),
	 *                 key = "vergilyn.routing.xml-mode",
	 *                 arguments = {})
	 * 	)
	 * }
	 * </pre>
	 *
	 * 或者 {@linkplain AnnotationConfiguration#annoMessageListener(ConnectionFactory, Queue, ConsumerListener)}
	 *
	 * @see org.springframework.amqp.rabbit.annotation.RabbitListener
	 */
	@Component
	@Slf4j
	public static class ConsumerListener implements ChannelAwareMessageListener {

		@Override
		public void onMessage(Message message, Channel channel) throws Exception {
			MessageProperties properties = message.getMessageProperties();
			long deliveryTag = properties.getDeliveryTag();
			String body = new String(message.getBody(), StandardCharsets.UTF_8);
			MessageDto messageDto = JSON.parseObject(body, MessageDto.class);
			RabbitMode rabbitMode = messageDto.getRabbitMode();

			log.info("consumer-queue >>>> {}, body: {}", properties.getConsumerQueue(), body);

			if (rabbitMode == null){
				channel.basicAck(deliveryTag, false);
				return;
			}

			if (rabbitMode.getMode() == MessageModeEnum.RABBIT_ACK){
				channel.basicAck(deliveryTag, rabbitMode.isMultiple());

			}else if(rabbitMode.getMode() == MessageModeEnum.RABBIT_REJECT){
				// 2. 拒绝单条消息：true, 被拒绝的消息重新排队（更靠近"队首"，而不是"队尾"）；false，消息被丢弃discard，或加入死信队列dead-letter
				channel.basicReject(deliveryTag, rabbitMode.isRequeue());

			}else if(rabbitMode.getMode() == MessageModeEnum.RABBIT_NACK){
				// 3. nack支持批量拒绝
				channel.basicNack(deliveryTag, rabbitMode.isMultiple(), rabbitMode.isRequeue());

			}else {
				channel.basicAck(deliveryTag, rabbitMode.isMultiple());
			}
		}
	}

	@TestConfiguration
	public static class AnnotationConfiguration {
		@Bean("annoQueue")
		public Queue annoQueue(){
			return new Queue(ANNOTATION_MODE.queue());
		}

		/* type: x-delayed-message
		 * DM: message-delayed:0
		 * args: x-delayed-type:direct
		 *
		 * 最终rabbitmq的type: x-delayed-message
		 */
		@Bean("annoExchange")
		public Exchange annoExchange(){
			Map<String, Object> arguments = Maps.newHashMap();
			arguments.put("x-delayed-type", "direct");

			DirectExchange exchange = new DirectExchange(ANNOTATION_MODE.exchange(), true, false, arguments);
			// false: 最终并不会是x-delayed-message，而是direct
			exchange.setDelayed(true);

			return exchange;
		}

		@Bean
		public Binding binding(@Qualifier("annoQueue") Queue queue, @Qualifier("annoExchange") Exchange exchange){
			BindingBuilder.GenericArgumentsConfigurer configurer = BindingBuilder
					.bind(queue)
					.to(exchange)
					.with(ANNOTATION_MODE.routing());
			// configurer.and(Collections.emptyMap());
			return configurer.noargs();
		}

		/**
		 * consumer-listener：ack-manual模式.
		 * @see org.springframework.amqp.rabbit.annotation.RabbitListener
		 */
		@Bean
		public MessageListenerContainer annoMessageListener(ConnectionFactory connectionFactory,
				@Qualifier("annoQueue") Queue annoQueue,
				ConsumerListener consumerListener){
			SimpleMessageListenerContainer listener = new SimpleMessageListenerContainer(connectionFactory);
			listener.setQueues(annoQueue);   // 需要监听的队列
			listener.setAcknowledgeMode(AcknowledgeMode.MANUAL);  // 手动应答模式
			// listener.setMaxConcurrentConsumers();
			// listener.setConcurrentConsumers();
			// listener.setMessagePropertiesConverter();

			listener.setMessageListener(consumerListener);  // FIXME 不清楚原理，这么写不一定线程安全。
			// listener.setMessageListener(new ConsumerListener());    // 可能需要这么写，或用@RabbitListener实现

			return listener;
		}
	}

}
