package cz.okhttputil.callback;

import java.io.File;

/**
 * Created by haozhou on 2017/6/1.
 */

public abstract class DownloadCallback implements IRequestCallback<File> {
    @Override
    public void onSuccess(File response) {}

    @Override
    public void onFail(Exception e) {}

    @Override
    public void onNoNetwork() {}

    public void onProgress(long finishSize, long fileSize) {}

    public abstract String getFilePath();
}
