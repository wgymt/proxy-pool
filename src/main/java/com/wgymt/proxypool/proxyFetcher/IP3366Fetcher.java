package com.wgymt.proxypool.proxyFetcher;

import com.wgymt.proxypool.annotations.FetcherType;
import com.wgymt.proxypool.common.FetcherConstants;
import com.wgymt.proxypool.interfaces.ProxyFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Component
@FetcherType(value = "IP3366")
public class IP3366Fetcher implements ProxyFetcher {

    private final Logger logger = LoggerFactory.getLogger(IP3366Fetcher.class);

    @Override
    public List<String> fetch() {

        // 获取高匿/普通各五页网页url
        List<String> urls = getPageUrls();

        // 保存抓取的数据
        List<String> ips = new ArrayList<>();

        for (String url : urls) {
            ips.addAll(getIps(url));
        }

        logger.info("IP3366获取了: {}", ips.size());
        return ips;
    }

    /**
     * 根据url获取代理
     *
     * @param url 待拉取地址
     * @return 代理ip集合
     */
    private List<String> getIps(String url) {

        List<String> ips = new ArrayList<>();

        Document document;
        try {
            document = Jsoup.connect(url)
                    .userAgent(FetcherConstants.USER_AGENT)
                    .ignoreContentType(false)
                    .get();

            Elements elements = document.selectXpath("//*[@id='list']//tr");
            // 跳过第一行为表头
            IntStream.range(0, elements.size())
                    .filter(i -> i != 0)
                    .mapToObj(elements::get)
                    .forEach(element -> {
                        String ip = element.selectXpath("./td[1]").text().trim();
                        String port = element.selectXpath("./td[2]").text().trim();
                        ips.add(ip + ":" + port);
                    });
        } catch (IOException ex) {
            logger.error("IP3366获取失败: " + ex);
            return Collections.emptyList();
        }

        return ips;
    }

    /**
     * 处理原始url,获取后续页码的地址
     *
     * @return 各五页的url, size = 10
     */
    private List<String> getPageUrls() {
        List<String> urls = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            urls.add(FetcherConstants.IP3366_URL_1.replace("pageNum", String.valueOf(i)));
            urls.add(FetcherConstants.IP3366_URL_2.replace("pageNum", String.valueOf(i)));
        }
        return urls;
    }
}
