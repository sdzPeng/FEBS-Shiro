package cc.mrbird.febs.business.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Spring容器工具类（请在Spring容器启动完成时使用该工具类）
 *
 * @author yujx
 * @date 2019/07/16 10:24
 */
@Component
public final class ApplicationContextUtil implements ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext;

    private static DefaultListableBeanFactory beanDefinitionRegistry;

    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;

        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
        ApplicationContextUtil.beanDefinitionRegistry = (DefaultListableBeanFactory) configurableContext.getBeanFactory();
    }

    @Override
    public void setEnvironment(Environment environment) {
        ApplicationContextUtil.environment = environment;
    }

    public static <T> T getBean(Class<T> clazz) throws BeansException {
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String className, Class<T> clazz) throws BeansException {
        return applicationContext.getBean(className, clazz);
    }

    /* 通过spi取出对象，加入Spring容器中再取出 */
    public static <T> List<T> registryObj2ApplicationContextWithSpi(Class<T> tClass) {

        ServiceLoader<T> ts = ServiceLoader.load(tClass);

        // 存储从spring中取出的对象
        List<T> tList = new ArrayList<>();

        for (T t : ts) {
            // 子类型
            Class<T> clazz = (Class<T>) t.getClass();
            // 父类型
            T bean = registryObj2ApplicationContext(clazz);
            tList.add(bean);
        }

        return tList;
    }

    /* 通过class对象将其注入Spring容器中，并从容器中取出 */
    public static <V> V registryObj2ApplicationContext(Class<? extends V> clazz) {
        // 获取实现类类名，将首字母小写
        String simpleName = clazz.getSimpleName();
        String beanName = StringUtils.uncapitalize(simpleName);

        // 注入Spring容器
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);

        // 从容器中把它取出
        return applicationContext.getBean(beanName, clazz);
    }

    /* 从配置文件中获取配置信息 */
    public static <T> T getConfigInfo(String key, Class<T> tClass) {
        return environment.getProperty(key, tClass);
    }

    private ApplicationContextUtil() {
    }
}