package com.vergilyn.examples.rabbitmq.listener;

import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.Channel;

import org.apache.commons.lang3.StringUtils;
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
public class ConsumerListener implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties properties = message.getMessageProperties();
        long deliveryTag = properties.getDeliveryTag();
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        String flag = StringUtils.split(body, ",")[0];

        System.out.printf("consumer >>> ConsumerQueue[%s], body: %s", properties.getConsumerQueue(), body);

        if ("ack".equals(flag)){
            channel.basicAck(deliveryTag, false);

        }else if("reject".equals(flag)){
            // 2. 拒绝单条消息：true, 被拒绝的消息重新排队（更靠近"队首"，而不是"队尾"）；false，消息被丢弃discard，或加入死信队列dead-letter
            channel.basicReject(deliveryTag, false);

        }else if("nack".equals(flag)){
            // 3. nack支持批量拒绝
            channel.basicNack(deliveryTag, false, false);

        }else {
            channel.basicAck(deliveryTag, false);
        }

    }
}
