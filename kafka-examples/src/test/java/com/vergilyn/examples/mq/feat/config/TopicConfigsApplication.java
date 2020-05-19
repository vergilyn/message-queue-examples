package com.vergilyn.examples.mq.feat.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

/**
 *
 * @author vergilyn
 * @date 2020-05-19
 *
 * @see <a href="http://kafka.apache.org/documentation/#topicconfigs">topic configs</a>
 * @see TopicConfig
 */
@SpringBootApplication
@Slf4j
public class TopicConfigsApplication {
    private static final String TOPIC = "vergilyn-topic-config-test";

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TopicConfigsApplication.class);
        application.run(args).close();
    }

    @Bean
    public NewTopic topic(){
        TopicBuilder builder = TopicBuilder.name(TOPIC)
                .partitions(1)
                .replicas(1);

        builder.config(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, "20");

        return builder.build();
    }
}
