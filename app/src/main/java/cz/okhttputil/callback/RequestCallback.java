package cz.okhttputil.callback;

/**
 * Created by haozhou on 2017/5/31.
 */
public abstract class RequestCallback implements IRequestCallback<String> {
    public void onSuccess(String response) {}
    public void onFail(Exception e) {}
    public void onNoNetwork() {}
}
