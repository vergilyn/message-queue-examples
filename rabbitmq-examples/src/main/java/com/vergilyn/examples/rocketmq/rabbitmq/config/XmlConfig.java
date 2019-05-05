package com.vergilyn.examples.rocketmq.rabbitmq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * @author VergiLyn
 * @date 2018/9/17
 */
@Configuration
@ImportResource("classpath:/config/spring-rabbit-hello.xml")
@Profile("xml")
public class XmlConfig {
}
