package com.wgymt.proxypool.scheduler;

import com.wgymt.proxypool.service.ProxyFetcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 定时代理ip抓取调度器
 */
@Component
public class ProxyFetcherScheduler {

    private final Logger logger = LoggerFactory.getLogger(ProxyFetcherScheduler.class);

    @Resource
    private ProxyFetcherService proxyFetcherService;

    // 8分钟获取一次各网站的代理ip
    @Scheduled(cron = "0 0/8 * * * ?")
    public void autoFetcher() {
        logger.info("=============== 代理ip抓取开始 ==================");
        try {
            // 缓存中超过300个ip,则终止抓取动作
            String count = proxyFetcherService.getCacheIpsCount();
            System.out.println("缓存中代理数量: " + count);
            if (Integer.parseInt(count) > 500) {
                logger.info("缓存中存在足够代理ip({}个), 暂停自动抓取动作", count);
                return;
            }
            // 抓取代理ip,并入库
            List<String> proxies = proxyFetcherService.getProxies();
            logger.info("自动获取代理ip成功: 数量为: {}", proxies.size());
        } catch (IOException e) {
            logger.error("自动抓取代理ip失败: " + e);
        }
        logger.info("=============== 代理ip抓取结束 ==================");
    }

}
