package com.letv.wallet.common.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linquan on 15-11-30.
 */
public class ParseHelper {
    private final static String TAG = "ParseHelper";

    private static Gson mGson = new Gson();

    public static <T> T parseByGson(String respond, Class<T> cls) {
        T bean = null;
        try {
            if (!TextUtils.isEmpty(respond)) {
                bean = mGson.fromJson(respond, cls);
            }
        } catch (Exception e) {
            LogHelper.w("[%s] analysisRespond catch exception getMessage=" +
                    e.getMessage() + " toString()=" + e.toString(), TAG);
            bean = null;
        }
        return bean;
    }

    public static <T> T parseByGson(String respond, Type typeOfT) {
        T bean = null;
        try {
            if (!TextUtils.isEmpty(respond)) {
                bean = mGson.fromJson(respond, typeOfT);
            }
        } catch (Exception e) {
            LogHelper.w("[%s] analysisRespond catch exception getMessage=" +
                    e.getMessage() + " toString()=" + e.toString(), TAG);
            bean = null;
        }
        return bean;
    }

    public static <T> List<T> parseArrayByGson(String respond, Type typeOfT) {
        List<T> list = new ArrayList<T>();
        try {
            JsonParser parser = new JsonParser();
            JsonArray jsonArray  = parser.parse(respond).getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement el = jsonArray.get(i);
                T tmp = mGson.fromJson(el, typeOfT);
                list.add(tmp);
            }

        } catch (Exception e) {
            LogHelper.w("[%s] analysisRespond catch exception getMessage=" +
                    e.getMessage() + " toString()=" + e.toString(), TAG);
            list = null;
        }

        return list;
    }

}
