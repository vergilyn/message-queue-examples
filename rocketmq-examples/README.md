# rocketmq-examples

**official:**  
+ <https://rocketmq.apache.org/>
+ <https://github.com/apache/rocketmq>
+ <https://github.com/apache/rocketmq-spring>

**article:**  
- [阿里 RocketMQ 安装与简介](https://www.cnblogs.com/xiaodf/p/5075167.html)
- [详解RocketMQ中的consumer](https://www.cnblogs.com/wanghuaijun/p/5881043.html)
- [RocketMQ——顺序消息和重复消息](https://blog.csdn.net/gwd1154978352/article/details/80691916)
- [rocketmq总结(消息的顺序、重复、事务、消费模式](https://www.cnblogs.com/xuwc/p/9034352.html)
- [RocketMQ的消息发送及消费](https://www.cnblogs.com/wuzhenzhao/p/11504941.html)
- [RocketMq 消费消息的两种方式 pull 和 push](https://blog.csdn.net/zhangcongyi420/article/details/90548393)

**code-examples:**    
- [高并发：RocketMQ 削峰实战！](https://mp.weixin.qq.com/s/aQJNazFzOtlbU4m6GhI0Nw)：
`pullBatchSize` 与 `Broker.maxTransferCountOnMessageInMemory`的坑，以及批量消费。  


## windows下安装
参考: [windows下RocketMQ安装部署](https://www.jianshu.com/p/4a275e779afa)

1. 官网下载地址[http://rocketmq.apache.org](http://rocketmq.apache.org)，选择下载`Binary`。
2. 解压zip到任意位置。
3. 配置环境变量`ROCKETMQ_HOME`，文件夹为bin目录的上一级。
4. 启动服务，cmd进入bin目录。（一定要先启动namesrv，再启动broker）
    i. 启动namesrv，`start mqnamesrv`。
    ii. 启动broker，`start mqbroker`。

## 2. 概念

### 2.1 Topic, Tag, Keys
- [RocketMQ中Topic、Tag、GroupName的设计初衷](https://my.oschina.net/javamaster/blog/2051703)
- [RocketMQ概念篇](https://www.jianshu.com/p/10b012f0cd85)
- [RocketMQ中Topic、Tag如何正确使用](https://blog.csdn.net/ye17186/article/details/89640286)

> [RocketMQ知识整理与总结](https://www.cnblogs.com/zhyg/archive/2019/02/28/10451518.html)

- topic：主题名称

- tag：消息TAG，用于消息过滤对消息的整体分类，
比如 topic为物流跟踪轨迹，轨迹包含 揽收 出库 入库 派送 签收，可以分别给这些相同topic不同类型的数据打标签分类解析处理

- keys：Message索引键，多个用空格隔开，RocketMQ可以根据这些key快速检索到消息对消息关键字的提取方便查询，比如一条消息某个关键字是 运单号，之后我们可以使用这个运单号作为关键字进行查询

- waitStoreMsgOK：消息发送时是否等消息存储完成后再返

- delayTimeLevel：消息延迟级别，用于定时消息或消息重

- User property：自定义消息属性

## 配置

### broker的配置
配置文件在`\rocketmq-all-4.4.0-bin-release\conf`下的`broker.conf`。
也可以通过启动时指定参数，例如`start mqbroker.cmd -n 127.0.0.1:9876  autoCreateTopicEnable=true`。

特别: 看到有人说其实启动并未读取`broker.conf`的配置，需要启动指定`start mqbroker -c ./conf/broker.properties -n ...`。
并且`-c`只能识别`*.properties`，所以可能还需要更改扩展名。


参考: [rocketmq配置文件详解](https://www.jianshu.com/p/6a6b89f7365a)、[RocketMq Deployment](https://rocketmq.apache.org/docs/rmq-deployment/)

1. brokerClusterName
所属集群名字。如果有多个master，那么每个master配置的名字应该一样，要不然识别不了对方，不知道是一个集群内的。

2. brokerName
broker名字，不同的配置文件填写的不一样。

3. brokerId
0表示Master，大于0表示slave。

4. namesrvAddr
nameServer 地址，分号分割。
备注：broker启动时会跟nameserver建一个长连接，broker通过长连接才会向nameserver发新建的topic主题，
然后java的客户端才能跟nameserver端发起长连接，向nameserver索取topic，
找到topic主题之后，判断其所属的broker，建立长连接进行通讯，这是一个至关重要的路由的概念，重点，也是区别于其它版本的一个重要特性

5. defaultTopicQueueNums
在发送消息时，自动创建服务器不存在的Topic，默认创建的队列数

6. autoCreateTopicEnable
是否允许Broker自动创建Topic。建议线下开启，线上关闭

7. autoCreateSubscriptionGroup
是否允许Broker自动创建订阅组。建议线下开启，线上关闭

8. listenPort
broker对外服务的监听端口。

9. deleteWhen
删除文件时间点，默认是凌晨4点`deleteWhen=04`。

10. fileReservedTime
文件保留时间，默认48小时。

11. mapedFileSizeCommitLog
commitLog每个文件的大小默认1G。

12. storePathCommitLog
commitLog存储路径

13. mapedFileSizeConsumeQueue
ConsumeQueue每个文件默认存30W条，根据业务情况调整。

14. storePathConsumeQueue
消费队列存储路径

14. storePathIndex
消息索引存储路径

16. diskMaxUsedSpaceRatio
检测物理文件磁盘空间。

17. storePathRootDir
存储路径

18. destroyMapedFileIntervalForcibly

19. redeleteHangedFileInterval

20. storeCheckpoint
checkpoint 文件存储路径

21. abortFile
abort 文件存储路径

22. 限制消息的大小
maxMessageSize=65536
flushCommitLogLeastPages=4
flushConsumeQueueLeastPages=2
flushCommitLogThoroughInterval=10000
flushConsumeQueueThoroughInterval=60000

23. brokerRole
`ASYNC_MASTER`异步复制master，`SYNC_MASTER`同步双写master。

24. flushDiskType
`ASYNC_FLUSH`异步刷新磁盘，`SYNC_MASTER`同步刷新磁盘。

25. sendMessageTreadPoolNums
发消息线程池数量

26. pullMessageTreadPoolNums
拉消息线程池数量

