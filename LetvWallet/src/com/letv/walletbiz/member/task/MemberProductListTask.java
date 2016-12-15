package com.letv.walletbiz.member.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.MemberHelper;
import com.letv.walletbiz.member.beans.ProductListBean;
import com.letv.walletbiz.member.beans.ProductListBean.ProductBean;
import com.letv.walletbiz.member.util.MemberCommonCallback;
import org.xutils.xmain;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberProductListTask implements Runnable {

    private Context mContext;
    private String mMemberType;
    private String mToken;
    private MemberCommonCallback<ProductBean[]> mCallback;
    private ProductListBean mListBean;

    private int mErrorCode = MemberCommonCallback.NO_ERROR;

    private static final int MSG_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (mCallback != null) {
                        ProductListBean listBean = (ProductListBean) msg.obj;
                        boolean needUpdate = true;
                        if (((mListBean != null && mListBean.equals(listBean))
                                || (listBean != null && listBean.equals(mListBean)))) {
                            needUpdate = false;
                        }
                        mCallback.onLoadFinished(listBean != null ? listBean.list : null, msg.arg1,needUpdate);
                        mListBean = listBean;
                    }
                    break;
            }
        }
    };

    public MemberProductListTask(Context context, String memberType, String token, MemberCommonCallback<ProductBean[]> callback) {
        mContext = context;
        mMemberType = memberType;
        mToken = token;
        mCallback = callback;
    }

    @Override
    public void run() {
        ProductListBean listBean = MemberHelper.getProductListFromDb(mContext,mMemberType);
        ProductListBean newListBean = getProductListFromNetwork();
        if (listBean == null || !(listBean.equals(newListBean))) {
            if (newListBean != null) {
                sendMessage(MSG_LOAD_FINISHED,newListBean,mErrorCode);
                MemberHelper.syncProductListToDb(mContext, newListBean,mMemberType);
                return;
            }
        }
        sendMessage(MSG_LOAD_FINISHED,listBean,listBean != null ? MemberCommonCallback.NO_ERROR : mErrorCode);
    }

    private ProductListBean getProductListFromNetwork() {
        BaseRequestParams params = new BaseRequestParams(MemberConstant.MEMBER_GET_PRODUCT_LIST);
        params.addQueryStringParameter(MemberConstant.MEMBER_TOKEN,mToken);
        params.addQueryStringParameter(MemberConstant.MEMBER_SPU_ID, mMemberType);

        BaseResponse<ProductListBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<ProductListBean>>() {
            };
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MemberCommonCallback.ERROR_NETWORK;
            response = null;
        }

        if (response == null) {
            return null;
        }
        return response.data;
    }

    private void sendMessage(int what, ProductListBean listBean, int erroCode) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = erroCode;
        mHandler.sendMessage(msg);
    }
}
