package com.wgymt.proxypool.proxyFetcher;

import com.alibaba.fastjson.JSONArray;
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
import java.util.Map;

@Component
@FetcherType(value = "DocIP")
public class DocIPFetcher implements ProxyFetcher {

    private final Logger logger = LoggerFactory.getLogger(DocIPFetcher.class);

    @Override
    public List<String> fetch() {
        Element body;
        try {
            body = Jsoup.connect(FetcherConstants.DocIP_URL)
                    .userAgent(FetcherConstants.USER_AGENT)
                    .ignoreContentType(true)
                    .get()
                    .body();
        } catch (IOException ex) {
            logger.error("稻壳ip拉取失败: " + ex);
            return Collections.emptyList();
        }

        Map map = JSONObject.parseObject(body.text(), Map.class);
        if (CollectionUtils.isEmpty(map)) {
            return Collections.emptyList();
        }

        JSONArray ipJsonArray = (JSONArray) map.get("data");
        List<String> ipObjs = JSONObject.parseArray(String.valueOf(ipJsonArray), String.class);
        List<String> ips = new ArrayList<>();

        for (String ipObj : ipObjs) {
            Map ipMap = JSONObject.parseObject(ipObj, Map.class);
            String ip = (String) ipMap.get("ip");
            ips.add(ip);
        }
        //[47.107.61.215:8000, 121.233.207.45:8089, ... ]
        System.out.println(ips);

        return ips;
    }
}
