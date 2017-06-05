package cz.okhttputil;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cz.okhttputil.call.AsyncCall;
import cz.okhttputil.callback.DownloadCallback;
import cz.okhttputil.callback.IRequestCallback;
import cz.okhttputil.callback.RequestCallback;
import cz.okhttputil.cookie.CookieJarImp;
import cz.okhttputil.interceptor.RetryInterceptor;
import cz.okhttputil.request.PostFileRequest;
import cz.okhttputil.request.Request;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Http请求工具
 * Created by haozhou on 2017/5/27.
 * <p>
 * TODO：上传进度、断点续传（下载）、缓存机制、mockService
 */
public class HttpRequestUtil {
    private static volatile HttpRequestUtil instance;

    private Context context;
    private Configuration configuration;
    private OkHttpClient defaultClient;
    private RetryInterceptor defaultRetryInterceptor;

    private HttpRequestUtil() {
    }

    public static HttpRequestUtil getInstance() {
        if (instance == null) {
            synchronized (HttpRequestUtil.class) {
                if (instance == null) {
                    instance = new HttpRequestUtil();
                }
            }
        }
        return instance;
    }

    public void init(Context context, Configuration config) {
        this.context = context.getApplicationContext();
        if (config == null) {
            config = new Configuration.Builder(context).build();
        }
        configuration = config;
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(config.getTimeoutInSec(), TimeUnit.SECONDS)
                .readTimeout(config.getTimeoutInSec(), TimeUnit.SECONDS)
                .writeTimeout(config.getTimeoutInSec(), TimeUnit.SECONDS)
                .cache(new Cache(new File(config.getCachePath()), config.getCacheSize()))
                .cookieJar(new CookieJarImp(context))
                .proxy(config.useProxy() ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyHost(), config.getProxyPort())) : null);
        if (config.getRetryCount() > 0) {
            defaultRetryInterceptor = new RetryInterceptor(config.getRetryCount());
            clientBuilder.addInterceptor(defaultRetryInterceptor);
        }
        defaultClient = clientBuilder.build();
    }

    private void checkInitState() {
        if (configuration == null) {
            throw new IllegalStateException("call HttpRequestUtil.init(Context, Configuration) first");
        }
    }

    private boolean needNewClient(Request request) {
        return request != null && ((request.getRetryCount() >= 0 && request.getRetryCount() != configuration.getRetryCount())
                || (request.getTimeoutInSec() >= 0 && request.getTimeoutInSec() != configuration.getTimeoutInSec()));
    }

    private OkHttpClient genOkClientIfNeed(Request request) {
        OkHttpClient okClient = defaultClient;
        if (needNewClient(request)) {
            OkHttpClient.Builder clientBuilder = defaultClient.newBuilder();
            if (request.getTimeoutInSec() >= 0 && request.getTimeoutInSec() != configuration.getTimeoutInSec()) {
                clientBuilder.connectTimeout(request.getTimeoutInSec(), TimeUnit.SECONDS)
                        .readTimeout(request.getTimeoutInSec(), TimeUnit.SECONDS)
                        .writeTimeout(request.getTimeoutInSec(), TimeUnit.SECONDS);
            }
            if (request.getRetryCount() >= 0 && request.getRetryCount() != configuration.getRetryCount()) {
                List<Interceptor> interceptors = clientBuilder.interceptors();
                if (interceptors != null) {
                    interceptors.remove(defaultRetryInterceptor);
                }
                clientBuilder.addInterceptor(new RetryInterceptor(request.getRetryCount()));
            }
            okClient = clientBuilder.build();
        }
        return okClient;
    }

    private okhttp3.Request genOkRequest(Request request) {
        okhttp3.Request.Builder okRequestBuilder = new okhttp3.Request.Builder();

        Request.Method method = request.getMethod();
        switch (method) {
            case GET:
                genGetRequest(okRequestBuilder, request);
                break;
            case POST:
                genPostRequest(okRequestBuilder, request);
                break;
        }

        Map<String, String> headers = request.getHeader();
        if (headers != null) {
            for (String key : headers.keySet()) {
                okRequestBuilder.addHeader(key, headers.get(key));
            }
        }

        if (!TextUtils.isEmpty(configuration.getUserAgent())) {
            okRequestBuilder.addHeader("User-Agent", configuration.getUserAgent());
        }

        return okRequestBuilder.build();
    }

    private static void genGetRequest(okhttp3.Request.Builder builder, Request request) {
        StringBuilder url = new StringBuilder(request.getUrl());
        boolean encode = request.isEncodeParams();

        Map<String, String> params = request.getParams();
        if (params != null && params.size() > 0) {
            url.append("?");
            int index = 0;
            for (String key : params.keySet()) {
                String value = params.get(key);
                if (encode) {
                    try {
                        value = URLEncoder.encode(value, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                url.append(key).append("=").append(value);
                if (index < params.size() - 1) {
                    url.append("&");
                }
                index++;
            }
        }

        builder.url(url.toString());
    }

    private void genPostRequest(okhttp3.Request.Builder builder, Request request) {
        builder.url(request.getUrl());

        if (request instanceof PostFileRequest) {
            genFileBody(builder, (PostFileRequest) request);
        } else {
            genFormBody(builder, request);
        }
    }

    private void genFormBody(okhttp3.Request.Builder builder, Request request) {
        Map<String, String> params = request.getParams();
        if (params != null && params.size() > 0) {
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            for (String key : params.keySet()) {
                bodyBuilder.add(key, params.get(key));
            }
            builder.post(bodyBuilder.build());
        }
    }

    private void genFileBody(okhttp3.Request.Builder builder, PostFileRequest request) {
        Map<String, File> files = request.getFiles();
        if (files.size() == 0) {
            genFormBody(builder, request);
        } else {
            Map<String, String> params = request.getParams();
            if (params != null && params.size() > 0
                    || files.size() > 1) {
                genMultiBody(builder, params, files);
            } else {
                File file = files.get(files.keySet().iterator().next());
                builder.post(RequestBody.create(MediaType.parse("application/octet-stream"), file));
            }
        }
    }

    private void genMultiBody(okhttp3.Request.Builder builder, Map<String, String> params, Map<String, File> files) {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (String key : params.keySet()) {
            bodyBuilder.addFormDataPart(key, params.get(key));
        }
        for (String key : files.keySet()) {
            File file = files.get(key);
            bodyBuilder.addFormDataPart(key, file.getName(),
                    RequestBody.create(MediaType.parse("application/octet-stream"), file));
        }
        builder.post(bodyBuilder.build());
    }

    public void syncRequest(Request request) {
        syncRequest(request, null);
    }

    public void syncRequest(Request request, IRequestCallback callback) {
        checkInitState();

        if (!NetworkUtil.isNetworkAvailable(context)) {
            if (callback != null) {
                callback.onNoNetwork();
            }
            return;
        }

        if (request != null) {
            OkHttpClient okClient = genOkClientIfNeed(request);
            okhttp3.Request okRequest = genOkRequest(request);

            ResponseBody responseBody = null;
            try {
                Response okResponse = okClient.newCall(okRequest).execute();
                responseBody = okResponse.body();
                processResponseBody(responseBody, callback);
            } catch (IOException e) {
                if (callback != null) {
                    callback.onFail(e);
                }
            } finally {
                if (responseBody != null) {
                    responseBody.close();
                }
            }
        } else {
            if (callback != null) {
                callback.onFail(new NullPointerException("Request is null"));
            }
        }
    }

    public AsyncCall asyncRequest(Request request) {
        return asyncRequest(request, null);
    }

    public AsyncCall asyncRequest(Request request, final IRequestCallback callback) {
        checkInitState();

        if (!NetworkUtil.isNetworkAvailable(context)) {
            if (callback != null) {
                callback.onNoNetwork();
            }
        } else {
            if (request != null) {
                OkHttpClient okClient = genOkClientIfNeed(request);
                okhttp3.Request okRequest = genOkRequest(request);

                Call realCall = okClient.newCall(okRequest);
                final AsyncCall asyncCall = new AsyncCall(realCall);
                realCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        asyncCall.completed();
                        if (callback != null) {
                            callback.onFail(e);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        asyncCall.completed();
                        ResponseBody responseBody = response.body();
                        try {
                            processResponseBody(responseBody, callback);
                        } catch (IOException e) {
                            if (callback != null) {
                                callback.onFail(e);
                            }
                        } finally {
                            responseBody.close();
                        }
                    }
                });
                return asyncCall;
            } else {
                if (callback != null) {
                    callback.onFail(new NullPointerException("Request is null"));
                }
            }
        }

        return new AsyncCall(null);
    }

    private void processResponseBody(ResponseBody body, IRequestCallback callback) throws IOException {
        if (callback != null) {
            if (callback instanceof RequestCallback) {
                ((RequestCallback) callback).onSuccess(body.string());
            } else if (callback instanceof DownloadCallback) {
                saveToFile(body.byteStream(), body.contentLength(), (DownloadCallback) callback);
            }
        }
    }

    private void saveToFile(InputStream is, long fileSize, DownloadCallback callback) throws IOException {
        String filePath = callback.getFilePath();
        File file = new File(filePath);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            callback.onFail(new IOException("can not create directory"));
            return;
        }

        FileOutputStream fos = null;
        byte[] buf = new byte[2048];
        try {
            int len;
            long sum = 0;
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                callback.onProgress(sum, fileSize);
            }
            fos.flush();
            callback.onSuccess(file);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {}
            }
        }
    }

    public void syncCookieToWebview(String url) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        List<Cookie> cookies = defaultClient.cookieJar().loadForRequest(HttpUrl.parse(url));
        StringBuilder cookieString = new StringBuilder();
        boolean firstTime = true;
        for (Cookie cookie : cookies) {
            if (firstTime) {
                firstTime = false;
            } else {
                cookieString.append(",");
            }
            cookieString.append(cookie.toString());
        }
        CookieManager.getInstance().setCookie(url, cookieString.toString());
        CookieSyncManager.getInstance().sync();
    }

    public void clearCache() {
        try {
            defaultClient.cache().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
