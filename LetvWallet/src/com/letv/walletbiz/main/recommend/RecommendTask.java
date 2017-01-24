package com.letv.walletbiz.main.recommend;

import android.content.Context;
import android.location.Address;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.walletbiz.main.recommend.bean.RecommendCardBean;

import java.util.List;

/**
 * Created by liuliang on 17-1-20.
 */

public class RecommendTask implements Runnable {

    private Context mContext;
    private Address mAddress;
    private int mOffset = 0;
    private int mLimit = RecommendConstant.LIMIT_DEFAULT;

    private int mErrorCode = CommonCallback.NO_ERROR;

    private CommonCallback<List<RecommendCardBean>> mCallback;

    public RecommendTask(Context context, CommonCallback<List<RecommendCardBean>> callback, Address address) {
        mContext = context;
        mCallback = callback;
        mAddress = address;
    }

    @Override
    public void run() {
        BaseResponse<List<RecommendCardBean>> response = RecommendHelper.getRecommendCardListFromNetWork(mContext, mOffset, mLimit, mAddress);
        if (response == null || response.errno != 10000) {
            mErrorCode = CommonCallback.ERROR_NETWORK;
        } else {
            mErrorCode = CommonCallback.NO_ERROR;
        }
        mCallback.onLoadFinished(response == null ? null : response.data, mErrorCode);
    }
}
