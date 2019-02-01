# RabbitMQ Example

参考:
- [spring-amqp-reference]
- [rabbitmq-document]
- [spring-boot-2.1.2 RabbitMQ support]

[spring-amqp-reference]: https://docs.spring.io/spring-amqp/reference/htmlsingle/
[rabbitmq-document]: http://www.rabbitmq.com/nack.html
[spring-boot-2.1.2 RabbitMQ support]: https://docs.spring.io/spring-boot/docs/2.1.2.RELEASE/reference/htmlsingle/#boot-features-rabbitmq

blog参考:
- [rabbit基础知识](https://blog.csdn.net/dreamchasering/article/details/77653512)
- [rabbit queue-arguments 含义](https://blog.csdn.net/qq_26656329/article/details/77891793)
- [exchange-type: topic、direct、fanout](https://blog.csdn.net/ww130929/article/details/72842234)



2. RabbitMQ support delay-message: rabbitmq_delayed_message_exchange
https://www.rabbitmq.com/community-plugins.html
https://github.com/rabbitmq/rabbitmq-delayed-message-exchange

3. spring-xml配置的问题
用xml的形式初始queue，可能会报: `received 'x-delayed-message' but current is 'direct'`
但是，非springboot下相同的xml却没有这个问题。
解决:
　　在rabbitMQ安装plugins `rabbitmq-delayed-message-exchange`后问题解决。
　　但是exchange-type会从`direct -> x-delayed-message`（rabbitmq-management中查看）


（windows安装教程）
https://www.cnblogs.com/ericli-ericli/p/5902270.html
添加到window-service
cmd %local%/rabbitmq-service.bat install



（主要看 为AmqpTemplate/RabbitTemplate配置 RetryTemplate）
https://www.jianshu.com/p/c65d15e718a2?utm_campaign

（spring-rabbit xml 配置代码参考）
https://www.cnblogs.com/libra0920/p/6230421.html

（注解模式，感觉xml形式创建queue、exchange、routing并不好管理"命名"）
https://blog.csdn.net/liam1994/article/details/80707219

（Spring 集成 RabbitMQ 与其概念，消息持久化，ACK机制）
https://blog.csdn.net/u012129558/article/details/79530653

（spring rabbitmq为listener配置并发消费者数量）
https://blog.csdn.net/tszxlzc/article/details/51014101
不了解，未测试。

（防止消息丢失）
http://www.cnblogs.com/Leo_wl/p/6581989.html
即启用ack，禁止autoAck。

（实现延时重试队列）
https://www.cnblogs.com/itrena/p/9044097.html
大概理解了下exchange-direct，貌似可以只用一个exchange、多个routing-key来实现。
（有空可以写下测试代码验证下）

（开发中使用RabbitMQ的手动确认机制）
https://www.cnblogs.com/yucl/articles/7699761.html
ack模式下，拒绝的消息会出现在"队首"，而不是"队尾"。可以通过消费此条消息，并重新publish一条相同的消息，来达到"队尾"的目的。
但是，无论如何要判断数据是否是最新的数据，否则可能出现old-data覆盖new-data.

RabbitMQ之mandatory和immediate
https://blog.csdn.net/u013256816/article/details/54914525

（RabbitMq + Spring 实现ACK机制）
https://my.oschina.net/gaoguofan/blog/776057

扩展
用java常量配置spring.xml （未达到想象要，主要想配置bean的id/name等）
https://www.oschina.net/question/254463_2182184?sort=time


实际中遇到的问题 >>>>
1.	java.net.SocketException: socket closed
https://blog.csdn.net/only09080229/article/details/43304543

个人：我也是创建了admin帐号（tags设置为administrator），删除guest。

启动时出现exception，然后查看admin帐号，发现确实没有"管理队列的权限"

2.	x-dead中的count一直未递增
具体不清楚原因，但已解决。
逻辑： 在`main_queue`中成功ack，但数据处理错误时，会把此数据publish给`retry_queue`

basicRevocer(); 始终无法递增count，未了解内部实现逻辑。
然后改成ack & re-publish也不行。主要是因为basicProperties是自己写代码从message.getMessageProperties()复制构造AMQP.BasicProperties。

更正 basicProperties = new DefaultMessagePropertiesConverter().fromMessageProperties(properties, "UTF-8");
后发现count可以正常递增。



备注 >>>>
1. 3种手动ack：
        1. channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            只应答当前任务，任务会从队列中移除

        2. channel.basicNack(deliveryTag, false, true);
            ack返回false，并重新回到队列。  但是，会回到"队首"，会造成阻塞。

        3. 拒绝消息
            channel.basicReject(deliveryTag, false);

        4. channel.basicRecover：
             消息重入队列，requeue=true，发送给新的consumer，false发送给相同的consumer
（2018-09-11：需要看测试，怎么获取重试次数，即是在"队首"还是"队尾"）

2. rabbit实现延迟消息队列逻辑：
会有3个exchange、3个queue、3个routing-key。
但貌似通过direct，可以用1个exchange、3个queue、3个routing-key来实现。
![delay-message-queue.png](./delay-message-queue.png)
