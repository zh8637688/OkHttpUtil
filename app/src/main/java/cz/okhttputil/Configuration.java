package cz.okhttputil;

import android.content.Context;

import java.io.File;

/**
 * Created by haozhou on 2017/5/27.
 */
public class Configuration {
    private int retryCount;
    private int timeoutInSec;
    private String cachePath;
    private long cacheSize;
    private boolean useProxy;
    private String proxyHost;
    private int proxyPort;
    private String userAgent;

    private Configuration() {

    }

    int getRetryCount() {
        return retryCount;
    }

    int getTimeoutInSec() {
        return timeoutInSec;
    }

    String getCachePath() {
        return cachePath;
    }

    long getCacheSize() {
        return cacheSize;
    }

    boolean useProxy() {
        return useProxy;
    }

    String getProxyHost() {
        return proxyHost;
    }

    int getProxyPort() {
        return proxyPort;
    }

    String getUserAgent() {
        return userAgent;
    }

    public static class Builder {
        private int retryCount;
        private int timeout;
        private String cachePath;
        private long cacheSize;
        private boolean useProxy;
        private String proxyHost;
        private int proxyPort;
        private String userAgent;

        public Builder(Context context) {
            this.retryCount = 0;
            this.timeout = 30;
            this.cachePath = context.getCacheDir().getAbsolutePath() + File.separator + "request";
            this.cacheSize = 1024 * 1024 * 50;
            this.proxyHost = null;
            this.proxyPort = 0;
            this.userAgent = null;
        }

        public Builder setRetryCount(int retryCount) {
            this.retryCount = retryCount >= 0 ? retryCount : 0;
            return this;
        }

        public Builder setTimeoutInSec(int timeout) {
            this.timeout = timeout >= 0 ? timeout : 0;
            return this;
        }

        public Builder setCache(String cachePath, long cacheSize) {
            this.cachePath = cachePath;
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder setProxy(String proxyHost, int proxyPort) {
            this.useProxy = true;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            return this;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Configuration build() {
            Configuration config = new Configuration();
            config.retryCount = retryCount;
            config.timeoutInSec = timeout;
            config.cachePath = cachePath;
            config.cacheSize = cacheSize;
            config.useProxy = useProxy;
            config.proxyHost = proxyHost;
            config.proxyPort = proxyPort;
            config.userAgent = userAgent;
            return config;
        }
    }
}
