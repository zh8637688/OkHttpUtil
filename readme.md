# 封装OkHttp工具
- 支持GET、POST请求
- 支持上传、下载
- 失败重试
- 设置超时时间
- Cookie
- 设置UserAgent
- Cache
- Proxy

初始化配置
```
Configuration configuration = new Configuration.Builder(this)
                .setTimeoutInSec(30)
                .setCache(getCacheDir().getAbsolutePath() + File.separator + "request", 1024 * 1024 * 50)
                .setRetryCount(3)
                .setUserAgent("android")
                .setProxy("192.168.136.159", 8888)
                .build();
HttpRequestUtil.getInstance().init(this, configuration);
```

GET请求
```
Request request = new Request.Builder(URL)
                .method(Request.Method.GET)
                .addParam("param", "1")
                .addHeader("Cache-Control", "no-cache")
                .timeoutInSec(5)
                .retryCount(3)
                .build();
HttpRequestUtil.getInstance().asyncRequest(request, new RequestCallback() {
    @Override
    public void onSuccess(String response) {

    }

    @Override
    public void onFail(Exception e) {

    }

    @Override
    public void onNoNetwork() {

    }
});
```