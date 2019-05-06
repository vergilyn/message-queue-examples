package com.vergilyn.examples.rabbitmq.listener;

import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.vergilyn.examples.constants.MessageModeEnum;
import com.vergilyn.examples.javabean.MessageDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

/**
 * @author VergiLyn
 * @date 2018/9/14
 */
/*@org.springframework.amqp.rabbitmq.annotation.RabbitListener(
        admin = "",
        containerFactory = "",
        bindings = @QueueBinding(
                value = @Queue(value = "queue.anno-hello"),
                exchange = @Exchange(value = "exchange.anno-hello"),
                key = "routing.anno-hello",
                arguments = {})
)*/
@Component("consumerListener")
@Slf4j
public class ConsumerListener implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties properties = message.getMessageProperties();
        long deliveryTag = properties.getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        MessageDto messageDto = JSON.parseObject(body, MessageDto.class);

        log.info("consumer-queue >>>> {}, body: {}", properties.getConsumerQueue(), body);

        if (messageDto.getMode() == MessageModeEnum.RABBIT_ACK){
            channel.basicAck(deliveryTag, false);

        }else if(messageDto.getMode() == MessageModeEnum.RABBIT_REJECT){
            // 2. 拒绝单条消息：true, 被拒绝的消息重新排队（更靠近"队首"，而不是"队尾"）；false，消息被丢弃discard，或加入死信队列dead-letter
            channel.basicReject(deliveryTag, false);

        }else if(messageDto.getMode() == MessageModeEnum.RABBIT_NACK){
            // 3. nack支持批量拒绝
            channel.basicNack(deliveryTag, false, false);

        }else {
            channel.basicAck(deliveryTag, false);
        }

    }
}
