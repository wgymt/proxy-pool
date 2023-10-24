package com.wgymt.proxypool.service.impl;

import com.wgymt.proxypool.common.FetcherConstants;
import com.wgymt.proxypool.interfaces.ProxyFetcher;
import com.wgymt.proxypool.interfaces.ProxyFetcherContext;
import com.wgymt.proxypool.service.ProxyFetcherService;
import com.wgymt.proxypool.utils.FetcherThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class ProxyFetcherServiceImpl implements ProxyFetcherService {

    private final Logger logger = LoggerFactory.getLogger(ProxyFetcherServiceImpl.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProxyFetcherContext proxyFetcherContext;

    @Override
    public List<String> getProxies() {
        // 从上下文中获取所有代理源下载器
        Map<String, ProxyFetcher> proxyFetchers = proxyFetcherContext.getProxyFetchers();
        logger.info("代理源数量: {}", proxyFetchers.size());

        // 获取代理源下载器实现类集合,方便后续多线程操作
        List<ProxyFetcher> fetchers = new ArrayList<>(proxyFetchers.values());

        // 集合容器保存抓取的代理ip
        List<String> ips = Collections.synchronizedList(new ArrayList<>());
        for (ProxyFetcher fetcher : fetchers) {
            Future<List<String>> submit = FetcherThreadPool.getThreadPool().submit(fetcher::fetch);
            try {
                ips.addAll(submit.get());
            } catch (ExecutionException | InterruptedException e) {
                logger.error("抓取过程出错: " + e);
            }
        }

        // 新增ip,批量保存收到缓存中
        Set<ZSetOperations.TypedTuple<String>> tupleSet = ips.stream()
                .map(ip -> new DefaultTypedTuple<>(ip, 10.0)).collect(Collectors.toSet());
        stringRedisTemplate.opsForZSet().add(FetcherConstants.PROXIES_KEY, tupleSet);

        // 返回代理ip集合
        logger.info("代理ip数量: {}", ips.size());
        return ips;
    }

    @Override
    public String getByRedis() {
        // 获取缓存中得分最高的ip,并返回
        Set<String> proxies = stringRedisTemplate.opsForZSet().reverseRange(FetcherConstants.PROXIES_KEY, 0, 1);
        if (CollectionUtils.isEmpty(proxies)) return "没有合适的代理ip";
        return new ArrayList<>(proxies).get(0);
    }

    @Override
    public String getCacheIpsCount() {
        Long count = stringRedisTemplate.opsForZSet().zCard(FetcherConstants.PROXIES_KEY);
        if (Objects.isNull(count)) return "缓存中没有代理ip";
        return String.valueOf(count);
    }

    @Override
    public String getByRedisRandom() {
        Set<String> ipByScore = stringRedisTemplate.opsForZSet().rangeByScore(FetcherConstants.PROXIES_KEY, 50, 101);
        if (CollectionUtils.isEmpty(ipByScore)) return "没有合适的ip";

        // 计算分数范围内的ip数量
        int ipCount = ipByScore.size();
        logger.info("分数值在50-100的ip数量: {}", ipCount);

        // 自动获取
        List<String> ipList = new ArrayList<>(ipByScore);
        int randomIndex = new Random().nextInt(ipCount);
        return ipList.get(randomIndex);
    }

}
