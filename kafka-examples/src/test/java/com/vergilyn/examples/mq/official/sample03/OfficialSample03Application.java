package com.vergilyn.examples.mq.official.sample03;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.vergilyn.examples.mq.common.Foo1;
import com.vergilyn.examples.mq.common.Foo2;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.util.StringUtils;

/**
 * <a href="https://github.com/spring-projects/spring-kafka/tree/master/samples/sample-03">Sample showing a batch listener and transactions.</a>
 *
 * @author vergilyn
 * @date 2020-05-18
 */
@SpringBootApplication
@Slf4j
public class OfficialSample03Application {
	private final static CountDownLatch LATCH = new CountDownLatch(1);
	@Autowired
	private KafkaTemplate<Object, Object> kafkaTemplate;

	public static void main(String[] args) throws InterruptedException {
		SpringApplication application = new SpringApplication(OfficialSample03Application.class);
		application.setAdditionalProfiles("official-sample-03");

		ConfigurableApplicationContext context = application.run(args);
		LATCH.await();
		Thread.sleep(5_000);
		context.close();
	}

	@Bean
	public ApplicationRunner runner() {
		String msg = String.format("vergilyn send to[topic2]: %s", LocalTime.now());

		kafkaTemplate.executeInTransaction(kafkaTemplate -> {
			StringUtils.commaDelimitedListToSet(msg).stream()
					.map(s -> new Foo1(s))
					.forEach(foo -> kafkaTemplate.send("topic2", foo));
			return null;
		});

		return args -> {
			System.out.println("Hit Enter to terminate...");
			System.in.read();
		};
	}


	@Bean
	public RecordMessageConverter converter() {
		return new StringJsonMessageConverter();
	}

	@Bean
	public BatchMessagingMessageConverter batchConverter() {
		return new BatchMessagingMessageConverter(converter());
	}

	@KafkaListener(id = "fooGroup2", topics = "topic2")
	public void listen1(List<Foo2> foos) throws IOException {
		log.info("received listen1: {}", foos);
		foos.forEach(f -> kafkaTemplate.send("topic3", f.getFoo().toUpperCase()));
		log.info("Messages sent, hit Enter to commit tx");
		System.in.read();
	}

	@KafkaListener(id = "fooGroup3", topics = "topic3")
	public void listen2(List<String> in) {
		log.info("received listen2: {}", in);
		LATCH.countDown();
	}

	@Bean
	public NewTopic topic2() {
		return TopicBuilder.name("topic2").partitions(1).replicas(1).build();
	}

	@Bean
	public NewTopic topic3() {
		return TopicBuilder.name("topic3").partitions(1).replicas(1).build();
	}

}
