package com.wgymt.proxypool.proxyFetcher;

import com.alibaba.fastjson.JSONObject;
import com.wgymt.proxypool.annotations.FetcherType;
import com.wgymt.proxypool.common.FetcherConstants;
import com.wgymt.proxypool.interfaces.ProxyFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@FetcherType(value = "FateZeroIp")
public class FateZeroFetcher implements ProxyFetcher {

    private final Logger logger = LoggerFactory.getLogger(FateZeroFetcher.class);

    @Override
    public List<String> fetch() {
        Element body;
        try {
            body = Jsoup.connect(FetcherConstants.Fate_Zero_URL)
                    .userAgent(FetcherConstants.USER_AGENT)
                    .ignoreContentType(true)
                    .get()
                    .body();
        } catch (IOException ex) {
            logger.error("FateZero免费ip拉取失败: " + ex);
            return Collections.emptyList();
        }

        // 报错: field null expect '[', but {,
        // 手动添加 [] 即可
        List<JSONObject> jsons = JSONObject.parseArray("[" + body.text() + "]", JSONObject.class);
        if (CollectionUtils.isEmpty(jsons)) return Collections.emptyList();

        logger.info("FateZeroIp抓取ip数量: {}", jsons.size());

        // 保存所有抓取的ip集合
        List<String> ips = new ArrayList<>();
        for (JSONObject json : jsons) {
            String host = (String) json.get("host");
            Integer port = (Integer) json.get("port");
            ips.add(host.trim() + ":" + String.valueOf(port).trim());
        }

        return ips;
    }
}
