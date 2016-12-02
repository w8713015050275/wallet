package com.letv.walletbiz.base.util;

import java.util.List;

/**
 * Created by changjiajie on 16-4-27.
 */
public class JsonUitls {

    public static String getJsonsString(List<JsonWrapper> jsons) {
        String jsonsStr = "[";
        if (jsons != null) {
            int size = jsons.size();
            for (int i = 0; i < size; i++) {
                JsonWrapper obj = jsons.get(i);
                if (i == (size - 1)) {
                    jsonsStr += obj.getJsonString();
                } else {
                    jsonsStr += (obj.getJsonString() + ",");
                }
            }
        }
        jsonsStr += "]";
        return jsonsStr;
    }
}
