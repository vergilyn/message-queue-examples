package com.vergilyn.examples.rabbitmq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author VergiLyn
 * @date 2019-05-07
 */
@Configuration
@ImportResource("classpath:/rabbit-delay-message.xml")
public class DelayConfiguration {
}

