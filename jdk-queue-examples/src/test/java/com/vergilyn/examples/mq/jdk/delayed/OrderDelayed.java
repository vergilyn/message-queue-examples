package com.vergilyn.examples.mq.jdk.delayed;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author vergilyn
 * @date 2020-05-25
 *
 * @see io.netty.util.concurrent.ScheduledFutureTask
 */
@Setter
@Getter
@NoArgsConstructor
public class OrderDelayed implements Delayed {

    private String name;
    private long delayedMs;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
//    @JsonSerialize(using = LocalTimeSerializer.class)
//    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime deadline;

    public OrderDelayed(String name, LocalTime now, long delayedMs) {
        this.name = name;
        this.delayedMs = delayedMs;
        this.deadline = now.plus(delayedMs, ChronoUnit.MILLIS);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        Duration duration = Duration.between(LocalTime.now(), deadline);
        long delay = unit.convert(duration.toMillis(), TimeUnit.MILLISECONDS);
        return delay;
    }

    @Override
    public int compareTo(Delayed o) {
        if (this == o) {
            return 0;
        }

        OrderDelayed p = (OrderDelayed) o;

        long diff = Duration.between(p.deadline, this.deadline).toMillis();
        return diff <= 0 ? -1 : 1;
    }
}
