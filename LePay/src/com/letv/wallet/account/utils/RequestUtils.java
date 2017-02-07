package com.letv.wallet.account.utils;

import android.text.TextUtils;

import com.letv.wallet.common.util.DigestUtils;

import org.xutils.common.util.KeyValue;

import java.util.List;

/**
 * Created by lijunying on 17-1-10.
 */

public class RequestUtils {
    private static final char HEX_DIGITS[] = "0123456789abcdef".toCharArray();

    /**
     * md5(url+请求参数+时间), 生成唯一请求码, 用于超时重试
     * @param bodyParams
     * @return
     */
    public static String getRequestNo(String url, List<KeyValue> bodyParams) {
        StringBuilder contentSb = new StringBuilder(url);
        if (bodyParams != null && bodyParams.size()>0) {
            for (KeyValue kv : bodyParams) {
                String name = kv.key;
                String value = kv.getValueStr();
                if (!TextUtils.isEmpty(name) && value != null) {
                    contentSb.append(name).append("=").append(value).append("&");
                }
            }
            if (contentSb.length() > 1) {
                return DigestUtils.getMd5_30(contentSb.append(System.currentTimeMillis()).toString());
            }
        }
        return null;
    }

}
