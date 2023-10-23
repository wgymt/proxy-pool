package com.wgymt.proxypool.scheduler;

import com.google.common.collect.Lists;
import com.wgymt.proxypool.common.FetcherConstants;
import com.wgymt.proxypool.utils.TesterThreadPool;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 定时代理ip校验调度器
 * 主要功能: 筛选出可用的代理ip,并打分,根据分数
 */
@Component
public class ProxyTesterScheduler {

    private final Logger logger = LoggerFactory.getLogger(ProxyTesterScheduler.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 3分钟校验一次缓存中的代理ip是否可用
    @Scheduled(cron = "0 0/3 * * * ?")
    public void autoTester() {
        logger.info("=============== 代理ip检验开始 ==================");
        // 遍历缓存中的代理ip
        Set<String> ips = stringRedisTemplate.opsForZSet().range(FetcherConstants.PROXIES_KEY, 0, -1);
        // 若缓存中无代理ip,则终止自动测试动作
        if (CollectionUtils.isEmpty(ips)) {
            logger.info("缓存中无代理ip, 终止自动校验ip有效性动作");
            return;
        }

        // 多线程测试代理ip
        // 结果map的key为validIps/invalidIps
        Map<String, List<String>> ipsMap = multiTestIp(ips);

        // 测试通过的ip,分数刷新为100
        List<String> validIps = new CopyOnWriteArrayList<>(ipsMap.get("validIps"));

        logger.info("测试通过的ip数量: {}", validIps.size());

        if (!CollectionUtils.isEmpty(validIps)) {
            Set<ZSetOperations.TypedTuple<String>> tupleSet = validIps.stream()
                    .map(ip -> new DefaultTypedTuple<>(ip, 100.0)).collect(Collectors.toSet());
            stringRedisTemplate.opsForZSet().add(FetcherConstants.PROXIES_KEY, tupleSet);
        }

        List<String> invalidIps = new CopyOnWriteArrayList<>(ipsMap.get("invalidIps"));
        if (CollectionUtils.isEmpty(invalidIps)) {
            logger.info("=============== 没有无效ip,检验结束 ==================");
            return;
        }

        logger.info("测试失败的ip数量: {}", invalidIps.size());

        // 测试失败的ip分数-1
        for (String invalidIp : invalidIps) {
            stringRedisTemplate.opsForZSet().incrementScore(FetcherConstants.PROXIES_KEY, invalidIp, -1);
            // 若分数为0, 则从缓存中删除
            Double score = stringRedisTemplate.opsForZSet().score(FetcherConstants.PROXIES_KEY, invalidIp);
            if (score != null && score < 1) {
                stringRedisTemplate.opsForZSet().remove(FetcherConstants.PROXIES_KEY, invalidIp);
            }
        }

        logger.info("=============== 代理ip检验结束 ==================");
    }

    /**
     * 多线程校验ip
     *
     * @param ips 待测试ip集合
     * @return 验证过后的集合(通过 / 不通过), 封装成map
     */
    private Map<String, List<String>> multiTestIp(Set<String> ips) {
        Map<String, List<String>> map = new HashMap<>();

        // 通过测试的ip容器
        List<String> validIps = Collections.synchronizedList(new ArrayList<>(500));
        // 未通过测试的ip容器
        List<String> invalidIps = Collections.synchronizedList(new ArrayList<>(500));

        // 总ip集合分段,方便控制多线程进度
        List<List<String>> ipsList = Lists.partition(new ArrayList<>(ips), 10);
        logger.info("总ip集合分段数: {}", ipsList.size());

        CountDownLatch latch = new CountDownLatch(ipsList.size());

        // 记录日志
        List<String> validLogs = Collections.synchronizedList(new ArrayList<>(1000));
        List<String> invalidLogs = Collections.synchronizedList(new ArrayList<>(1000));
        for (List<String> ipList : ipsList) {
            for (String ip : ipList) {
                // 多个代理ip异步发送测试请求
                TesterThreadPool.getThreadPool().execute(() -> {
                    Boolean isPassed = testProxyIp(ip);
                    if (isPassed) {
                        validIps.add(ip);
                        validLogs.add("validIp: " + ip);
                    } else {
                        invalidIps.add(ip);
                        invalidLogs.add("invalidIp: " + ip);
                    }
                });
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }

        logger.info(validLogs.toString());
        logger.info(invalidLogs.toString());

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("CountDownLatch await 异常" + e);
        }

        map.put("validIps", validIps);
        map.put("invalidIps", invalidIps);

        return map;
    }

    /**
     * 测试代理ip是否可用
     *
     * @param proxyIp 待测试id
     * @return 如果测试已通过, 返回TRUE, 否则FALSE
     */
    public Boolean testProxyIp(String proxyIp) {
        try {
            String[] split = proxyIp.split(":");
            String ip = split[0].trim();
            String port = split[1].trim();
            Connection.Response response = Jsoup.connect(FetcherConstants.TEST_URL)
                    .proxy(ip, Integer.parseInt(port))
                    .userAgent(FetcherConstants.USER_AGENT)
                    .timeout(2500)
                    //.ignoreHttpErrors(true)
                    //.ignoreContentType(true)
                    .execute();
            int statusCode = response.statusCode();
            logger.info("ip有效性测试,返回状态码为: {}", statusCode);
            // 测试通过,则返回True
            if (statusCode == 200 || statusCode == 206 || statusCode == 302) {
                return Boolean.TRUE;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("ip有效性测试失败: " + e);
        }

        return Boolean.FALSE;
    }

}
