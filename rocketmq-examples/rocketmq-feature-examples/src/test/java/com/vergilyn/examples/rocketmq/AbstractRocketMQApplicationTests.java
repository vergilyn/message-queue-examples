package com.vergilyn.examples.rocketmq;

import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author vergilyn
 * @date 2020-06-15
 */
@SpringBootTest(classes = RocketMQApplication.class)
public abstract class AbstractRocketMQApplicationTests extends AbstractRocketMQTests{

	@Autowired
	protected ApplicationContext applicationContext;

	/**
	 * @see ListenerContainerConfiguration#afterSingletonsInstantiated()
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
	 */
	protected <T> T registryAnnoRocketMQListener(Class<T> componentClass)
			throws InvocationTargetException, IllegalAccessException {

		// 1. 手动registry-class，并由spring生成bean
		AnnotationConfigApplicationContext annotationApplicationContext = (AnnotationConfigApplicationContext) applicationContext;
		annotationApplicationContext.register(componentClass);
		ListenerContainerConfiguration listenerContainerConfiguration = applicationContext.getBean(ListenerContainerConfiguration.class);

		// 2. 2022-07-06，没有找到别的方式通过代码注册`@RocketMQMessageListener`，
		//   所以通过反射调用。
		Method registerContainerMethod = ReflectionUtils.findMethod(ListenerContainerConfiguration.class, "registerContainer",
		                                                      String.class, Object.class);

		ReflectionUtils.makeAccessible(registerContainerMethod);

		T bean = annotationApplicationContext.getBean(componentClass);
		String[] beanNames = annotationApplicationContext.getBeanNamesForType(bean.getClass());

		registerContainerMethod.invoke(listenerContainerConfiguration, beanNames[0], bean);

		return bean;
	}
}
