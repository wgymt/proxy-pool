package com.wgymt.proxypool.controller;

import com.wgymt.proxypool.service.ProxyFetcherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class FetcherController {

    @Resource
    private ProxyFetcherService proxyFetcherService;

    @GetMapping
    public String index() {
        return "<h2>Welcome to ProxyPool4j</h2>";
    }

    /**
     * 拉取所有被下载器注解标记的网站的代理ip
     *
     * @return 代理ip集合
     */
    @GetMapping("/manualGet")
    public String getProxies() {
        try {
            return proxyFetcherService.getProxies().toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("获取代理ip失败");
        }
    }

    /**
     * 从缓存中获取第一个高分的代理ip
     *
     * @return 一个代理ip
     */
    @GetMapping("/get")
    public String getByRedis() {
        return proxyFetcherService.getByRedis();
    }

    /**
     * 从缓存中随机获取一个高分的代理ip
     *
     * @return 随机一个代理ip
     */
    @GetMapping("/random")
    public String getByRedisRandom() {
        return proxyFetcherService.getByRedisRandom();
    }

    /**
     * 获取在缓存中的所有代理ip的数量
     *
     * @return 缓存代理ip的数量
     */
    @GetMapping("/count")
    public String getIpsCount() {
        return proxyFetcherService.getCacheIpsCount();
    }

}
