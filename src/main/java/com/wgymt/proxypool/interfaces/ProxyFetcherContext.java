package com.wgymt.proxypool.interfaces;

import java.util.Map;

/**
 * 代理下载器上下文
 */
public interface ProxyFetcherContext {
    Map<String, ProxyFetcher> getProxyFetchers();
}
