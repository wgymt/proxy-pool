# ProxyPool4j
代理池Java实现

# 主要参考了两位大神的作品：
1. `https://github.com/jhao104/proxy_pool`
2. `https://github.com/Python3WebSpider/ProxyPool`

# 使用
启动服务后, 默认配置下会开启 http://127.0.0.1:5050 的api接口服务:

| api | method | Description | params|
| ----| ---- | ---- | ----|
| /random | GET | 随机获取一个代理|   
| /count | GET | 查看代理数量 |
| /manualGet | GET | 手动抓取代理 | 

# 添加新的抓取源
- 步骤一：在`com/wgymt/proxypool/proxyFetcher`包中创建一个自定义Fetcher类 
- 步骤二：实现`ProxyFetcher`接口
- 步骤三：@FetcherType(value = "Xxxx")
- 步骤四：在`com.wgymt.proxypool.proxyFetcher.XxxxFetcher#fetch`方法实现目标代理网站的抓取逻辑
