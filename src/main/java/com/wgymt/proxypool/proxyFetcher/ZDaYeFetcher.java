package com.wgymt.proxypool.proxyFetcher;

import com.wgymt.proxypool.annotations.FetcherType;
import com.wgymt.proxypool.common.FetcherConstants;
import com.wgymt.proxypool.interfaces.ProxyFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@FetcherType(value = "ZDaYe")
public class ZDaYeFetcher implements ProxyFetcher {

    private final Logger logger = LoggerFactory.getLogger(ZDaYeFetcher.class);

    @Override
    public List<String> fetch() {
        Document document;
        try {
            document = Jsoup.connect(FetcherConstants.ZDaYe_URL)
                    .userAgent(FetcherConstants.USER_AGENT)
                    .timeout(100)
                    .get();
        } catch (IOException ex) {
            logger.error("站大爷ip拉取失败: " + ex);
            return Collections.emptyList();
        }

        List<String> ips = new ArrayList<>();

        Elements elements = document.selectXpath("//table//tr");

        if (CollectionUtils.isEmpty(elements)) {
            return Collections.emptyList();
        }

        for (Element element : elements) {
            logger.info("element: {}", element.text());
            String ip = element.selectXpath("td[1]/text()").get(0).text().trim();
            String port = element.selectXpath("td[2]/text()").get(0).text().trim();
            ips.add(ip + ":" + port);
        }

        return ips;
    }
}
