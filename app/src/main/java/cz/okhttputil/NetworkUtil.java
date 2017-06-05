package cz.okhttputil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 获取网络状态工具类
 * Created by haozhou on 2017/5/31.
 */
public class NetworkUtil {
    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            return  networkInfo != null && networkInfo.isAvailable();
        }
        return false;
    }
}
