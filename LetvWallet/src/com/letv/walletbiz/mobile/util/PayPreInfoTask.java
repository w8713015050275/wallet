package com.letv.walletbiz.mobile.util;

import android.content.Context;

import com.letv.wallet.common.util.CommonCallback;
import com.letv.walletbiz.base.util.PayHelper;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by changjiajie on 16-4-22.
 */
public class PayPreInfoTask implements Runnable {

    private Context mContext;
    private PayInfoCommonCallback<String> mCallback;

    private ProvidePayPreParams mProvider;

    private int mErrorCode = CommonCallback.NO_ERROR;

    public PayPreInfoTask(Context context, PayInfoCommonCallback<String> callback, ProvidePayPreParams provider) {
        mProvider = provider;
        mContext = context;
        mCallback = callback;
    }

    public void onCancelled() {
        mProvider = null;
    }


    public boolean isCancelled() {
        if (mProvider == null)
            return true;
        return false;
    }

    @Override
    public void run() {
        String payInfo = null;
        try {
            if (mProvider == null) return;
            payInfo = PayHelper.getPrepayInfo(mContext, mProvider.getRequestPath(),
                    mProvider.getRequestParams());
        } catch (Throwable throwable) {
            mErrorCode = CommonCallback.ERROR_NETWORK;
        }
        if (mCallback != null && !isCancelled()) {
            mCallback.onLoadPayInfoFinished(payInfo, mErrorCode);
            mProvider = null;
        }
    }

    public interface ProvidePayPreParams extends Serializable {
        String getRequestPath();

        Map<String, String> getRequestParams();
    }
}
