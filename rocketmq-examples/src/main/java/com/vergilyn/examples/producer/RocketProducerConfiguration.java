package com.vergilyn.examples.producer;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.vergilyn.examples.RocketConstants;
import com.vergilyn.examples.bean.TestBean;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.context.annotation.Configuration;

/**
 * @date 2019/1/31
 */
@Configuration
public class RocketProducerConfiguration {

    private static final List<Message> messages = Lists.newArrayList();
    static {
        Message message;
        for (Long i = 1L; i <= 100L; i++){
            message = new Message(RocketConstants.MESSAGE_TOPIC, RocketConstants.MESSAGE_TAG, i + "", JSON.toJSONBytes(new TestBean(i, "user-" + i)));
            messages.add(message);
        }
    }

    public void producer(){
        DefaultMQProducer producer = new DefaultMQProducer(RocketConstants.GROUP_PRODUCER);
        producer.setNamesrvAddr(RocketConstants.NAMESRV_ADDR);
        producer.setVipChannelEnabled(false);
        try {
            producer.start();
            //  producer.createTopic("t_p_key", MESSAGE_TOPIC, 5);
            producer.send(messages);
        } catch (MQClientException | InterruptedException | RemotingException | MQBrokerException e) {
            e.printStackTrace();
        }
    }
}
