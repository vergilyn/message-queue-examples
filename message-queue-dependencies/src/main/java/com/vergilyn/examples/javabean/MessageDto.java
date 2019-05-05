package com.vergilyn.examples.javabean;

import java.util.Date;
import java.util.UUID;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author VergiLyn
 * @date 2019-05-05
 */
@Data
public class MessageDto {
    private Long id;
    private Integer integer;
    private boolean bool;
    private String str;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date date;

    private long timestamp;

    private MessageDto(){
        this.timestamp = System.currentTimeMillis();
    }

    public static MessageDto newInstance(Long id, String str){
        MessageDto rs = new MessageDto();
        rs.setId(id);
        rs.setInteger(RandomUtils.nextInt(0, 10));
        rs.setBool(RandomUtils.nextBoolean());
        rs.setStr(str != null ? str : UUID.randomUUID().toString());
        rs.setDate(new Date());

        return rs;
    }
}
