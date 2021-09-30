package com.vergilyn.examples.rabbitmq.feature;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.vergilyn.examples.javabean.MessageDto;
import com.vergilyn.examples.rabbitmq.AbstractRabbitMQApplicationTests;
import com.vergilyn.examples.rabbitmq.constants.RabbitDefinedGenerator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

@Import({XmlModeTests.XmlConfiguration.class, XmlModeTests.ConsumerListener.class })
public class XmlModeTests extends AbstractRabbitMQApplicationTests {
	public static final RabbitDefinedGenerator XML_MODE = new RabbitDefinedGenerator("xml-mode");

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Configuration
	@ImportResource("classpath:/com/vergilyn/examples/rabbitmq/feature/xml-mode.xml")
	public static class XmlConfiguration implements BeanDefinitionRegistryPostProcessor {
		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			// 目的，只是为了自动生成需要的：queue、exchange，以及bind关系。（也可以通过其他方式，例如`@Bean`）

			Queue queue = new Queue(XML_MODE.queue());
			beanFactory.registerSingleton(queue.getName(), queue);

			Exchange exchange = new FanoutExchange(XML_MODE.exchange());
			beanFactory.registerSingleton(exchange.getName(), exchange);

			Binding binding = BindingBuilder
					.bind(queue)
					.to(exchange)
					.with(XML_MODE.routing())
					.noargs();
			beanFactory.registerSingleton(XML_MODE.routing(), binding);
		}
	}

	@Test
	public void producer(){
		MessageDto messageDto = MessageDto.builder()
				.date(LocalDateTime.now())
				.str(JSON.toJSONString(XML_MODE))
				.build();

		rabbitTemplate.convertAndSend(XML_MODE.exchange(), XML_MODE.routing(), messageDto.toString());
		System.out.println("producer send message >>>> " + messageDto);

		preventExit();
	}

	@Component("consumerListener")
	@Slf4j
	public static class ConsumerListener implements ChannelAwareMessageListener {

		@Override
		public void onMessage(Message message, Channel channel) throws Exception {
			MessageProperties properties = message.getMessageProperties();
			long deliveryTag = properties.getDeliveryTag();
			String body = new String(message.getBody(), StandardCharsets.UTF_8);

			log.info("consumer-queue >>>> {}, body: {}", properties.getConsumerQueue(), body);

			channel.basicAck(deliveryTag, false);
		}
	}
}
