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
import com.letv.walletbiz.member.beans.BannerListBean;
import com.letv.walletbiz.member.beans.BannerListBean.BannerBean;
import com.letv.walletbiz.member.util.MemberCommonCallback;

import org.xutils.xmain;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberBannerListTask implements Runnable {

    private MemberCommonCallback<BannerBean[]> mCallback;
    private String mMemberType;
    private Context mContext;
    private int mErrorCode = MemberCommonCallback.NO_ERROR;
    private static final int MSG_LOAD_FINISHED = 1;
    private long mVersion = -2;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (mCallback != null) {
                        BannerListBean listBean = (BannerListBean) msg.obj;
                        long newVersion = listBean == null ? -1 : listBean.version;
                        boolean needUpdate = true;
                        if (mVersion == newVersion) {
                            needUpdate = false;
                        }
                        mCallback.onLoadFinished(listBean != null ? listBean.list : null, msg.arg1, needUpdate);
                        mVersion = newVersion;
                    }
                    break;
            }
        }
    };
    public MemberBannerListTask(Context context, String memberType, MemberCommonCallback<BannerBean[]> callback) {
        mContext = context;
        mMemberType = memberType;
        mCallback = callback;
    }

    @Override
    public void run() {
        BannerListBean listBean = MemberHelper.getBannerListFromDb(mContext,mMemberType);
        BannerListBean newListBean = getBannerListFromNetwork();
        if(listBean == null || (newListBean != null && (newListBean.version > listBean.version))) {
            if (newListBean != null) {
                sendMessage(MSG_LOAD_FINISHED,newListBean,mErrorCode);
                MemberHelper.syncBannerListToDb(mContext, newListBean,mMemberType);
                return;
            }
        }
        sendMessage(MSG_LOAD_FINISHED,listBean,listBean != null ? MemberCommonCallback.NO_ERROR : mErrorCode);
    }

    private BannerListBean getBannerListFromNetwork() {
        BaseRequestParams params = new BaseRequestParams(MemberConstant.MEMBER_GET_BANNER_LIST);
        params.addQueryStringParameter(MemberConstant.MEMBER_TYPE,mMemberType);

        BaseResponse<BannerListBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<BannerListBean>>() {
            };
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            mErrorCode = MemberCommonCallback.ERROR_NETWORK;
            response = null;
        }

        if (response != null) {
            return response.data;
        }

        return null;
    }

    private void sendMessage(int what, BannerListBean listBean, int erroCode) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = erroCode;
        mHandler.sendMessage(msg);
    }
}
