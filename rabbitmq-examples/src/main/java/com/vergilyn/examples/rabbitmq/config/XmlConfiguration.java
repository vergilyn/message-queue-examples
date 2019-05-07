package com.vergilyn.examples.rabbitmq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author VergiLyn
 * @date 2018/9/17
 */
@Configuration
@ImportResource("classpath:/spring-rabbit-hello.xml")
public class XmlConfiguration {
}
