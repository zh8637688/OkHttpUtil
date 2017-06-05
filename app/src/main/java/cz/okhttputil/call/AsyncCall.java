package cz.okhttputil.call;

import okhttp3.Call;

/**
 * Created by haozhou on 2017/5/27.
 */
public class AsyncCall implements IAsyncCall {
    private Call realCall;
    private volatile boolean isCompleted;

    public AsyncCall(Call call) {
        realCall = call;
        isCompleted = false;
    }

    @Override
    public void cancel() {
        if (realCall != null) {
            realCall.cancel();
        }
    }

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    public void completed() {
        isCompleted = true;
    }
}
