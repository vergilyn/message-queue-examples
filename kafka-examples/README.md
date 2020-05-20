# kafka-examples

+ <https://kafka.apache.org/>
+ <https://spring.io/projects/spring-kafka/>
+ <https://github.com/spring-projects/spring-kafka>

docs:  
- [spring kafka test](https://docs.spring.io/spring-kafka/docs/2.5.0.RELEASE/reference/html/#testing)
- <https://www.orchome.com/535>: kafka 配置含义

- [【interview】kafka](https://docs.qq.com/doc/DUnFiRVVzeUVMbWt3)

blog:  
- [**kafka Consumer均衡算法，partition的个数和消费组组员个数的关系**](https://blog.csdn.net/qq_20641565/article/details/59746101)
- [为什么kafka中1个partition只能被同组的一个consumer消费?](https://www.zhihu.com/question/328057678)：意义不大，还造成重复消费。
- [Kafka消费组(consumer group)](https://www.cnblogs.com/huxi2b/p/6223228.html)

## core code
- org.apache.kafka.clients.CommonClientConfigs
- org.apache.kafka.clients.consumer.ConsumerConfig

## install
download: <https://kafka.apache.org/downloads>

## run
- [Win10下kafka简单安装及使用](https://blog.csdn.net/github_38482082/article/details/82112641)

1. 启动 kafaka 内置 zookeeper   
kafka 高度依赖 zookeeper，最好还是不要用内置的zk！

```cmd
PS D:\VergiLyn\MQ\kafka_2.13-2.5.0\bin\windows> .\zookeeper-server-start.bat ..\..\config\zookeeper.properties
```

- [kafka 中 zookeeper 具体是做什么的？](https://zhuanlan.zhihu.com/p/95831768)
- [为什么搭建Kafka需要zookeeper?](https://www.oschina.net/question/181141_2157270)

2. 启动 kafaka  
```cmd
PS D:\VergiLyn\MQ\kafka_2.13-2.5.0\bin\windows> .\kafka-server-start.bat ..\..\config\server.properties
```

## kafka UI

- [kafka tool](http://www.kafkatool.com/download.html): C/S
- [yahoo CMAK (kafka manager)](https://github.com/yahoo/CMAK): B/S ?

## spring-kafka-test
+ [Testing Applications](https://docs.spring.io/spring-kafka/docs/2.5.0.RELEASE/reference/html/#testing)
- `org.springframework.kafka.test.utils.KafkaTestUtils`

## FAQ

### 1. CommitFailedException
+ [记一次线上kafka一直rebalance故障](https://www.jianshu.com/p/271f88f06eb3)

```text
ERROR --- [ack-group-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-ack-group-1, groupId=ack-group] 
Offset commit failed on partition vergilyn-kafka-ack-0 at offset 10: The coordinator is not aware of this member.

Caused by: org.apache.kafka.clients.consumer.CommitFailedException: 
	Commit cannot be completed since the group has already rebalanced and assigned the partitions to another member. 
	This means that the time between subsequent calls to poll() was longer than the configured max.poll.interval.ms, 
	which typically implies that the poll loop is spending too much time message processing. 
	You can address this either by increasing `max.poll.interval.ms` 
    or by reducing the maximum size of batches returned in poll() with `max.poll.records`.
```

- [`max.poll.records`](http://kafka.apache.org/documentation/#max.poll.records): 500
- [`max.poll.interval.ms`](http://kafka.apache.org/documentation/#max.poll.interval.ms): 300000(300s)
- [`heartbeat.interval.ms`](http://kafka.apache.org/documentation/#heartbeat.interval.ms): 3000(3s)
- [`session.timeout.ms`](http://kafka.apache.org/documentation/#session.timeout.ms): 10000(10s)
- [`group.min.session.timeout.ms`](http://kafka.apache.org/documentation/#group.min.session.timeout.ms): 6000(6s)
- [`group.max.session.timeout.ms`](http://kafka.apache.org/documentation/#group.max.session.timeout.ms): 1800000(1800s)

个人遇到此问题可能是debug造成(heartbeat丢失)，改成`Thead.sleep`后是正常的。

### **2. kafka delay consume message**
貌似默认不支持！

- [如何在MQ中实现支持任意延迟的消息？](https://www.cnblogs.com/hzmark/p/mq-delay-msg.html)

**备注：**  
1. 延时分为，1) 生产延时 2) 消费延时

### 3. repeat consume

1. [kafka消费延迟或者重复消费原因](https://www.jianshu.com/p/98697293d827)  
**案例：**  
假设consumer消费一条数据平均需要200ms的时间（1s消费50条），
在某个时刻，producer会在短时间内产生大量的数据丢进kafka的broker里面（假设平均1s中内丢入了5w条需要消费的消息，这个情况会持续几分钟）。

对于这种情况，kafka的consumer的行为会是：  
- kafka的consumer会从broker里面取出一批数据，给消费线程进行消费。
- 由于取出的一批消息数量太大，consumer在session.timeout.ms时间之内没有消费完成
- consumer coordinator 会由于没有接受到心跳而挂掉，并且出现一些日志日志的意思大概是coordinator挂掉了，然后自动提交offset失败，然后重新分配partition给客户端
- 由于自动提交offset失败，导致重新分配了partition的客户端又重新消费之前的一批数据
- 接着consumer重新消费，又出现了消费超时，无限循环下去。

### 4. 消息及时性
+ [kafka design pull](http://kafka.apache.org/documentation/#design_pull)
- [Kafka vs RocketMQ ——消息及时性对比](https://yq.aliyun.com/articles/62836)

kafka 采用的是`拉模式`（poll/pull），所以存在一定的延时。  
（RocketMQ 及时性优于 kafka）

**pull vs push**  
推(push)模式是一种基于客户器/服务器机制，**由服务器主动将信息送到客户器的技术。**  
在push模式应用中，服务器把信息送给客户器之前，并没有明显的客户请求。  
push事务由服务器发起。push模式可以让信息主动、快速地寻找用户/客户器，信息的主动性和实时性比较好。  
**但精确性较差，可能推送的信息并不一定满足客户的需求。**    
推送模式不能保证能把信息送到客户器，因为推模式采用了广播机制，如果客户器正好联网并且和服务器在同一个频道上，推送模式才是有效的。  
push模式无法跟踪状态，采用了开环控制模式，没有用户反馈信息。在实际应用中，由客户器向服务器发送一个申请，并把自己的地址（如IP、port）告知服务器，
然后服务器就源源不断地把信息推送到指定地址。在多媒体信息广播中也采用了推模式。另外，如手机、qq广播。

拉（pull）模式与推模式相反，是**由客户器主动发起的事务**。  
服务器把自己所拥有的信息放在指定地址（如IP、port），客户器向指定地址发送请求，把自己需要的资源“拉”回来。  
不仅可以准确获取自己需要的资源，还可以及时把客户端的状态反馈给服务器。

### 5. 数据删除策略（基于时间、基于日志大小、基于日志起始偏移量）
+ [Kafka学习笔记之Kafka日志删出策略](https://cloud.tencent.com/developer/article/1455485)
+ [kafka 文件删除策略](https://blog.csdn.net/qq_34897849/article/details/102690945)