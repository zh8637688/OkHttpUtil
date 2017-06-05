package cz.okhttputil.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by haozhou on 2017/5/31.
 */
public class Request {
    public enum Method {
        GET, POST
    }
    Method method;
    String url;
    Map<String, String> params;
    boolean encodeParams;
    Map<String, String> header;
    int timeoutInSec;
    int retryCount;

    Request() {}

    public Method getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public boolean isEncodeParams() {
        return encodeParams;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public int getTimeoutInSec() {
        return timeoutInSec;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public static class Builder {
        Method method;
        String url;
        Map<String, String> params;
        boolean encodeParams;
        Map<String, String> header;
        int timeoutInSec;
        int retryCount;

        public Builder(String url) {
            this.url = url;
            this.params = null;
            this.encodeParams = false;
            this.header = null;
            this.timeoutInSec = -1;
            this.retryCount = -1;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder addParam(String key, String value) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put(key, value);
            return this;
        }

        public Builder params(Map<String, String> params) {
            this.params = params;
            this.encodeParams = false;
            return this;
        }

        public Builder params(Map<String, String> params, boolean encodeParams) {
            this.params = params;
            this.encodeParams = encodeParams;
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (header == null) {
                header = new HashMap<>();
            }
            header.put(key, value);
            return this;
        }

        public Builder header(Map<String, String> header) {
            this.header = header;
            return this;
        }

        public Builder timeoutInSec(int timeoutInSec) {
            this.timeoutInSec = timeoutInSec;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Request build() {
            Request request = new Request();
            request.method = method == null ? Method.GET : method;
            request.url = url;
            request.params = params;
            request.encodeParams = encodeParams;
            request.header = header;
            request.timeoutInSec = timeoutInSec;
            request.retryCount = retryCount;
            return request;
        }
    }
}
