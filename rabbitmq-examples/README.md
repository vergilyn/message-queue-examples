# RabbitMQ Example

2020-06-17 >>>
1. rabbitmq 在 batch-send 和 batch-receive 不是很好实现，且实现方式不是很理想，并且 spring-rabbit 的实现情况也不是很理想！
2. `spring-AMQP` 或 `spring-rabbit`的 star 才 600 （apache-rocketmq 12k）。

参考:
- [spring-amqp-reference]
- [rabbitmq-document]
- [spring-boot-2.1.2 RabbitMQ support]

[spring-amqp-reference]: https://docs.spring.io/spring-amqp/reference/htmlsingle/
[rabbitmq-document]: http://www.rabbitmq.com/nack.html
[spring-boot-2.1.2 RabbitMQ support]: https://docs.spring.io/spring-boot/docs/2.1.2.RELEASE/reference/htmlsingle/#boot-features-rabbitmq
[RabbitMQ发布订阅实战-实现延时重试队列]: https://www.cnblogs.com/itrena/p/9044097.html

blog参考:
- [RabbitMQ中 exchange、route、queue的关系](https://www.cnblogs.com/linkenpark/p/5393666.html)
- [提升RabbitMQ消费速度的一些实践](https://www.cnblogs.com/bossma/p/practices-on-improving-the-speed-of-rabbitmq-consumption.html)
- [rabbit queue-arguments 含义](https://blog.csdn.net/qq_26656329/article/details/77891793)
- [exchange-type: topic、direct、fanout](https://blog.csdn.net/ww130929/article/details/72842234)
- [Spring 集成 RabbitMQ 与其概念，消息持久化，ACK机制](https://blog.csdn.net/u012129558/article/details/79530653)

## 概念
### exchange-type, binding-key, routing-key
+ [AMQP 0-9-1 Model Explained](https://www.rabbitmq.com/tutorials/amqp-concepts.html)
- [rabbit基础知识](https://blog.csdn.net/dreamchasering/article/details/77653512)

![rabbitmq-hello-world-example-routing](../docs/images/rabbitmq-hello-world-example-routing.png)

```
Queue queue = new Queue("queue-name");
Exchange exchange = new DirectExchange("exchange-name");  // direct、fanout、topic

// 未体现出 binding-key
Binding binding = BindingBuilder
                    .bind(queue)
                    .to(exchange)
                    .with("routing-key")
                    .noargs();
```

在绑定（Binding）Exchange与Queue的同时，一般会指定一个binding-key。在绑定多个Queue到同一个Exchange的时候，这些Binding允许使用相同的binding-key。  
生产者在将消息发送给Exchange的时候，一般会指定一个routing-key，来指定这个消息的路由规则，生产者就可以在发送消息给Exchange时，通过指定routing-key来决定消息流向哪里。

RabbitMQ常用的Exchange-Type有三种：  
**fanout:** 把所有发送到该Exchange的消息投递到所有与它绑定的队列中。  
**direct:** 把消息投递到那些binding key与routing key完全匹配的队列中。  
**topic:** 将消息路由到binding key与routing key模式匹配的队列中。  

## 安装
[windows下 安装 rabbitMQ 及操作常用命令](https://www.cnblogs.com/ericli-ericli/p/5902270.html)：rabbitMQ服务，及可视化管理控制台。
```
# 添加到window-service
cmd %local%/rabbitmq-service.bat install
```

### RabbitMQ support delay-message
rabbitMQ v3.5.8+开始不需要自己实现延迟消息队列。可以通过安装rabbit-plugin `rabbitmq_delayed_message_exchange`实现。
- [rabbitmq community plugins](https://www.rabbitmq.com/community-plugins.html)
- [rabbitmq-delayed-message-exchange](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)

扩展：
根据rabbit特性实现[RabbitMQ发布订阅实战-实现延时重试队列]。
> 本文将会讲解如何使用RabbitMQ实现延时重试和失败消息队列，
> 实现可靠的消息消费，消费失败后，自动延时将消息重新投递，
> 当达到一定的重试次数后，将消息投递到失败消息队列，等待人工介入处理。


## ack机制
ack机制可以解决：（客户端）消息丢失。
默认情况下，rabbitmq使用的是auto-ack，即服务端成功的将消息发送给消费者后，服务端就会移除该消息，而不关心此消息是否被成功消费。

- [RabbitMQ防止消息丢失](http://www.cnblogs.com/Leo_wl/p/6581989.html)
- [开发中使用RabbitMQ的手动确认机制](https://www.cnblogs.com/yucl/articles/7699761.html)

备注：
1. ack机制下，拒绝的消息会出现在"队首"，而不是"队尾"。可以通过消费此条消息，并重新publish一条相同的消息，来达到"队尾"的目的。
但是，无论如何要判断数据是否是最新的数据，否则可能出现old-data覆盖new-data。

2. ack机制引起的服务端内存泄漏、消费者重复消费、消息队列阻塞
ack模式下服务端只有在收到消费者确认后，才会从内存中删除消息，如果消费者忘了确认（或其它异常情况，导致服务端未接收到确认信息）。
那么，此消息一直处于服务端的消息队列的队首位置，所以会导致`消息队列阻塞`，并且会一直发送此消息给消费者消费（`消费者重复消费`），直到此条消息被移除。
因为消息队列阻塞，那么服务端会堆积越来越多的消息，导致`服务端内存泄漏`。

## push/pull模式
rabbitmq获取消息有两种模式，push模式和pull模式。
1、push模式，又称为Subscribe订阅方式，使用监听器等待数据。
2、pull模式，又称为poll API方式(轮询方式)，从rabbitmq服务器中手动读取数据。

push方式：
  - 消息保存在服务端。容易造成消息堆积。
  - 服务端需要维护每次传输状态，遇到问题需要重试
  - 实时性高
  - 服务端需要依据订阅者消费能力做流控(流转机制)

pull方式：
  - 保存在消费端。获取消息方便。（不一定是保存在消费者端。最先是保存在服务端，消费端pull后取决于是否会删除服务端的消息）
  - 传输失败，不需要重试
  - 默认的端短询方式的实时性依赖于pull间隔时间，间隔越大，实时性越低，长轮询方式和push一致
  - 消费端可以根据自身消费能力决定是否pull(流转机制)

官方文档请移步：<http://www.rabbitmq.com/api-guide.html#consuming>
push与pull区别参考自：<https://blog.csdn.net/hyperin/article/details/79515849>

2020-06-18 >>>>   
- [RabbitMQ：消息发送确认 与 消息接收确认（ACK）](https://www.jianshu.com/p/2c5eebfd0e95)

**deliveryTag（唯一标识 ID）**：  
当一个消费者向 RabbitMQ 注册后，会建立起一个 Channel ，RabbitMQ 会用`basic.deliver`方法向消费者**推送**消息，这个方法携带了一个 delivery-tag，
它代表了rabbitmq向该 Channel 投递的这条消息的唯一标识 ID，是一个单调递增的正整数，delivery tag 的范围仅限于 Channel。

**multiple**：  
为了减少网络流量，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息


## TTL(Time-To-Live)
rabbitMQ可以针对queue，或者具体的message设置TTL `x-message-ttl`。

第一种，通过队列属性设置，队列中所有消息都有相同的过期时间。
第二种，对消息进行单独设置，每条消息TTL可以不同。

如果同时使用，则消息的过期时间以两者之间TTL较小的那个数值为准。
消息在队列的生存时间一旦超过设置的TTL值，就称为dead message，消费者将无法再收到该消息。

区别:
对于第一种设置队列TTL属性的方法，一旦消息过期，就会从队列中抹去，
而第二种方法里，即使消息过期，也不会马上从队列中抹去，因为每条消息是否过期时在即将投递到消费者之前判定的，为什么两者得处理方法不一致？

因为第一种方法里，队列中已过期的消息肯定在队列头部，RabbitMQ只要定期从队头开始扫描是否有过期消息即可，
而第二种方法里，每条消息的过期时间不同，如果要删除所有过期消息，势必要扫描整个队列，所以不如等到此消息即将被消费时再判定是否过期，如果过期，再进行删除。

备注：通过TTL + Dead-Letter 可以实现延迟消息重试。

[RabbitMQ之TTL](https://blog.csdn.net/u013256816/article/details/54916011)

## vhost、exchange、queue
[RabbitMQ的Vhost，Exchange，Queue原理分析](https://www.cnblogs.com/zhengchunyuan/p/9253725.html)

## 实际中遇到的问题
1. java.net.SocketException: socket closed
解决：https://blog.csdn.net/only09080229/article/details/43304543

个人也是创建了admin帐号（tags设置为administrator），删除guest。
启动时出现exception，然后查看admin帐号，发现没有"管理队列的权限"。

2. `header: x-dead`中的count一直未递增

在`main_queue`中成功ack，但数据处理错误时，会把此数据通过`channel.basicRecover()` publish给`retry_queue`; 
但是`header: x-dead`中的`count`未递增，改成ack/re-publish也不行。

主要是因为basicProperties是自己写代码从message.getMessageProperties()复制构造AMQP.BasicProperties。

更正`basicProperties = new DefaultMessagePropertiesConverter().fromMessageProperties(properties, "UTF-8")` 后count可以正常递增。

- [RabbitMQ发布订阅实战-实现延时重试队列]
- [Dead Letter Exchanges](http://next.rabbitmq.com/dlx.html)

3. spring-xml配置的问题
用xml的形式初始queue，可能会报: `received 'x-delayed-message' but current is 'direct'`。
但是，非springboot下相同的xml却没有这个问题（可能是我在demo中依赖了不同的rabbitmq-client导致的）。
解决:
　　在rabbitMQ安装plugins `rabbitmq-delayed-message-exchange`后问题解决。
　　但是exchange-type会从`direct -> x-delayed-message`（rabbitmq-management中查看）

4. 批量获取或批量消费
貌似 rabbitMQ 和 spring-rabbitmq 都没有实现的很理想！

## 备注
1. 4种手动ack：
`channel.basicAck(message.getMessageProperties().getDeliveryTag(), false)`: 只应答当前任务，任务会从队列中移除。
`channel.basicNack(deliveryTag, false, true)`: ack返回false，并重新回到队列。但是，会回到"队首"，会造成阻塞。
`channel.basicReject(deliveryTag, false)`: 拒绝处理消息。
`channel.basicRecover`: 消息重入队列，requeue=true，发送给新的consumer，false发送给相同的consumer。

一般的ack返回false或拒绝处理某条消息，都会是返回到消息队列的"队首"位置，而不是"队尾"。

2. rabbit实现延迟消息队列逻辑
blog: [RabbitMQ发布订阅实战-实现延时重试队列]
会有3个exchange、3个queue、3个routing-key。
但貌似通过direct，可以用1个exchange、3个queue、3个routing-key来实现。

设计思路 >>>>
![delay-message-queue.png](../docs/images/delay-message-queue.png)

3. 配置项含义
[MQ中间件-rabbitmq-connection以及channel的思考（连接池）](https://blog.csdn.net/weixin_34281477/article/details/88434772)

## 参考

一般功能代码参考 >>>>
- [Spring集成RabbitMQ-连接和消息模板](https://www.jianshu.com/p/c65d15e718a2)：主要看 为AmqpTemplate/RabbitTemplate配置 RetryTemplate
- [消息队列 RabbitMQ 与 Spring 整合使用](https://www.cnblogs.com/libra0920/p/6230421.html)：spring-rabbit xml 配置代码参考
- [Spring整合RabbitMQ之注解实现](https://blog.csdn.net/liam1994/article/details/80707219)：注解模式，感觉xml形式创建queue、exchange、routing并不好管理"命名"

其它特性 >>>>
- [spring rabbitmq为listener配置并发消费者数量](https://blog.csdn.net/tszxlzc/article/details/51014101)
- [RabbitMQ之mandatory和immediate](https://blog.csdn.net/u013256816/article/details/54914525)

纯代码参考 >>>>
- [RabbitMq + Spring 实现ACK机制](https://my.oschina.net/gaoguofan/blog/776057)