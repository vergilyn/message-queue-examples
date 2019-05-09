package com.vergilyn.examples.rabbitmq.listener;

import com.rabbitmq.client.Channel;
import com.vergilyn.examples.javabean.MessageDto;
import com.vergilyn.examples.javabean.RabbitMode;
import com.vergilyn.examples.rabbitmq.constants.RabbitMQConstants;
import com.vergilyn.examples.util.RabbitMessageUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

/**
 * @author VergiLyn
 * @date 2019-05-09
 */
@Slf4j
public class DelayMessageListener implements ChannelAwareMessageListener {
    private static final int MAX_RETRY_TIMES = 5;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties properties = message.getMessageProperties();
        long deliveryTag = properties.getDeliveryTag();
        String body = RabbitMessageUtils.body(message);
        MessageDto messageDto = RabbitMessageUtils.parseObject(body);
        RabbitMode rabbitMode = messageDto.getRabbitMode();

        log.info("consumer-queue >>>> {}, body: {}", properties.getConsumerQueue(), body);

        int retryCount = RabbitMessageUtils.retryCount(properties);

        // 消费失败，且允许重试。 先从推送到retry-queue，
        if (rabbitMode.isConsumerError() && retryCount < MAX_RETRY_TIMES){

            // 先推送到retry-queue
            channel.basicPublish(RabbitMQConstants.DELAY_RETRY.exchange,
                    RabbitMQConstants.DELAY_RETRY.routing,
                    RabbitMessageUtils.fromMessageProperties(message),
                    message.getBody());

            // 再从queue移除，让消费者继续消费之后的消息，避免长时间阻塞
            // 顺序不能反，以防先queue-ack后，未成功推送到retry-queue，造成消息丢失。同理，需要考虑重复消费
            channel.basicAck(deliveryTag, false);

        }else{
            channel.basicAck(deliveryTag, false);  // 正常消费
            // channel.basicReject(deliveryTag, false);  // 转到failed-queue(dead-letter)
        }


    }
}
