package com.letv.walletbiz.base.util;

import com.google.gson.JsonElement;

/**
 * Created by changjiajie on 16-4-27.
 */
public class JsonStringInterface {

    public interface JsonStatus {
        void setErrno(Number errno);

        void setErrmsg(String errmsg);
    }

    public interface JsonData {
        void addData(String key, Object value);

        void add(String key, JsonElement element);
    }
}
