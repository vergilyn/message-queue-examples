# kafka-examples

+ <https://kafka.apache.org/>
+ <https://spring.io/projects/spring-kafka/>
+ <https://github.com/spring-projects/spring-kafka>

docs:  
- [spring kafka test](https://docs.spring.io/spring-kafka/docs/2.5.0.RELEASE/reference/html/#testing)
- <https://www.orchome.com/535>: kafka 配置含义

- [【interview】kafka](https://docs.qq.com/doc/DUnFiRVVzeUVMbWt3)

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

### CommitFailedException
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