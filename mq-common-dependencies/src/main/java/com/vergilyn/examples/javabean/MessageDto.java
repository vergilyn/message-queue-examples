package com.vergilyn.examples.javabean;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * @author VergiLyn
 * @date 2019-05-05
 */
@Data
@Builder
public class MessageDto implements Serializable {
    private Long id;
    private Integer integer;
    private boolean bool;
    private String str;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime date;
    private RabbitMode rabbitMode;

    private long timestamp = System.currentTimeMillis();

    // `@Data` 与 `@Builder` 一起使用时，没有无参构造函数的问题
    @Tolerate
    public MessageDto() {
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
