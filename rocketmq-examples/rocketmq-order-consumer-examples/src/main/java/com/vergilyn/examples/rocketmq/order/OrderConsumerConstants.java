package com.vergilyn.examples.rocketmq.order;

import com.vergilyn.examples.rocketmq.RocketConstants;

public interface OrderConsumerConstants {
	String NAMESRV_ADDR = RocketConstants.NAMESRV_ADDR;

	String KEY_ORDER = "order";

	String GROUP_CONSUMER = "vergilyn_consumer_group_" + KEY_ORDER;
	String GROUP_PRODUCER = "vergilyn_producer_group_" + KEY_ORDER;

	String TOPIC = "vergilyn_topic_" + KEY_ORDER;

	String TAG_ORDER_1 = KEY_ORDER + "_1";
	String TAG_ORDER_2 = KEY_ORDER + "_2";
	String TAG_ORDER_3 = KEY_ORDER + "_3";
	String TAG_OTHER_1 = "other_1";
	String TAG_OTHER_2 = "other_2";
	String TAG_IGNORE_1 = "ignore_1";

}
