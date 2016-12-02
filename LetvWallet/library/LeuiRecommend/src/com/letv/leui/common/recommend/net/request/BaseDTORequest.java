package com.letv.leui.common.recommend.net.request;

import com.letv.leui.common.recommend.net.parse.DefaultParser;
import com.letv.leui.common.recommend.net.parse.IBaseParser;
import com.letv.leui.common.recommend.volley.NetworkResponse;
import com.letv.leui.common.recommend.volley.ParseError;
import com.letv.leui.common.recommend.volley.Request;
import com.letv.leui.common.recommend.volley.Response;
import com.letv.leui.common.recommend.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

/**
 * Created by dupengtao on 15-1-6.
 */
public class BaseDTORequest<T> extends Request<T> {

    private Class<T> mClazz;
    private IBaseParser mParser;
    private Response.Listener<T> mListener;

    public BaseDTORequest(String url, Class<T> clazz, Response.Listener<T> responseListener, Response.ErrorListener listener) {
        this(Method.GET, url, DefaultParser.getInstance(), clazz, responseListener, listener);
    }

    public BaseDTORequest(int method, String url, IBaseParser parser, Class<T> clazz, Response.Listener<T> responseListener, Response.ErrorListener listener) {
        super(method, url, listener);
        mParser = parser;
        mClazz = clazz;
        mListener = responseListener;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        T t;
        try {
            t = mParser.toParse(parsed, mClazz);
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
        if (t == null) {
            return Response.error(new ParseError(response));
        }
        return Response.success(t, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }
}
