# kafka-examples

+ <https://kafka.apache.org/>
+ <https://spring.io/projects/spring-kafka/>
+ <https://github.com/spring-projects/spring-kafka>

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