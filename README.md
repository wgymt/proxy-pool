# ProxyPool4j
代理池Java实现

# 主要参考了两位大神的作品：
1. `https://github.com/jhao104/proxy_pool`
2. `https://github.com/Python3WebSpider/ProxyPool`

# 添加新的抓取源
- 步骤一：在下面的包中创建一个自定义Fetcher类
  `com/wgymt/proxypool/proxyFetcher`
- 步骤二：实现`ProxyFetcher`接口
- 步骤三：@FetcherType(value = "Xxxx")
- 步骤四：在下面的方法实现目标代理网站的抓取逻辑
  `com.wgymt.proxypool.proxyFetcher.XxxxFetcher#fetch`
