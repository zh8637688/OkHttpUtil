package cz.okhttputil.callback;

/**
 * Created by haozhou on 2017/5/31.
 */

public interface IRequestCallback<T> {
    void onSuccess(T response);
    void onFail(Exception e);
    void onNoNetwork();
}
