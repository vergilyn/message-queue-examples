package com.vergilyn.examples.rabbitmq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author VergiLyn
 * @date 2018/9/17
 */
@Configuration
@ImportResource("classpath:/rabbit-batch-get-ack.xml")
public class BatchGetAckConfiguration {

}
