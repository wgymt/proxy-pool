package com.wgymt.proxypool.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FetcherThreadPool {

    private static volatile ThreadPoolExecutor threadPoolExecutor = null;

    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    private FetcherThreadPool() {
    }

    /**
     * 主要为IO操作, 故核心线程数为2倍核心数
     *
     * @return 代理源下载器线程池
     */
    public static ThreadPoolExecutor getThreadPool() {
        if (threadPoolExecutor == null) {
            synchronized (FetcherThreadPool.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                            PROCESSORS * 2,
                            PROCESSORS * 2,
                            60,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(100),
                            new ThreadFactoryBuilder().setNameFormat("ProxyFetcherThread-%d").build(),
                            new ThreadPoolExecutor.CallerRunsPolicy());
                }
            }
        }
        return threadPoolExecutor;
    }
}
