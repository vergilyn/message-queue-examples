package com.vergilyn.examples.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.vergilyn.examples.javabean.MessageDto;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;

public class RabbitMessageUtils {
    public static String body(Message message){
        return body(message, StandardCharsets.UTF_8);
    }

    public static String body(Message message, Charset charset){
        return new String(message.getBody(), charset);
    }

    public static MessageDto parseObject(Message message){
        return parseObject(body(message));
    }

    public static MessageDto parseObject(String messageBody){
        return DefaultObjectMapper.readValue(messageBody, MessageDto.class);
    }


    public static List<MessageDto> parseArray(Message message){
        return parseArray(body(message));
    }

    public static List<MessageDto> parseArray(String messageBody){
        return DefaultObjectMapper.readValueAsList(messageBody, MessageDto.class);
    }

    public static int retryCount(Message message){
        return retryCount(message.getMessageProperties());
    }

    /**
     * <a href="http://next.rabbitmq.com/dlx.html">http://next.rabbitmq.com/dlx.html</a>
     */
    public static int retryCount(MessageProperties properties){
        Map<String, Object> headers = properties.getHeaders();
        if (headers != null && headers.containsKey("x-death")){
            List<Map<String, Object>> deathList = (List<Map<String, Object>>) headers.get("x-death");
            if (CollectionUtils.isNotEmpty(deathList)){
                Map<String, Object> deathEntry = deathList.get(0);
                return NumberUtils.toInt(deathEntry.get("count") + "", 0);
            }
        }

        return 0;
    }

    public static AMQP.BasicProperties fromMessageProperties(Message message){
        return fromMessageProperties(message.getMessageProperties());
    }

    /**
     * @see DefaultMessagePropertiesConverter
     */
    public static AMQP.BasicProperties fromMessageProperties(MessageProperties properties){
       return new DefaultMessagePropertiesConverter().fromMessageProperties(properties, "UTF-8");
    }

}
