# message-queue-examples

[Kafka、RabbitMQ、RocketMQ 消息发送性能对比](http://blog.sina.com.cn/s/blog_64e320320102xfes.html)  

> kafka, 17.3w/s  
>   
> rocket, 11.6w/s  
>   
> RabbitMQ, 5.95w/s 为了保证消息的可靠性在吞吐量上做了取舍。RabbitMQ在消息持久化场景下的性能测，吞吐量在2.6w/s左右。

## MQ中的问题

1. [消息队列的优缺点，区别]

2. [如何保证消息的顺序性]
3. [消息丢失怎么办？]
4. [如何保证消息不被重复消费]
5. [如何解决消息队列的延时以及过期失效问题？消息队列满了以后该怎么处理？有几百万消息持续积压几小时呢？]

6. [消息中间件的高可用]
7. [如果让你写一个消息队列，该如何进行架构设计？]


**备注: 以下场景，不同的MQ解决方案可能不一样！**
### 消息中间件的高可用
[消息中间件的高可用]
[RabbitMQ 分布式设置和高可用性讨论](http://www.cnblogs.com/zhengchunyuan/p/9253722.html)

> RabbitMQ 有三种模式：单机模式、普通集群模式、镜像集群模式。

rabbitMQ只是集群（普通集群、镜像集群），并不是分布式。当queue达到瓶颈时，无解！

### 消息丢失场景及解决方案
防止消息丢失，可启用ack模式（禁用auto-ack）。
消息丢失可以简单的分为两种：客户端丢失和服务端丢失。

服务端丢失：
  1. mq服务器重启/宕机
  2. rabbitMQ可以设置TTL，当消费过慢导致TTL到期则会丢弃此消息。
  
因为消息是保存在内存中，所以需要引入持久化机制。
针对情况2，不设置TTL就可以。（可以针对queue或具体某条消息设置ttl，取最小值）

客户端丢失：
  1. 客户端重启或宕机时，会丢失 执行中未完成 和 已接收未执行 的消息。
改为ack模式（禁用auto-ack）。

rabbit: [RabbitMQ防止消息丢失](http://www.cnblogs.com/Leo_wl/p/6581989.html)
rocket: 
kafka: 

### ack机制引起的服务端内存泄漏、消费者重复消费、消息队列阻塞
ack模式下服务端只有在收到消费者确认后，才会从内存中删除消息，如果消费者忘了确认（或其它异常情况，导致服务端未接收到确认信息）。
那么，此消息一直处于服务端的消息队列的队首位置，所以会导致`消息队列阻塞`，并且会一直发送此消息给消费者消费（`消费者重复消费`），直到此条消息被移除。
因为消息队列阻塞，那么服务端会堆积越来越多的消息，导致`服务端内存泄漏`。

### 消息重复发送，如何避免重复消费，及如何防止old-data覆盖new-data

### 如何保证消息不被重复消费

### 如何解决消息队列的延时以及过期失效问题？消息队列满了以后该怎么处理？有几百万消息持续积压几小时呢？

### 消息队列堆积的情况，及处理方案
出现堆积的情况:
  1、消息生产的速率远大于消息消费的速率。
    例如消费者宕机或其它原因导致消费速率降低，或者消费者过少达到消费瓶颈，亦或消费者的并发消费设置过低（每个listener的`concurrency`）。
    
  2、消息队列阻塞
    例如某条消息处理异常，消费者ack=false，导致此消息一直处于"队首"，陷入死循环，导致后面的消息无法被消费造成堆积。


消息堆积后，如果解决？ 
（临时扩容消费者，快速消费掉堆积的消息）

1）先修复consumer的问题，确保其恢复消费速度，然后将现有consumer都停掉
2）新建一个topic，partition是原来的10倍，临时建立好原先10倍或者20倍的queue数量
3）然后写一个临时的分发数据的consumer程序，这个程序部署上去消费积压的数据，
消费之后不做耗时的处理，直接均匀轮询写入临时建立好的10倍数量的queue
4）接着临时征用10倍的机器来部署consumer，每一批consumer消费一个临时queue的数据
5）这种做法相当于是临时将queue资源和consumer资源扩大10倍，以正常的10倍速度来消费数据
6）等快速消费完积压数据之后，得恢复原先部署架构，重新用原先的consumer机器来消费消息

  


[如何解决消息队列的延时以及过期失效问题？消息队列满了以后该怎么处理？有几百万消息持续积压几小时呢？]
[使用rabbitmq消息堵塞处理](https://blog.csdn.net/weixin_38379125/article/details/78791076)

### 如何保证消息的顺序性
- [如何保证消息的顺序性]
- [消息队列如何保证顺序性？](https://hacpai.com/article/1542162310805)
- [如何保证消息的顺序性？](https://blog.csdn.net/qq_38990795/article/details/86899886)
(以上blog都差不多是一个意思)

- [消息顺序性为何这么难？](https://yq.aliyun.com/articles/684344)

2019-05-05：并未找到很理想的、万能的方案，具体要结合需求来考虑。

顺序消息需要Producer和Consumer都保证顺序。Producer需要保证消息被路由到正确的分区，消息者需要保证每个分区的数据只有一个线程消息，那么就会有一些缺陷：
  - 发送顺序消息无法利用集群的Failover特性，因为不能更换MessageQueue进行重试
  - 因为发送的路由策略导致的热点问题，可能某一些MessageQueue的数据量特别大
  - 消费的并行读依赖于分区数量 （吞吐量问题）
  - 消费失败时无法跳过

不能更换MessageQueue重试就需要MessageQueue有自己的副本，通过Raft、Paxos之类的算法保证有可用的副本，或者通过其他高可用的存储设备来存储MessageQueue。

热点问题好像没有什么好的解决办法，只能通过拆分MessageQueue和优化路由方法来尽量均衡的将消息分配到不同的MessageQueue。

消费并行度理论上不会有太大问题，因为MessageQueue的数量可以调整。

消费失败的无法跳过是不可避免的，因为跳过可能导致后续的数据处理都是错误的。不过可以提供一些策略，由用户根据错误类型来决定是否跳过，并且提供重试队列之类的功能，在跳过之后用户可以在“其他”地方重新消费到这条消息。



1）rabbitmq保证数据的顺序性
如果存在多个消费者，那么就让每个消费者对应一个queue，然后把要发送的数据全都放到一个queue，
这样就能保证所有的数据只到达一个消费者从而保证每个数据到达数据库都是顺序的。

将原本错乱的 queue -> consumer 的 OneToMany 改成 OneToOne。
拆分多个queue，每个queue一个consumer，就是多一些queue而已，确实是麻烦点；
或者就一个queue但是对应一个consumer，然后这个consumer内部用内存队列做排队，然后分发给底层不同的worker来处理。



与其过度考虑如何保证有序消费（强一致性），不如换一种解决思路：最终一致性。
比如blog示例中的：增加 - 修改 - 删除。 如果消费顺序是：修改 - 删除 - 增加，或 删除 - 修改 - 增加。

（如果是 1个queue 多个consumer，那么需要考虑 并发锁。但是 吞吐量还是高于 1个queue 1个consumer）

可以这么做：
  消息中包含字段timestamp来记录操作时间，新增包含完整的字段信息，修改可能包含完整的字段信息，删除一般只需要pk。
  如果最先被消费的是`删除`，如果db是逻辑删除，那么新增相应的一条信息（必填字段用默认值填充）。如果是物理删除，需要记录此数据最终被删除，那么可以直接舍弃`修改/新增`操作。
  如果最先被消费的是`修改`，但是不包含完整的信息，其余必填字段也是用默认值填充，然后保存到数据库。
  
  然后之后的操作对比timestamp，是否需要执行，或直接丢弃，或为了最终一致性需要如何处理该消息。

具体还是需要结合业务设计，比如以上方案也会引出别的问题，比如如果我要严格的mysql的binlog怎么办，或者我要有序执行的操作log怎么办。
以及`最终一致性`要到什么程度，是只需要知道数据最后是`删除`状态就可以，还是同时需要保证其它字段的信息是完整且正确的？
（`最终一致性`比较容易的一种方案是，修改/删除时都包含完整的字段信息。但如果这个信息过大，不便于每次传递完整的信息，又该如何解决？）





[消息队列的优缺点，区别]: https://blog.csdn.net/Iperishing/article/details/86674084

[如何保证消息的顺序性]: https://blog.csdn.net/Iperishing/article/details/86674561
[消息丢失怎么办？]: https://blog.csdn.net/Iperishing/article/details/86674488
[如何保证消息不被重复消费]: https://blog.csdn.net/Iperishing/article/details/86674649
[如何解决消息队列的延时以及过期失效问题？消息队列满了以后该怎么处理？有几百万消息持续积压几小时呢？]: https://blog.csdn.net/Iperishing/article/details/86676682

[消息中间件的高可用]: https://blog.csdn.net/Iperishing/article/details/86674317
[如果让你写一个消息队列，该如何进行架构设计？]: https://blog.csdn.net/Iperishing/article/details/86676861