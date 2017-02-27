package com.letv.wallet.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by changjiajie on 17-3-8.
 */

public class UrlUtils {

    /**
     * 解析出url参数中的键值对
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> getUrlParams(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit = null;
        String strUrlParam = truncateUrl(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        //每个键值为一组
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    public static String getParams(String url, String key) {
        String content = null;
        Map<String, String> mapRequest = getUrlParams(url);
        if (mapRequest != null && mapRequest.containsKey(key)) {
            content = mapRequest.get(key);
        }
        return content;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String truncateUrl(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;
        strURL = strURL.trim();
        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }
        return strAllParam;
    }

}
