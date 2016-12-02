package com.letv.wallet.common.util;

import com.letv.wallet.common.http.beans.BaseResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.app.ResponseParser;
import org.xutils.http.request.UriRequest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by wyouflf on 15/11/5.
 */
public class JsonResponseParser implements ResponseParser {
    private static int mErrorCode = 0;
    private static String mErrorMessage = null;

    @Override
    public void checkResponse(UriRequest request) throws Throwable {
        // custom check ?
        // check header ?
    }

    /**
     * 转换result为resultType类型的对象
     *
     * @param resultType  返回值类型(可能带有泛型信息)
     * @param resultClass 返回值类型
     * @param result      字符串数据
     * @return
     * @throws Throwable
     */
    @Override
    public Object parse(Type resultType, Class<?> resultClass, String result) throws Throwable {
        // TODO: json to java bean
        Type dataType = ((ParameterizedType) resultType).getActualTypeArguments()[0];

        BaseResponse rspObj = new BaseResponse();
        String data = getResponseData(result, rspObj);
        if (data != null) {
            Type rawType = null;
            try {
                rawType = ((ParameterizedType) dataType).getRawType();
            } catch (Exception e) {
                LogHelper.w("[parse]  catch exception getMessage=" +
                        e.getMessage() + " toString()=" + e.toString());
            }


            if (rawType == List.class) {
                Type cType = ((ParameterizedType) dataType).getActualTypeArguments()[0];

                rspObj.data = ParseHelper.parseArrayByGson(data, cType);

            } else {
                if (dataType == String.class) {
                    rspObj.data = data;
                } else {
                    rspObj.data = ParseHelper.parseByGson(data, dataType);
                }
            }
        }
        return rspObj;
    }

    protected static String getResponseData(String respond, BaseResponse rsp) throws JSONException {
        JSONObject jObj = new JSONObject(respond);
        rsp.errno = jObj.getInt("errno");
        if (!jObj.isNull("errmsg")) {
            rsp.errmsg = jObj.getString("errmsg");
        }
        if (rsp.errno != 10000) {
            LogHelper.d("[respond] " + rsp.errno + " : " + rsp.errmsg + "\n" + respond);
        }

        if (!jObj.isNull("data")) {
            return jObj.getString("data");
        }
        return null;
    }

}
