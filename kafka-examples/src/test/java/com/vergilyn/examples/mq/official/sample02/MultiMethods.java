
package com.vergilyn.examples.mq.official.sample02;

import com.vergilyn.examples.mq.common.Bar2;
import com.vergilyn.examples.mq.common.Foo2;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author Gary Russell
 * @since 5.1
 *
 */
@Component
@KafkaListener(id = "multiGroup", topics = { "foos", "bars" })
public class MultiMethods {

	@KafkaHandler
	public void foo(Foo2 foo) {
		System.out.println("received foo: " + foo);
	}

	@KafkaHandler
	public void bar(Bar2 bar) {
		System.out.println("received bar: " + bar);
	}

	@KafkaHandler(isDefault = true)
	public void unknown(Object object) {
		System.out.println("1 unknown: " + object);
	}

}
