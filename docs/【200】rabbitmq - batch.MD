# 【200】rabbitmq - batch.MD

- `BatchingRabbitTemplate#send()`
- `SimpleBatchingStrategy#addToBatch()`







## batch send

1. **provider-side 丢失消息**
> Batched data is held in memory. Unsent messages can be lost in the event of a system failure.  
> 因为是基于 provider-side 的 memory 来将n条信息组装成1条message发送，所以存在消息丢失的风险。

相对于后面的问题，感觉provider-side丢失消息产生的问题比较严重！
所以，现在**个人还是推荐** provider 不使用 batch-send-message(即 `BatchingRabbitTemplate`)，还是使用逐条发送到rabbit-mq(即 `RabbitTemplate`)。  
但是，consumer-side 还是采用batch-receive进行消费。  
（因为是ACK，所以起码可以尽量避免消息丢失，但可能产生重复消费）

2. `BatchingRabbitTemplate` 还处于 Experimental(试验性的)，并且`send()` 方法是 `synchronized` !  

3. 因为`BatchingStrategy`是`BatchingRabbitTemplate`成员属性，且`SimpleBatchingStrategy.addToBatch()`中校验了 exchange 和 routing-key 是否一致，
所以**同一个`BatchingRabbitTemplate`实例**只能send-message到相同的 exchange 和 routing-key。  

并且，**exchange 和 routing-key 是在第一次调用 `BatchingRabbitTemplate#send()` 才赋值，而不是在实例化`SimpleBatchingStrategy`时。**  
（并不存在 `simpleBatchingStrategy.setExchange()` 方法）

4. `SimpleBatchingStrategy`中的 timeout 指的是：  
> "A time after which a partial batch is sent when there is no new activity adding messages to the batch."  

并不是类似滑动时间窗的timeout，具体看源码`BatchingRabbitTemplate#send()`。  
例如`timeout = 5s`，单条消息发送的时间分别是：12:00:00, 12:00:04, 12:00:07, 12:00:13  

（个人）期望的消息发送组合是：  
- 12:00:00, 12:00:04
- 12:00:07
- 12:00:13
 
但现有的默认实现情况是：
- 12:00:00, 12:00:04, 12:00:07
- 12:00:13 


## batch receive

```
SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

/**
 * when {@link #setConsumerBatchEnabled(boolean)} is true, `batch-size` determines how
 * many records to include in the batch as long as sufficient messages arrive within
 * {@link #setReceiveTimeout(long)}.
 *
 * @see SimpleMessageListenerContainer#setBatchSize(int)
 */
factory.setConsumerBatchEnabled(true);
factory.setBatchSize(10);
factory.setReceiveTimeout(3_000L);
```

假设provider随机1s ~ 4s push消息
```
AtomicInteger total = new AtomicInteger(33);
AtomicInteger index = new AtomicInteger(0);

// send messages continuously
for(int i = 0, len = 10 + 3; i < len; i++){
    send();
    index.incrementAndGet();
}

// +1s for ensure receive-timeout
sleep(TimeUnit.MILLISECONDS, 3000L + 1000L);

// send messages at random intervals (1s ~ 4s). when grand than 3s(receive-timeout) execute consume-handler
HashedWheelTimer timer = new HashedWheelTimer();
timer.newTimeout(new TimerTask() {
    @Override
    public void run(Timeout timeout) throws Exception {
        send();
        if (index.incrementAndGet() < total.get()){
            timer.newTimeout(this, RandomUtils.nextInt(1, 4), TimeUnit.SECONDS);
        }else {
            System.out.printf("[%s]provider send message completed. total: %d \r\n", LocalTime.now().toString(), total.get());
        }
    }
}, 0, TimeUnit.SECONDS);
```

最终执行的可能情况：
```text
[13:40:06.128][1] begin >>>> size: 10, total: 10 
13:40:06.114
13:40:06.119
13:40:06.119
13:40:06.119
13:40:06.119
13:40:06.120
13:40:06.120
13:40:06.120
13:40:06.120
13:40:06.120
[13:40:09.130][2] begin >>>> size: 3, total: 13 
13:40:06.120
13:40:06.120
13:40:06.121
[13:40:14.378][3] begin >>>> size: 2, total: 15 
13:40:10.275
13:40:11.375
[13:40:23.977][4] begin >>>> size: 6, total: 21 
13:40:14.476
13:40:15.575
13:40:16.675
13:40:18.775
13:40:19.875
13:40:20.975
[13:40:29.177][5] begin >>>> size: 2, total: 23 
13:40:24.076
13:40:26.175
[13:40:34.383][6] begin >>>> size: 2, total: 25 
13:40:29.276
13:40:31.375
[13:40:46.176]provider sends message completed. total: 33 
[13:40:49.179][7] begin >>>> size: 8, total: 33 
13:40:34.476
13:40:35.575
13:40:37.676
13:40:39.777
13:40:41.876
13:40:42.976
13:40:44.076
13:40:46.176
```

期望：当消息条数达到`batch-size`，或者经过`timeout`时，将当前的消息全部获取。

实际的`receive-timeout`，并不是期望的`timeout`。而是rabbitmq获取2条消息之间的间隔。  
例如，`receive-timeout = 3s, batch-size = 10`，但消息发送间隔为 2s，意味着每隔 10 * 2s 会获取10条消息触发consumer-listener。