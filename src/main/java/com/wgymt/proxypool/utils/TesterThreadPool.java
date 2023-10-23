package com.wgymt.proxypool.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TesterThreadPool {

    private static volatile ThreadPoolExecutor threadPoolExecutor = null;

    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    private TesterThreadPool() {
    }

    /**
     * 主要为IO操作, 故核心线程数为2倍核心数
     *
     * @return 代理ip测试线程池
     */
    public static ThreadPoolExecutor getThreadPool() {
        if (threadPoolExecutor == null) {
            synchronized (TesterThreadPool.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                            PROCESSORS * 2,
                            PROCESSORS * 2,
                            60,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(100),
                            new ThreadFactoryBuilder().setNameFormat("ProxyTesterThread-%d").build(),
                            new ThreadPoolExecutor.CallerRunsPolicy());
                }
            }
        }
        return threadPoolExecutor;
    }
}
