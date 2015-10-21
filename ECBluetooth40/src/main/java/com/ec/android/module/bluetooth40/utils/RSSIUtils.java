package com.ec.android.module.bluetooth40.utils;

import android.util.Log;

/**
 * RSSI信号强度范围 工具类
 *
 * @author ZLJ
 */
public class RSSIUtils {

    private static final String TAG = RSSIUtils.class.getSimpleName();

    /**
     * 获取rssi等级
     *
     * @param rssi 注意rssi值是负的
     * @return
     */
    public static int getLevel(int rssi) {
        if (rssi > 0) {
            //根本没有大于0的rssi，此处防止出错
            Log.w(TAG, "根本没有大于0的rssi，此处防止出错");
            return 1;
        }
        //
        if (rssi >= -40) {
            //大于-40为优
            return 1;
        } else if (rssi >= -60 && rssi < -40) {
            //大于-60且小于-40为良
            return 2;
        } else if (rssi >= -80 && rssi < -60) {
            //大于-80且小于-60为中
            return 3;
        } else if (rssi < -80) {
            //小于-80为差
            return 4;
        }
        //
        return 1;
    }

}
