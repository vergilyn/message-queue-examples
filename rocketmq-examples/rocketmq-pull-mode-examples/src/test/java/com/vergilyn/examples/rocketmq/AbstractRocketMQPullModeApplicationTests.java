package com.vergilyn.examples.rocketmq;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RocketMQPullModeApplication.class)
public class AbstractRocketMQPullModeApplicationTests {

	protected void sleep(TimeUnit unit, long timeout){
		try {
			unit.sleep(timeout);
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	protected void preventExit(){
		try {
			// new Semaphore(0).acquire();
			TimeUnit.HOURS.sleep(1);
		} catch (InterruptedException e) {
			// do nothing
		}
	}
}