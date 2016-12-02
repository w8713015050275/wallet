package com.letv.leui.common.recommend.net;

import android.net.Uri;
import android.text.TextUtils;
import com.letv.leui.common.recommend.utils.LogHelper;

/**
 * Created by dupengtao on 2014/6/16.
 */
public class UriHelper {

    public static final String SERVER_HOST;
    public static final boolean IS_ONLINE = true;

    static {
        if (IS_ONLINE) {// 线上
            SERVER_HOST = "https://xsquare.scloud.letv.com";
        } else {// 线下
            SERVER_HOST = "http://10.154.156.118:5003";// 内网
        }
    }


    /**
     * about API reference
     * {@link http://wiki.letv.cn/pages/viewpage.action?pageId=34900187}
     *
     * @throws IllegalArgumentException
     */
    public static String getRecommendListUri(String tagid, String appid, String restype) throws IllegalArgumentException {
        Uri.Builder build = Uri.parse(SERVER_HOST).buildUpon();
        build.appendEncodedPath("api/v1/star");
        addMajorParam("tagid", tagid, build);
        addMajorParam("website", "2", build);
        addParam("appid", appid, build);
        addParam("restype", restype, build);
        addParam("mltag", Integer.toString(1), build);
        LogHelper.e("UriHelper", "url == " + build.toString());
        return build.toString();
    }

    private static Uri.Builder addMajorParam(String name, String value, Uri.Builder build) {
        if (TextUtils.isEmpty(value)) {
            throw new IllegalArgumentException("key [" + name + "] is null");
        } else {
            build.appendQueryParameter(name, value);
        }
        return build;
    }

    private static Uri.Builder addParam(String name, String value, Uri.Builder build) {
        if (!TextUtils.isEmpty(value)) {
            build.appendQueryParameter(name, value);
        }
        return build;
    }
}
