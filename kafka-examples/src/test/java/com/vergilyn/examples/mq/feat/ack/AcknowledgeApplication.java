package com.vergilyn.examples.mq.feat.ack;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.testng.collections.Maps;

/**
 * <a href="https://docs.spring.io/spring-kafka/docs/2.5.0.RELEASE/reference/html/#committing-offsets">AckMode</a>
 *
 * <ul>
 *   <li>RECORD: Commit the offset when the listener returns after processing the record.</li>
 *   <li>BATCH: Commit the offset when all the records returned by the `poll()` have been processed.</li>
 *   <li>TIME: Commit the offset when all the records returned by the `poll()` have been processed,
 *          as long as the `ackTime` since the last commit has been exceeded.</li>
 *   <li>COUNT: Commit the offset when all the records returned by the poll() have been processed,
 *          as long as `ackCount` records have been received since the last commit.</li>
 *   <li>COUNT_TIME: Similar to `TIME` and `COUNT`, but the commit is performed if either condition is `true`.</li>
 *   <li>MANUAL: The message listener is responsible to `acknowledge()` the `Acknowledgment`.
 *          After that, the same semantics as `BATCH` are applied.</li>
 *   <li>MANUAL_IMMEDIATE: Commit the offset immediately when the `Acknowledgment.acknowledge()` method is called by the listener.</li>
 * </ul>
 *
 * @author vergilyn
 * @date 2020-05-19
 *
 * @see org.springframework.kafka.listener.ContainerProperties.AckMode
 */
@SpringBootApplication
@Slf4j
public class AcknowledgeApplication {
    private static final String TOPIC = "vergilyn-kafka-ack";
    private static final String GROUP = TOPIC + "_group";

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AcknowledgeApplication.class);

        Map<String, Object> properties = Maps.newHashMap();
        properties.put("spring.kafka.consumer.enable-auto-commit", false);
        properties.put("spring.kafka.producer.value-serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        properties.put("spring.kafka.listener.ack-mode", ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        application.setDefaultProperties(properties);

        application.run(args).close();
    }

    @KafkaListener(id = GROUP, topics = TOPIC)
    public void listen(String message, Acknowledgment acknowledgment) {
        log.info("listen received >>>> {}", message);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            // do nothing
        }

        acknowledgment.acknowledge();
        // acknowledgment.nack();

        log.info("ack finish!");
    }

    @Bean
    public ApplicationRunner runner() {
        String msg = String.format("vergilyn send to[%s]: %s", TOPIC, LocalTime.now());
        kafkaTemplate.send(TOPIC, msg);

        return args -> {
            System.out.println("Hit Enter to terminate...");
            System.in.read();
        };
    }

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
