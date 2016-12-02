package com.letv.walletbiz.base.util;

import com.google.gson.JsonObject;

/**
 * Created by changjiajie on 16-4-27.
 */
public class JsonObjectWrapper implements JsonStringInterface.JsonStatus {

    private static final String JSON_ERRNO = "errno";
    private static final String JSON_ERRMSG = "errmsg";

    protected JsonObject mObj;

    protected JsonObjectWrapper() {
        if (mObj == null) {
            mObj = new JsonObject();
        }
    }

    @Override
    public void setErrno(Number errno) {
        mObj.addProperty(JSON_ERRNO, errno);
    }

    @Override
    public void setErrmsg(String errmsg) {
        mObj.addProperty(JSON_ERRMSG, errmsg);
    }

    public String getJsonString() {
        return mObj == null ? null : mObj.toString();
    }

}
