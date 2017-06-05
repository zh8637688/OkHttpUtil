package cz.okhttputil.call;

/**
 * Created by haozhou on 2017/5/27.
 */

public interface IAsyncCall {
    /**
     * 取消该异步请求
     */
    void cancel();

    /**
     * 返回该异步请求是否执行完毕
     */
    boolean isCompleted();
}
