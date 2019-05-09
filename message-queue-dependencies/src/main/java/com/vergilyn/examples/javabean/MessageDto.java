package com.vergilyn.examples.javabean;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.vergilyn.examples.constants.MessageModeEnum;

import lombok.Data;
import lombok.ToString;

/**
 * @author VergiLyn
 * @date 2019-05-05
 */
@Data
@ToString
public class MessageDto implements Serializable {
    private Long id;
    private Integer integer;
    private boolean bool;
    private String str;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date date;
    private RabbitMode rabbitMode;

    private long timestamp;

    public MessageDto(){
        this.timestamp = System.currentTimeMillis();
    }

    public static final class Builder {
        private Long id;
        private Integer integer;
        private boolean bool;
        private String str;
        private Date date;
        private RabbitMode rabbitMode = new RabbitMode();

        private Builder() {
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder integer(Integer integer) {
            this.integer = integer;
            return this;
        }

        public Builder bool(boolean bool) {
            this.bool = bool;
            return this;
        }

        public Builder str(String str) {
            this.str = str;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder rabbitMultiple(boolean multiple) {
            this.rabbitMode.setMultiple(multiple);
            return this;
        }

        public Builder rabbitRequeue(boolean requeue) {
            this.rabbitMode.setRequeue(requeue);
            return this;
        }

        public Builder rabbitMode(MessageModeEnum mode) {
            this.rabbitMode.setMode(mode);
            return this;
        }

        public Builder rabbitConsumerError(boolean consumerError) {
            this.rabbitMode.setConsumerError(consumerError);
            return this;
        }

        public MessageDto build() {
            MessageDto messageDto = new MessageDto();
            messageDto.setId(id);
            messageDto.setInteger(integer);
            messageDto.setBool(bool);
            messageDto.setStr(str);
            messageDto.setDate(date);
            messageDto.setRabbitMode(rabbitMode);

            return messageDto;
        }
    }
}
