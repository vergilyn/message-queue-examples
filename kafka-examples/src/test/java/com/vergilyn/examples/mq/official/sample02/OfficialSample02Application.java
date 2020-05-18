package com.vergilyn.examples.mq.official.sample02;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import com.vergilyn.examples.mq.common.Bar1;
import com.vergilyn.examples.mq.common.Bar2;
import com.vergilyn.examples.mq.common.Foo1;
import com.vergilyn.examples.mq.common.Foo2;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.converter.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;


/**
 * <a href="https://github.com/spring-projects/spring-kafka/tree/master/samples/sample-02">Sample shows use of a multi-method listener.</a>
 *
 * @author vergilyn
 * @date 2020-05-18
 *
 */
@SpringBootApplication
@Slf4j
public class OfficialSample02Application {
	@Autowired
	private KafkaTemplate<Object, Object> kafkaTemplate;

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(OfficialSample02Application.class);

		application.setAdditionalProfiles("official-sample-02");
		application.run(args).close();
	}

	@Bean
	public ApplicationRunner runner() {

		Foo1 foo1 = new Foo1(String.format("vergilyn send to[foos]: %s", LocalTime.now()));
		kafkaTemplate.send("foos", foo1);

		Bar1 bar1 = new Bar1(String.format("vergilyn send to[bars]: %s", LocalTime.now()));
		kafkaTemplate.send("bars", bar1);

		kafkaTemplate.send("bars", String.format("vergilyn send to[unknown]: %s", LocalTime.now()));

		return args -> {
			System.out.println("Hit Enter to terminate...");
			System.in.read();
		};
	}

	@Bean
	public SeekToCurrentErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
		return new SeekToCurrentErrorHandler(
				new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 2));
	}

	@Bean
	public RecordMessageConverter converter() {
		StringJsonMessageConverter converter = new StringJsonMessageConverter();
		DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
		typeMapper.setTypePrecedence(TypePrecedence.TYPE_ID);
		typeMapper.addTrustedPackages("com.vergilyn.examples.mq.official.sample02");
		Map<String, Class<?>> mappings = new HashMap<>();
		mappings.put("foo", Foo2.class);
		mappings.put("bar", Bar2.class);
		typeMapper.setIdClassMapping(mappings);
		converter.setTypeMapper(typeMapper);
		return converter;
	}

	@Bean
	public NewTopic foos() {
		return new NewTopic("foos", 1, (short) 1);
	}

	@Bean
	public NewTopic bars() {
		return new NewTopic("bars", 1, (short) 1);
	}

}
