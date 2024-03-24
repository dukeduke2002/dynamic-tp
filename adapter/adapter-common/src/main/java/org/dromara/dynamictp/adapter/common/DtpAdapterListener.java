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

package org.dromara.dynamictp.adapter.common;

import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.common.event.AlarmCheckEvent;
import org.dromara.dynamictp.common.event.CollectEvent;
import org.dromara.dynamictp.common.event.RefreshEvent;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.handler.CollectorHandler;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.SCHEDULE_NOTIFY_ITEMS;

/**
 * DtpAdapterListener related
 * 这也是一个比较关键的类，实现了GenericApplicationListener接口，算是一个事件监听器，主要监听RefreshEvent、CollectEvent、AlarmCheckEvent这三个事件。
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class DtpAdapterListener implements GenericApplicationListener {

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return RefreshEvent.class.isAssignableFrom(type)
                    || CollectEvent.class.isAssignableFrom(type)
                    || AlarmCheckEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    /**
     * 这里就是集中处理所有框架产生的事件了，我们先来看看事件都是在什么时机发布的。
     * @param event
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        try {
            // 还记得我们在我们的应用中引入了dynamic-tp-spring-boot-starter-nacos这个starter么，
            // 在这个starter中有个自动装配类:DtpAutoConfiguration,
            // 在这个配置类中初始化了唯一的bean就是NacosRefresher，它负责配置更新时的刷新。
            if (event instanceof RefreshEvent) {
                doRefresh(((RefreshEvent) event).getDtpProperties());
            } else if (event instanceof CollectEvent) {
                doCollect(((CollectEvent) event).getDtpProperties());
            } else if (event instanceof AlarmCheckEvent) {
                doAlarmCheck(((AlarmCheckEvent) event).getDtpProperties());
            }
        } catch (Exception e) {
            log.error("DynamicTp adapter, event handle failed.", e);
        }
    }

    /**
     * Compatible with lower versions of spring.
     *
     * @param sourceType sourceType
     * @return true if support
     */
    @Override
    public boolean supportsSourceType(@Nullable Class<?> sourceType) {
        return true;
    }

    /**
     * Compatible with lower versions of spring.
     *
     * @return order
     */
    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * Do collect thread pool stats.
     * @param dtpProperties dtpProperties
     */
    protected void doCollect(DtpProperties dtpProperties) {
        val handlerMap = ApplicationContextHolder.getBeansOfType(DtpAdapter.class);
        if (CollectionUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> v.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorTypes())));
    }

    /**
     * Do refresh.
     * @param dtpProperties dtpProperties
     */
    protected void doRefresh(DtpProperties dtpProperties) {
        val handlerMap = ApplicationContextHolder.getBeansOfType(DtpAdapter.class);
        if (CollectionUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> v.refresh(dtpProperties));
    }

    /**
     * Do alarm check.
     * @param dtpProperties dtpProperties
     */
    protected void doAlarmCheck(DtpProperties dtpProperties) {
        val handlerMap = ApplicationContextHolder.getBeansOfType(DtpAdapter.class);
        if (CollectionUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> {
            val executorWrapper = v.getExecutorWrappers();
            executorWrapper.forEach((kk, vv) -> AlarmManager.tryAlarmAsync(vv, SCHEDULE_NOTIFY_ITEMS));
        });
    }
}
