ARG TAGS_RABBITMQ=${TAGS_RABBITMQ}

## 基于"Ubuntu 20.04.2"，没有tzdata，无法直接`ENV TZ`更改timezone
FROM rabbitmq:${TAGS_RABBITMQ}

ARG TIME_ZONE='Asia/Shanghai'
ENV TZ=${TIME_ZONE}

## 安装 tzdata，并设置timezone
RUN apt update \
    && apt install -y tzdata \
    && ln -fs /usr/share/zoneinfo/Asia/Chongqing /etc/localtime \
    && echo Asia/Chongqing > /etc/timezone \
    && dpkg-reconfigure --frontend noninteractive tzdata \
    && rm -rf /var/lib/apt/lists/*

## 预创建 user & permissions
# - <https://rabbitmq.com/access-control.html>
# - Schema Definition Export and Import: <https://rabbitmq.com/definitions.html>
#
# 关联`rabbitmq.conf`存在"management.load_definitions = /opt/rabbitmq/definitions.json"
COPY ./config/definitions.json /opt/rabbitmq/

## 安装指定插件 https://github.com/rabbitmq/rabbitmq-delayed-message-exchange
# FIMXE 2021-08-06 可否通过扩展`docker-entrypoint.sh`安装需要的plugins & enable-plugins？
#
# 关联`enabled_plugins`中配置了"rabbitmq_delayed_message_exchange"
COPY ./plugins/rabbitmq_delayed_message_exchange-3.9.0.ez ${RABBITMQ_HOME}/plugins

CMD ["rabbitmq-server"]