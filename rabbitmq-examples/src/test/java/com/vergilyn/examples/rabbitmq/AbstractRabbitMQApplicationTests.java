package com.vergilyn.examples.rabbitmq;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * @author vergilyn
 * @date 2020-06-15
 */
@SpringBootTest(classes = RabbitMQApplication.class)
public abstract class AbstractRabbitMQApplicationTests {
    @Autowired
    protected ApplicationContext applicationContext;

    protected void sleep(TimeUnit unit, long timeout){
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    protected void preventExit(){
        try {
            new Semaphore(0).acquire();
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
