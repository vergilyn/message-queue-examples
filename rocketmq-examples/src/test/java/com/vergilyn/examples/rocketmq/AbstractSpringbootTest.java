package com.vergilyn.examples.rocketmq;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author vergilyn
 * @date 2020-06-15
 */
@SpringBootTest(classes = RocketMQApplication.class)
public abstract class AbstractSpringbootTest {

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
