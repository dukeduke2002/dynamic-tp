/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.core.spring;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.util.ConstructorUtil;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.eager.EagerDtpExecutor;
import org.dromara.dynamictp.core.executor.eager.TaskQueue;
import org.dromara.dynamictp.common.plugin.DtpInterceptorRegistry;
import org.dromara.dynamictp.core.support.DynamicTp;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ScheduledThreadPoolExecutorProxy;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.dromara.dynamictp.core.support.DtpLifecycleSupport.shutdownGracefulAsync;

/**
 * BeanPostProcessor that handles all related beans managed by Spring.
 * 这个类是真正拦截我们的线程池对象的一个bean的后处理器，它是关键
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
@SuppressWarnings("all")
public class DtpPostProcessor implements BeanPostProcessor, BeanFactoryAware, PriorityOrdered {

    private static final String REGISTER_SOURCE = "beanPostProcessor";

    private DefaultListableBeanFactory beanFactory;

    /**
     * Compatible with lower versions of Spring.
     *
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use
     * @throws BeansException in case of errors
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 这个类对我们应用中定义的线程池进行拦截，并填充相关的map，以实现一些增强逻辑
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        //如果bean不是ThreadPoolExecutor或者ThreadPoolTaskExecutor，那么就不对bean做任何处理，直接返回
        if (!(bean instanceof ThreadPoolExecutor) && !(bean instanceof ThreadPoolTaskExecutor)) {
            return bean;
        }
        //如果bean是DtpExecutor,调用registerDtp方法填充DTP_REGISTRY这个map
        if (bean instanceof DtpExecutor) {
            return registerAndReturnDtp(bean);
        }
        // register juc ThreadPoolExecutor or ThreadPoolTaskExecutor
        return registerAndReturnCommon(bean, beanName);
    }

    private Object registerAndReturnDtp(Object bean) {
        DtpExecutor dtpExecutor = (DtpExecutor) bean;
        Object[] args = ConstructorUtil.buildTpExecutorConstructorArgs(dtpExecutor);
        Class<?>[] argTypes = ConstructorUtil.buildTpExecutorConstructorArgTypes();
        Set<String> pluginNames = dtpExecutor.getPluginNames();

        val enhancedBean = (DtpExecutor) DtpInterceptorRegistry.plugin(bean, pluginNames, argTypes, args);
        if (enhancedBean instanceof EagerDtpExecutor) {
            ((TaskQueue) enhancedBean.getQueue()).setExecutor((EagerDtpExecutor) enhancedBean);
        }
        DtpRegistry.registerExecutor(ExecutorWrapper.of(enhancedBean), REGISTER_SOURCE);
        return enhancedBean;
    }

    private Object registerAndReturnCommon(Object bean, String beanName) {
        String dtpAnnoValue;
        try {
            //如果bean上有DynamicTp注解
            DynamicTp dynamicTp = beanFactory.findAnnotationOnBean(beanName, DynamicTp.class);
            if (Objects.nonNull(dynamicTp)) {
                dtpAnnoValue = dynamicTp.value();
            } else {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
                    return bean;
                }
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                MethodMetadata methodMetadata = (MethodMetadata) annotatedBeanDefinition.getSource();
                if (Objects.isNull(methodMetadata) || !methodMetadata.isAnnotated(DynamicTp.class.getName())) {
                    return bean;
                }
                dtpAnnoValue = Optional.ofNullable(methodMetadata.getAnnotationAttributes(DynamicTp.class.getName()))
                        .orElse(Collections.emptyMap())
                        .getOrDefault("value", "")
                        .toString();
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("There is no bean with the given name {}", beanName, e);
            return bean;
        }
        //如果说bean上面的DynamicTp注解，使用注解的值作为线程池的名称，没有的话就使用bean的名称
        String poolName = StringUtils.isNotBlank(dtpAnnoValue) ? dtpAnnoValue : beanName;
        return doRegisterAndReturnCommon(bean, poolName);
    }

    private Object doRegisterAndReturnCommon(Object bean, String poolName) {
        if (bean instanceof ThreadPoolTaskExecutor) {
            val proxy = newProxy(poolName, ((ThreadPoolTaskExecutor) bean).getThreadPoolExecutor());
            try {
                ReflectionUtil.setFieldValue("threadPoolExecutor", bean, proxy);
            } catch (IllegalAccessException ignored) { }
            DtpRegistry.registerExecutor(new ExecutorWrapper(poolName, proxy), REGISTER_SOURCE);
            return bean;
        }
        Executor proxy;
        if (bean instanceof ScheduledThreadPoolExecutor) {
            proxy = newScheduledTpProxy(poolName, (ScheduledThreadPoolExecutor) bean);
        } else {
            proxy = newProxy(poolName, (ThreadPoolExecutor) bean);
        }
        DtpRegistry.registerExecutor(new ExecutorWrapper(poolName, proxy), REGISTER_SOURCE);
        return proxy;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private ThreadPoolExecutorProxy newProxy(String name, ThreadPoolExecutor originExecutor) {
        val proxy = new ThreadPoolExecutorProxy(originExecutor);
        shutdownGracefulAsync(originExecutor, name, 0);
        return proxy;
    }

    private ScheduledThreadPoolExecutorProxy newScheduledTpProxy(String name, ScheduledThreadPoolExecutor originExecutor) {
        val proxy = new ScheduledThreadPoolExecutorProxy(originExecutor);
        shutdownGracefulAsync(originExecutor, name, 0);
        return proxy;
    }
}
