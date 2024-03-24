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

package org.dromara.dynamictp.core.refresher;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.event.RefreshEvent;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.handler.ConfigHandler;
import org.dromara.dynamictp.core.support.BinderHelper;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;

/**
 * AbstractRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractRefresher implements Refresher, EnvironmentAware {

    protected final DtpProperties dtpProperties;

    protected Environment environment;

    protected AbstractRefresher(DtpProperties dtpProperties) {
        this.dtpProperties = dtpProperties;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void refresh(String content, ConfigFileTypeEnum fileType) {
        //如果更新的内容为空或者文件类型为空，则直接返回
        if (StringUtils.isBlank(content) || Objects.isNull(fileType)) {
            log.warn("DynamicTp refresh, empty content or null fileType.");
            return;
        }

        try {
            //获取到配置文件处理器，这里面维护了多重格式的文件解析器：比如properties、yaml以及json格式的解析器
            val configHandler = ConfigHandler.getInstance();
            //将配置文件解析为properties格式的数据
            val properties = configHandler.parseConfig(content, fileType);
            //当有配置变化的时候，执行刷新
            refresh(properties);
        } catch (IOException e) {
            log.error("DynamicTp refresh error, content: {}, fileType: {}", content, fileType, e);
        }
    }

    protected void refresh(Map<Object, Object> properties) {
        if (MapUtils.isEmpty(properties)) {
            log.warn("DynamicTp refresh, empty properties.");
            return;
        }
        //这一步很关键，将发生变化的属性绑定到DtpProperties对象上
        BinderHelper.bindDtpProperties(properties, dtpProperties);
        //使用更新后的DtpProperties相关属性去更新线程池属性
        doRefresh(dtpProperties);
    }

    protected void refresh(Environment environment) {
        BinderHelper.bindDtpProperties(environment, dtpProperties);
        doRefresh(dtpProperties);
    }

    protected void doRefresh(DtpProperties properties) {
        DtpRegistry.refresh(properties);
        publishEvent(properties);
    }

    protected boolean needRefresh(Set<String> changedKeys) {
        if (CollectionUtils.isEmpty(changedKeys)) {
            return false;
        }
        changedKeys = changedKeys.stream()
                .filter(str -> str.startsWith(MAIN_PROPERTIES_PREFIX))
                .collect(Collectors.toSet());
        return CollectionUtils.isNotEmpty(changedKeys);
    }

    private void publishEvent(DtpProperties dtpProperties) {
        RefreshEvent event = new RefreshEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }
}
