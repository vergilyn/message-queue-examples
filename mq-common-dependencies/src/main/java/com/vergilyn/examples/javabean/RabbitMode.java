package com.vergilyn.examples.javabean;

import java.io.Serializable;

import com.vergilyn.examples.constants.MessageModeEnum;

import lombok.Builder;
import lombok.Data;

/**
 * @author VergiLyn
 * @date 2019-05-07
 */
@Data
@Builder
public class RabbitMode implements Serializable {
    private boolean multiple;
    private boolean requeue;
    private MessageModeEnum mode;
    private boolean consumerError = false;
}
