package net.ysq.webchat.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Description: 提供手动获取被spring管理的bean对象
 */
public class SpringUtils implements ApplicationContextAware {

	private static ApplicationContext appContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (appContext == null) {
			appContext = applicationContext;
		}
	}

	// 获取applicationContext
	public static ApplicationContext getAppContext() {
		return appContext;
	}

	// 通过name获取 Bean.
	public static Object getBean(String name) {
		return getAppContext().getBean(name);
	}

	// 通过class获取Bean.
	public static <T> T getBean(Class<T> clazz) {
		return getAppContext().getBean(clazz);
	}

	// 通过name,以及Clazz返回指定的Bean
	public static <T> T getBean(String name, Class<T> clazz) {
		return getAppContext().getBean(name, clazz);
	}

}
