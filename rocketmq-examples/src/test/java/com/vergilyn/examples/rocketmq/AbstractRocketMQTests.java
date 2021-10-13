package com.vergilyn.examples.rocketmq;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.rocketmq.constants.RocketDefinedGenerator;

import org.apache.rocketmq.common.message.Message;

/**
 * @author vergilyn
 * @date 2020-06-15
 */
public abstract class AbstractRocketMQTests {
    protected static final String NAMESRV_ADDR = "localhost:9876";


    protected static Message createMessage(RocketDefinedGenerator defined, LocalDateTime now){
        Message message = new Message(defined.topic(),
                                      defined.tag(),
                                      generatorKey(now),
                                      JSON.toJSONBytes(String.format("{\"now\": \"%s\"}", now)));

        return message;
    }

    protected static String generatorKey(LocalDateTime time){
        return "key-" + time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

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
