package com.letv.walletbiz.base.util;

import com.google.gson.JsonElement;

/**
 * Created by changjiajie on 16-4-27.
 */
public class JsonWrapper extends JsonObjectWrapper implements JsonStringInterface.JsonData {

    public JsonWrapper() {
        super();
    }

    @Override
    public void addData(String key, Object value) {
        if (mObj != null) {
            if (value instanceof String) {
                mObj.addProperty(key, (String) value);
            } else if (value instanceof Character) {
                mObj.addProperty(key, (Character) value);
            } else if (value instanceof Boolean) {
                mObj.addProperty(key, (Boolean) value);
            } else if (value instanceof Number) {
                mObj.addProperty(key, (Number) value);
            }
        }
    }

    @Override
    public void add(String key, JsonElement element) {
        if (mObj != null) {
            mObj.add(key, element);
        }
    }
}
