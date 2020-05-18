package com.vergilyn.examples.mq.official.sample01;

import java.time.LocalTime;

import com.vergilyn.examples.mq.common.Foo2;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;


/**
 * <a href="https://github.com/spring-projects/spring-kafka/tree/master/samples/sample-01">Sample shows use of a dead letter topic.</a>
 *
 * @author vergilyn
 * @date 2020-05-18
 *
 */
@SpringBootApplication
@Slf4j
public class OfficialSample01Application {
	private final TaskExecutor exec = new SimpleAsyncTaskExecutor();

	@Autowired
	private KafkaTemplate<Object, Object> kafkaTemplate;

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(OfficialSample01Application.class);

		application.setAdditionalProfiles("official-sample-01");
		application.run(args).close();

	}

	/*
	 * Boot will autowire this into the container factory.
	 */
	@Bean
	public SeekToCurrentErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
		return new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 2));
	}

	@Bean
	public RecordMessageConverter converter() {
		return new StringJsonMessageConverter();
	}

	@KafkaListener(id = "fooGroup", topics = "topic1")
	public void listen(Foo2 foo) {
		log.info("listen received >>>> {}", foo);
		if (foo.getFoo().startsWith("fail")) {
			throw new RuntimeException("failed");
		}
		this.exec.execute(() -> System.out.println("Hit Enter to terminate..."));
	}

	@KafkaListener(id = "dltGroup", topics = "topic1.DLT")
	public void dltListen(String in) {
		log.info("listen received from DLT(Dead-Letter-Topic): " + in);
		this.exec.execute(() -> System.out.println("Hit Enter to terminate..."));
	}

	// 会自动创建topic
	@Bean
	public NewTopic topic() {
		return new NewTopic("topic1", 1, (short) 1);
	}

	@Bean
	public NewTopic dlt() {
		return new NewTopic("topic1.DLT", 1, (short) 1);
	}

	@Bean
	public ApplicationRunner runner() {
		String topic = "topic1";
		String msg = String.format("vergilyn send to[%s]: %s", topic, LocalTime.now());
		kafkaTemplate.send(topic, msg);

		return args -> {
			System.out.println("Hit Enter to terminate...");
			System.in.read();
		};
	}

}
