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
import com.letv.walletbiz.member.beans.MemberTypeListBean;
import com.letv.walletbiz.member.beans.MemberTypeListBean.MemberTypeBean;
import com.letv.walletbiz.member.util.MemberCommonCallback;
import org.xutils.xmain;
import org.json.JSONObject;
import org.json.JSONArray;
import com.letv.wallet.common.util.ParseHelper;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberTypeListTask implements Runnable {

    private Context mContext;
    private MemberCommonCallback<MemberTypeBean[]> mCallback;

    private int mErrorCode = MemberCommonCallback.NO_ERROR;

    private static final int MSG_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (mCallback != null) {
                        MemberTypeListBean listBean = (MemberTypeListBean) msg.obj;
                        mCallback.onLoadFinished(listBean != null ? listBean.list : null, msg.arg1,true);
                    }
                    break;
            }
        }
    };

    public MemberTypeListTask(Context context, MemberCommonCallback<MemberTypeBean[]> callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void run() {
        MemberTypeListBean listBean = MemberHelper.getMemberTypeListFromDb(mContext);
        MemberTypeListBean newListBean = getMemberTypeListFromNetwork();
        if (listBean == null || !(listBean.equals(newListBean))) {
            if (newListBean != null) {
                sendMessage(MSG_LOAD_FINISHED,newListBean,mErrorCode);
                MemberHelper.syncMemberTypeListToDb(mContext, newListBean);
                return;
            }
        }
        sendMessage(MSG_LOAD_FINISHED,listBean, listBean != null ? MemberCommonCallback.NO_ERROR : mErrorCode);
    }

    private MemberTypeListBean getMemberTypeListFromNetwork() {

        BaseRequestParams params = new BaseRequestParams(MemberConstant.MEMBER_GET_MEMBER_TYPE_LIST);
        BaseResponse<String> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<String>>() {
            };
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }

        if (response == null) {
            return null;
        }

        TypeToken typeToken = new TypeToken<MemberTypeListBean>() {};
        MemberTypeListBean typeListBean = ParseHelper.parseByGson(response.data, typeToken.getType());
        try {
            JSONObject jsonObjSplit = new JSONObject(response.data);
            JSONArray ja = jsonObjSplit.getJSONArray("list");
            int length = ja.length();
            for (int i = 0; i < length; i++) {
                JSONObject jb = ja.getJSONObject(i);
                for (MemberTypeBean bean : typeListBean.list) {
                    if(bean.id.equals(jb.getString("id"))) {
                        bean.goods_json = jb.getString("goods");
                        continue;
                    }
                }
            }
        } catch (Throwable throwable) {
        }
        return typeListBean;
    }

    private void sendMessage(int what, MemberTypeListBean listBean, int errorCode) {
        Message msg = mHandler.obtainMessage(what);
        msg.obj = listBean;
        msg.arg1 = errorCode;
        mHandler.sendMessage(msg);
    }
}
