package com.wgymt.proxypool.interfaces.impl;

import com.wgymt.proxypool.annotations.FetcherType;
import com.wgymt.proxypool.interfaces.ProxyFetcher;
import com.wgymt.proxypool.interfaces.ProxyFetcherContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理下载器上下文实现类
 * 1.项目启动时,获取上下文中被代理来源注解标识的代理下载器实现类
 * 2.封装代理下载器Map, key为代理来源注解值, value为对应的下载器实现类
 * 3.返回封装好的代理下载器Map, 提供给调度器使用
 *
 * @see FetcherType 代理来源注解
 */
@Component
public class ProxyFetcherContextImpl implements ApplicationContextAware, ProxyFetcherContext {

    private static final Map<String, ProxyFetcher> FETCHER_MAP = new HashMap<>();

    @Override
    public Map<String, ProxyFetcher> getProxyFetchers() {
        return FETCHER_MAP;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, ProxyFetcher> map = applicationContext.getBeansOfType(ProxyFetcher.class);
        map.values().forEach(v -> {
            String fetcherType = v.getClass().getAnnotation(FetcherType.class).value();
            FETCHER_MAP.put(fetcherType, v);
        });
    }

}
