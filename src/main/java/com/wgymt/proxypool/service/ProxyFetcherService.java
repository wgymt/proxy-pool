package com.wgymt.proxypool.service;

import java.io.IOException;
import java.util.List;

/**
 * 代理下载器业务接口
 */
public interface ProxyFetcherService {

    /**
     * 手动抓取各大网站的代理ip
     *
     * @return 所有ip集合(包括不能用的)
     * @throws IOException io异常
     */
    List<String> getProxies() throws IOException;

    /**
     * 从换从获取得分最高的ip
     *
     * @return 有效的ip, 形式[ip:port]
     */
    String getByRedis();

    /**
     * 计算缓存中代理ip的数量
     *
     * @return 代理ip数量
     */
    String getCacheIpsCount();

    /**
     * 随机获取一个分数值为100的代理ip
     *
     * @return 分数值为100的代理ip
     */
    String getByRedisRandom();
}
