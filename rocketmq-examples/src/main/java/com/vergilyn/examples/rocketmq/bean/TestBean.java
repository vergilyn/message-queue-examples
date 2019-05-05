package com.vergilyn.examples.rocketmq.bean;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date 2019/1/31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestBean {
    private Long id;
    private String username;
    private boolean isTrue;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date date;

    public TestBean(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
