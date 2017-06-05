package cz.okhttputil.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 强制重试
 */
public class RetryInterceptor implements Interceptor {
    private int retryCount;

    public RetryInterceptor(int retryCount) {
        this.retryCount = retryCount > 0 ? retryCount : 0;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        for (int i = 0; !response.isSuccessful() && i < retryCount; i++) {
            response = chain.proceed(request);
        }
        return response;
    }
}
