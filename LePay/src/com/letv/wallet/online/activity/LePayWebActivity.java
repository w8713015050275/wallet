package com.letv.wallet.online.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.letv.lepaysdk.LePay;
import com.letv.wallet.common.activity.BaseWebViewActivity;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.UrlUtils;
import com.letv.wallet.online.LePayConstants;
import com.letv.wallet.online.bean.LePayCashierUrlBean;
import com.letv.wallet.online.bean.LePayChannelBean;
import com.letv.wallet.online.utils.LePayCashierUrlLoadTask;
import com.letv.wallet.online.utils.LePayOnlineCallback;
import com.letv.wallet.utils.SslUtil;

import java.net.URLDecoder;

/**
 * Created by changjiajie on 17-1-10.
 */

public class LePayWebActivity extends BaseWebViewActivity {

    private static final String TAG = LePayWebActivity.class.getSimpleName();
    private static final String PREFIX = "lepay://";
    private static final String PAYHOST = "pay";
    private static final String ACTIVEHOST = "active";
    // 记录激活状态
    private String mActivateStatus;
    // 激活后回调时携带参数，表示支付状态(分为可用和不可用)
    private String mPayStatus;
    // 记录回调时跳转类型
    private String mJumpType;
    private String mExternLePayInfo = null;
    private LePayChannelBean mChannelBean;

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJumpType = getIntent().getStringExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE);
        if (LePayConstants.JUMP_TYPE.ACTIVE.equals(mJumpType)) {
            mActivateStatus = LePayConstants.ACTIVE_STATUS.NO;
            mPayStatus = LePayConstants.PAY_STATUS.UNAVAILABLE;
        }
        mExternLePayInfo = getIntent().getStringExtra(LePayConstants.ApiIntentExtraKEY.LEPAY_INFO);
        mChannelBean = (LePayChannelBean) getIntent().getSerializableExtra(LePayConstants.ApiIntentExtraKEY.CHANNEL_DATA_KEY);
        overrideUrlLoading("lepay://");
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isFinishing()) return;
            if (!TextUtils.isEmpty(mJumpType)) {
                if (mJumpType.equals(LePayConstants.JUMP_TYPE.PAY)) {
                    // 回调支付结果
                    setResultPayFail();
                } else if (mJumpType.equals(LePayConstants.JUMP_TYPE.ACTIVE)) {
                    // 回调激活结果
                    setResultActive(mActivateStatus, mPayStatus);
                }
            }
        }
    };

    @Override
    protected View.OnClickListener getCloseButtonClickListener() {
        return mClickListener;
    }

    @Override
    protected boolean overrideUrlLoading(String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith(PREFIX)) {
            try {
                url = URLDecoder.decode(url, "utf-8");
                Uri uri = Uri.parse(url);
                if (uri != null) {
                    String host = uri.getHost();
                    if (!TextUtils.isEmpty(host)) {
                        if (PAYHOST.equals(host)) {
                            String info = UrlUtils.getParams(url, LePayConstants.ApiIntentExtraKEY.INFO);
                            if (!TextUtils.isEmpty(info)) {
                                setResultPaySuccess(info);
                                return true;
                            }
                        } else if (ACTIVEHOST.equals(host)) {
                            String activeStatus = UrlUtils.getParams(url, LePayConstants.ApiIntentExtraKEY.ACTIVE_STATUS);
                            if (!TextUtils.isEmpty(activeStatus)) {
                                if (LePayConstants.ACTIVE_STATUS.YES.equals(activeStatus)) {
                                    mActivateStatus = LePayConstants.ACTIVE_STATUS.YES;
                                    String usingState = SslUtil.getInstance().decryptData(
                                            UrlUtils.getParams(url, LePayConstants.ApiIntentExtraKEY.PAYSTATUS));
                                    if (!TextUtils.isEmpty(usingState)) {
                                        if (LePayConstants.PAY_STATUS.AVAILABLE.equals(usingState)) {
                                            mPayStatus = LePayConstants.PAY_STATUS.AVAILABLE;
                                            if (mChannelBean != null) {
                                                getCashierUrl(mExternLePayInfo, mChannelBean.getName());
                                            } else {
                                                LogHelper.e("[%S] 激活成功 mChannelBean == null", TAG);
                                            }
                                        } else {
                                            setResultActiveSuccess(LePayConstants.PAY_STATUS.UNAVAILABLE);
                                        }
                                        return true;
                                    }
                                } else if (LePayConstants.ACTIVE_STATUS.NO.equals(activeStatus)) {
                                    setResultActiveFail();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.overrideUrlLoading(url);
    }

    private LePayCashierUrlLoadTask mCashierUrlTask;

    private void getCashierUrl(String payInfo, String name) {
        if (!TextUtils.isEmpty(name)) {
            if (mCashierUrlTask == null) {
                mCashierUrlTask = new LePayCashierUrlLoadTask(new CashierUrlCallback(), payInfo, name);
            }
            showLoadingView();
            ExecutorHelper.getExecutor().runnableExecutor(mCashierUrlTask);
            return;
        }
        LogHelper.e("[%S] name == null", TAG);
    }

    private void goCashier(String url) {
        if (TextUtils.isEmpty(url)) {
            LogHelper.e("[%S] cashierUrl == null", TAG);
            return;
        }
        // 标记为支付
        mJumpType = LePayConstants.JUMP_TYPE.PAY;
        // 加载出支付页面
        mWebView.loadUrl(url, getAdditionalHttpHeaders());
        setTitle(mWebView.getTitle());
    }

    private void setResultPaySuccess(String info) {
        Intent intent = new Intent();
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE, LePayConstants.JUMP_TYPE.PAY);
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.INFO, info);
        setResult(intent);
    }

    private void setResultPayFail() {
        Intent intent = new Intent();
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE, LePayConstants.JUMP_TYPE.PAY);
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.PAY_STATUS, LePayConstants.PAY_STATUS.FAIL);
        setResult(intent);
    }

    private void setResultActive(String status, String payStatus) {
        Intent intent = new Intent();
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE, LePayConstants.JUMP_TYPE.ACTIVE);
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.ACTIVE_STATUS, status);
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.PAYSTATUS, payStatus);
        setResult(intent);
    }

    private void setResultActiveSuccess(String payStatus) {
        Intent intent = new Intent();
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE, LePayConstants.JUMP_TYPE.ACTIVE);
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.ACTIVE_STATUS, LePayConstants.ACTIVE_STATUS.YES);
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.PAYSTATUS, payStatus);
        setResult(intent);
    }

    private void setResultActiveFail() {
        Intent intent = new Intent();
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE, LePayConstants.JUMP_TYPE.ACTIVE);
        intent.putExtra(LePayConstants.ApiIntentExtraKEY.ACTIVE_STATUS, LePayConstants.ACTIVE_STATUS.NO);
        setResult(intent);
    }

    private void setResult(Intent data) {
        setResult(RESULT_OK, data);
        finish();
    }

    protected class CashierUrlCallback implements LePayOnlineCallback {

        @Override
        public void onSuccess(Object result) {
            if (isFinishing()) return;
            hideLoadingView();
            BaseResponse<LePayCashierUrlBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayCashierUrlBean>) result;
            }
            if (response != null) {
                LePayCashierUrlBean urlBean = response.data;
                if (urlBean == null) {
                    LogHelper.e("[%S] cashier response.data == null", TAG);
                    // 获取支付地址失败时,回调为激活成功,传回可支付状态
                    setResultActiveSuccess(mPayStatus);
                    return;
                }
                goCashier(urlBean.getCashierUrl());
            }
        }

        @Override
        public void onError(Object result, int errorCode) {
            if (isFinishing()) return;
            hideLoadingView();
            // 获取支付地址失败时,回调为激活成功,传回可支付状态
            setResultActiveSuccess(mPayStatus);
        }
    }
}
