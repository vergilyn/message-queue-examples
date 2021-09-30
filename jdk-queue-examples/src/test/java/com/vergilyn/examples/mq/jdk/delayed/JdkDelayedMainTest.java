package com.vergilyn.examples.mq.jdk.delayed;

import java.time.LocalTime;
import java.util.concurrent.DelayQueue;

import com.alibaba.fastjson.JSON;

/**
 * DelayQueue是一个BlockingQueue（无界阻塞）队列，它本质就是封装了一个PriorityQueue（优先队列），PriorityQueue内部使用完全二叉堆（不知道的自行了解哈）来实现队列元素排序，
 *
 * <p>我们在向DelayQueue队列中添加元素时，会给元素一个Delay（延迟时间）作为排序条件，队列中最小的元素会优先放在队首。队列中的元素只有到了Delay时间才允许从队列中取出。
 *
 * <p>队列中可以放基本数据类型或自定义实体类，在存放基本数据类型时，优先队列中元素默认升序排列，自定义实体类就需要我们根据类属性值比较计算了。
 * @author vergilyn
 * @date 2020-05-25
 *
 * @see io.netty.util.concurrent.ScheduledFutureTask
 */
@SuppressWarnings("JavadocReference")
public class JdkDelayedMainTest {

    public static void main(String[] args) throws InterruptedException {
        LocalTime now = LocalTime.now();

        OrderDelayed order1 = new OrderDelayed("Order1", now, 5_000);
        OrderDelayed order2 = new OrderDelayed("Order2", now, 10_000);
        OrderDelayed order3 = new OrderDelayed("Order3", now, 15_000);

        DelayQueue<OrderDelayed> delayQueue = new DelayQueue<>();
        delayQueue.put(order1);
        delayQueue.put(order2);
        delayQueue.put(order3);

        System.out.printf("jdk-delayed start >>>> %s \r\n", LocalTime.now());

        while (delayQueue.size() != 0) {
            /*
             * 取队列头部元素是否过期
             * poll() 为非阻塞获取，没有到期的元素直接返回null；
             * take() 阻塞方式获取，没有到期的元素线程将会等待。
             */
            OrderDelayed task = delayQueue.take();
            System.out.printf("order: %s, exec-time: %s \r\n", JSON.toJSONString(task), LocalTime.now());

            // TimeUnit.SECONDS.sleep(1);
        }
    }
}
