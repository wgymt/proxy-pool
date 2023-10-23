package com.wgymt.proxypool.interfaces;

import java.io.IOException;
import java.util.List;

/**
 * 代理下载器接口
 */
public interface ProxyFetcher {
    List<String> fetch() throws IOException;
}
