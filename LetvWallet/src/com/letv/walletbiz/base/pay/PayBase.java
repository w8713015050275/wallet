package com.letv.walletbiz.base.pay;

import android.content.Context;

/**
 * Created by linquan on 15-12-7.
 */
public abstract class PayBase {
    private static final String TAG = "PayBase";

    protected Callback mCallback;
    protected Context mContext;

    protected int mResult;
    protected String mResultInfo;
    protected String mResultMemo;

    public PayBase(Context context, String payInfo) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void doPay() {

        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                pay();
                boolean isFinish = mCallback.onPayResult(mResult, mResultMemo);

            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public boolean doCheckExist() {
        return checkExist();
    }

    protected abstract int pay();
    protected abstract boolean checkExist();

    public int getResult() {
        return mResult;
    }

    public String getResultInfo() {
        return mResultInfo;
    }

    public interface Callback {

        boolean onPayResult(int result, String status);
    }
}
