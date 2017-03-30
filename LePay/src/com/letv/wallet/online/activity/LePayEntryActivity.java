package com.letv.wallet.online.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.lepaysdk.Constants;
import com.letv.lepaysdk.ELePayState;
import com.letv.lepaysdk.LePay;
import com.letv.lepaysdk.LePayApi;
import com.letv.lepaysdk.wxpay.WXPay;
import com.letv.shared.widget.LeBottomSheet;
import com.letv.shared.widget.LeLoadingView;
import com.letv.tracker2.enums.EventType;
import com.letv.tracker2.enums.Key;
import com.letv.wallet.R;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.wallet.online.LePayConstants;
import com.letv.wallet.online.bean.LePayCashierUrlBean;
import com.letv.wallet.online.bean.LePayChannelBean;
import com.letv.wallet.online.bean.LePayChannelListBean;
import com.letv.wallet.online.bean.LePayOrderStatusBean;
import com.letv.wallet.online.ui.LePayChannelListAdapter;
import com.letv.wallet.online.utils.LePayCashierUrlLoadTask;
import com.letv.wallet.online.utils.LePayChannelListLoadTask;
import com.letv.wallet.online.utils.LePayOnlineCallback;
import com.letv.wallet.online.utils.LePayOrderStatusTask;
import com.letv.wallet.utils.SslUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LePayEntryActivity extends BaseFragmentActivity implements View.OnClickListener, AccountHelper.OnAccountChangedListener {

    private static final String TAG = LePayEntryActivity.class.getSimpleName();
    private String mExternLePayInfo = null;
    private String mOrderNo = null;
    private static final int WEB_FORRESULT = 1;
    private int mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
    private LeBottomSheet mPayPageDialog;
    private LeBottomSheet mNetworkDialog;
    private LeBottomSheet mPayFailDialog;
    private LeBottomSheet mSelectStatusDialog;
    private int mDialogTitleId = -1;

    private TextView mPriceTv;
    private View mPayPageView;
    private LePayChannelListLoadTask mPayChannelTask;
    private LePayCashierUrlLoadTask mCashierUrlTask;
    private LePayOrderStatusTask mOrderStatusTask;
    private LePayChannelListBean mLePayChannelListBean;
    private LePayChannelBean mCurrentChannelBean;

    private RecyclerView mChannelRecyclerV;
    private LeLoadingView mLoadingV;
    private LinearLayoutManager mLinearLayoutManager;
    private LePayChannelListAdapter mChannelListAdapter;

    private boolean isGoActivition, isCheckOrderStatus, isResultPayFail, isShowPayFail, isClickReGoPay;
    private boolean isFirst = true, isFirstShow = true;
    private String mFrom = LePayConstants.PAY_FROM.OTHER;

    private int lastActiveStatus = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        setContentView(R.layout.lepay_entry_activity_v);
        mPayPageView = View.inflate(getApplicationContext(), R.layout.lepay_channel_view, null);
    }

    private boolean checkData(boolean showDialog) {
        if (!checkAppNetWork(showDialog)) {
            return false;
        }
        if (isGoActivition) {
            isGoActivition = false;
            showBottomLoadingView();
            // 去激活回调为支付失败时网络错误或者禁网导致数据无法更新,此时更新数据并显示支付失败弹框
            if (isResultPayFail) {
                isResultPayFail = false;
                updatePayChannelData(false, false);
                showPayFailDialog();
                return false;
            }
            // 去激活回调时网络错误或者禁网导致数据无法更新，此时更新数据并显示支付弹框
            updatePayChannelData(false, true);
            return false;
        }
        // 是否在查询订单状态
        if (isCheckOrderStatus) {
            return false;
        }
        if (isShowPayFail) {
            return false;
        }
        return true;
    }

    /**
     * 检测网络状态，传入true并且返回false时会有相应网络弹框
     *
     * @param showDialog
     * @return
     */
    private boolean checkAppNetWork(boolean showDialog) {
        int uid = getUid();
        if (!NetworkHelper.isNetworkAvailable()) {
            //未开启移动网络
            if (!NetworkHelper.isDataNetworkAvailable()) {
                if (showDialog) {
                    showUserSelectDialog(R.string.pay_no_network);
                }
                return false;
            }
            //开启了移动网络并且被禁用
            if (!NetworkHelper.isEnableMobileNetwork(uid)) {
                if (showDialog) {
                    showUserSelectDialog(R.string.pay_network_error);
                }
                return false;
            }
        }
        //链接wifi并且wifi被禁用
        if (NetworkHelper.isWifiAvailable() && !NetworkHelper.isEnableWifi(uid)) {
            //未开启移动网络
            if (showDialog) {
                showUserSelectDialog(R.string.pay_network_error);
            }
            return false;
        }
        mDialogTitleId = -1;
        if (mNetworkDialog != null && mNetworkDialog.isShowing()) {
            mNetworkDialog.dismiss();
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkData(true) && isFirst) {
            isFirst = false;
            getExternExtra();
            if (TextUtils.isEmpty(mExternLePayInfo)) {
                setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                return;
            }
            init();
        }
    }

    @Override
    public boolean hasToolbar() {
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogHelper.e("[%S] %s", TAG, "onConfigurationChanged");
        updateUI();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == WEB_FORRESULT) {
                if (data != null) {
                    String jumpType = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE);
                    if (jumpType.equals(LePayConstants.JUMP_TYPE.PAY)) {
                        payResult(data);
                        return;
                    }
                    if (jumpType.equals(LePayConstants.JUMP_TYPE.ACTIVE)) {
                        activeResult(data);
                        return;
                    }
                }
            }
        }
        // 回调异常，显示支付页,防止出现空白页面
        showPayPage();
    }

    private void activeResult(Intent data) {
        //检测网络状态：
        //      1>网络异常弹出相应网络异常的弹框，在onStart中会更新支付列表数据并显示支付弹框
        //      2>网络状态正常,判断支付是否可用:
        //              (1)可用，更新支付列表数据防止支付失败后重新支付时数据出错
        //              (2)不可用或其他异常情况，更新支付列表数据，显示支付弹框
        if (checkAppNetWork(true)) {
            // 如果为true,onStart中需要更新列表数据
            isGoActivition = false;
            String activeStatus = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.ACTIVE_STATUS);
            String payStatus = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.PAYSTATUS);
            if (!TextUtils.isEmpty(activeStatus)) {
                if (LePayConstants.ACTIVE_STATUS.YES.equals(activeStatus)) {
                    String decryptUsingStatus = SslUtil.getInstance().decryptData(payStatus);
                    if (!TextUtils.isEmpty(decryptUsingStatus)
                            && LePayConstants.PAY_STATUS.AVAILABLE.equals(decryptUsingStatus)) {
                        // 如果支付为可用状态获取支付地址并调起对应的支付
                        if (mCurrentChannelBean != null) {
                            // 更新支付列表数据
                            showBottomLoadingView();
                            updatePayChannelData(false, false);
                            getCashierUrl(mCurrentChannelBean.getName());
                            return;
                        }
                    }
                }
            }
            // 回传数据异常 或激活失败，显示支付列表
            showBottomLoadingView();
            updatePayChannelData(false, true);
        }
    }

    private void payResult(Intent data) {
        //1.数据正常
        //      1>支付成功，把isGoActivition置为false,查询订单状态后会显示是否已支付弹框
        //      2>支付失败或者状态异常检测网络状态：
        //              (1)网络异常弹出相应网络异常的弹框，并记录isResultPayFail为true,
        //                  onStart判断(isGoActivition==true&&isResultPayFail==true)更新支付列表数据并显示支付失败弹框
        //              (2)网络状态正常,把isGoActivition置为false,更新支付列表数据，弹出支付失败弹框
        //2.数据异常 检测网络状态：
        //      1>网络异常弹出相应网络异常的弹框，在onStart中会更新支付列表数据并显示支付弹框
        //      2>网络状态正常,更新支付列表数据显示支付列表
        String info = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.INFO);
        if (!TextUtils.isEmpty(info)) {
            String decryptInfo = SslUtil.getInstance().decryptData(info);
            if (!TextUtils.isEmpty(decryptInfo)) {
                String[] strs = decryptInfo.split("\\^");
                if (strs != null && strs.length == 2) {
                    String order_no = strs[0].trim();
                    String pay_status = strs[1].trim();
                    if (LePayConstants.PAY_STATUS.SUCCESS.equals(pay_status)) {
                        if (!TextUtils.isEmpty(order_no)) {
                            // 查询订单状态
                            isCheckOrderStatus = true;
                            if (isGoActivition) {
                                isGoActivition = false;
                            }
                            loadOrderStatus(order_no);
                            return;
                        } else {
                            Map<String, Object> props = new HashMap<String, Object>();
                            props.put(Key.Content.getKeyId(), "orderID");
                            Action.uploadCustom(EventType.Exception.getEventId(), Action.PAY_RESULT_EXCEPTION, props);
                        }
                    } else if (LePayConstants.PAY_STATUS.FAIL.equals(pay_status)) {
                        // 支付失败
                    } else {
                        Map<String, Object> props = new HashMap<String, Object>();
                        props.put(Key.Content.getKeyId(), "paymentStatus");
                        Action.uploadCustom(EventType.Exception.getEventId(), Action.PAY_RESULT_EXCEPTION, props);
                    }
                    if (checkAppNetWork(true)) {
                        //如果是去激活，回来刷新数据
                        if (isGoActivition) {
                            isGoActivition = false;
                            showBottomLoadingView();
                            updatePayChannelData(false, false);
                        }
                        showPayFailDialog();
                        return;
                    }
                    // 网络异常或禁网导致的无法更新，记录变量
                    isResultPayFail = true;
                    return;
                }
            }
        } else {
            String status = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.PAY_STATUS);
            if (!TextUtils.isEmpty(status)) {
                if (LePayConstants.PAY_STATUS.FAIL.equals(status)) {
                    if (checkAppNetWork(true)) {
                        //如果是去激活，回来刷新数据
                        if (isGoActivition) {
                            isGoActivition = false;
                            showBottomLoadingView();
                            // 更新数据不弹出支付弹框
                            updatePayChannelData(false, false);
                        }
                        showPayFailDialog();
                        return;
                    }
                    // 网络异常或禁网导致的无法更新，记录变量
                    isResultPayFail = true;
                    return;
                }
            }
        }
        // 回调数据异常
        if (checkAppNetWork(true)) {
            if (isGoActivition) {
                isGoActivition = false;
            }
            // 更新列表数据显示支付列表
            showBottomLoadingView();
            updatePayChannelData(false, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideBottomLoadingView();
        hideLoadingView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        if (mNetworkDialog != null) {
            mNetworkDialog.dismiss();
        }
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public void onAccountLogin() {

    }

    @Override
    public void onAccountLogout() {
        // 退出账户 关闭支付,回调给业务方支付失败
        if (isFinishing()) return;
        setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if (checkData(false)) {
                if (mLePayChannelListBean == null) {
                    loadPayChannel();
                } else {
                    showPayPage();
                }
            }
        }
    }

    // ----- go pay ----

    private void reGoPay() {
        showPayPage();
    }

    private void goPay(LePayChannelBean channelBean) {
        if (channelBean != null) {
            mCurrentChannelBean = channelBean;
            // 支付时统计选择的支付渠道
            Map<String, Object> props = new HashMap<String, Object>();
            props.put(Key.Content.getKeyId(), channelBean.getName());
            Action.uploadClick(Action.PAY_PAGE_PAY_CLICK, props);
            switch (channelBean.getSourcing()) {
                case LePayConstants.PAY_CHANNEL.BOSS_PAY:
                    startBossPay(channelBean, mExternLePayInfo);
                    break;
                case LePayConstants.PAY_CHANNEL.FINANCE_PAY:
                    loadCashierUrlChannel(channelBean);
                    break;
            }
            return;
        }
        LogHelper.e("[%S] channelBean == null", TAG);
    }

    private void goActivation(LePayChannelBean channelBean) {
        String url = channelBean.getActiveLink();
        if (TextUtils.isEmpty(url)) {
            LogHelper.e("[%S] goActivation == null", TAG);
            return;
        }
        try {
            Intent intent = new Intent(LePayEntryActivity.this, LePayWebActivity.class);
            intent.putExtra(LePayConstants.ApiIntentExtraKEY.LEPAY_INFO, this.mExternLePayInfo);
            intent.putExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE, LePayConstants.JUMP_TYPE.ACTIVE);
            intent.putExtra(CommonConstants.EXTRA_URL, url);
            intent.putExtra(LePayConstants.ApiIntentExtraKEY.CHANNEL_DATA_KEY, channelBean);
            startActivityForResult(intent, WEB_FORRESULT);
            isGoActivition = true;
            LogHelper.d("[%S] start goActivation", TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goCashier(String url) {
        if (TextUtils.isEmpty(url)) {
            LogHelper.e("[%S] cashierUrl == null", TAG);
            return;
        }
        try {
            Intent intent = new Intent(LePayEntryActivity.this, LePayWebActivity.class);
            intent.putExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE, LePayConstants.JUMP_TYPE.PAY);
            intent.putExtra(CommonConstants.EXTRA_URL, url);
            startActivityForResult(intent, WEB_FORRESULT);
            LogHelper.d("[%S] start finance pay", TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBossPay(LePayChannelBean channelBean, String payInfo) {
        if (channelBean.getActive() == LePayConstants.PAY_ACTIVE.AVAILABLE) {
            int channelId = channelBean.getChannelId();
            String channelStr = Constants.ILepayChannel.alipay_channelId;
            if (channelId == LePayConstants.PAY_CHANNEL.CHANNEL_ALIPAY) {
                channelStr = Constants.ILepayChannel.alipay_channelId;
                showBottomLoadingView();
            } else if (channelId == LePayConstants.PAY_CHANNEL.CHANNEL_WXPAY) {
                channelStr = Constants.ILepayChannel.wx_channelId;
                if (WXPay.getInstance(this).isWXAppInstalled()
                        && WXPay.getInstance(this).isSupportWXPay()) {
                    showBottomLoadingView();
                }
            }
            LogHelper.d("[%S] start boss pay", TAG);
            LePayApi.createGetdirectpay(this, channelStr, payInfo, new LePay.ILePayCallback() {

                @Override
                public void payResult(ELePayState eLePayState, String s) {
                    if (isFinishing()) return;
                    mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
                    if (eLePayState != null) {
                        if (ELePayState.OK.equals(eLePayState)) {
                            mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_SUCCESSED;
                            setReturnResult(mPayReturnResult);
                        } else if (ELePayState.FAILT.equals(eLePayState)) {
                            mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
                        } else if (ELePayState.CANCEL.equals(eLePayState)) {
                            mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_CANCLE;
                        } else if (ELePayState.NONETWORK.equals(eLePayState)) {
                        }
                        if (eLePayState != ELePayState.OK) {
                            hideBottomLoadingView();
                            showPayFailDialog();
                            LogHelper.e("[%S] %s", TAG, "ELePayState == " + mPayReturnResult);
                        }
                    }
                }
            });
        }
    }

    public void setReturnResult(int result) {
        if (result == LePayConstants.PAY_RETURN_RESULT.PAY_CANCLE) {
            // 统计支付取消
            Action.uploadCustom(EventType.Close, Action.PAY_PAGE_CLOSE);
        }
        Intent intent = new Intent();
        intent.putExtra(LePayConstants.ApiReqeustKey.PAY_RETURN_RESULT, result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pay_cancel_tv:
                setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_CANCLE);
                break;
        }
    }

    // ------ request data ---------------
    private void updatePayChannelData(boolean isResult, boolean showPayPage) {
        if (mPayChannelTask == null) {
            mPayChannelTask = new LePayChannelListLoadTask(this.mExternLePayInfo);
        }
        mPayChannelTask.setCallback(new ChannelListCallback(isResult, showPayPage));
        ExecutorHelper.getExecutor().runnableExecutor(mPayChannelTask);
    }

    private void loadPayChannel() {
        if (mPayChannelTask == null) {
            mPayChannelTask = new LePayChannelListLoadTask(this.mExternLePayInfo);
        }
        mPayChannelTask.setCallback(new ChannelListCallback(true, true));
        ExecutorHelper.getExecutor().runnableExecutor(mPayChannelTask);
    }

    protected class ChannelListCallback implements LePayOnlineCallback {

        private boolean mReturn = false;
        private boolean mShowPayPage = false;

        public ChannelListCallback(boolean isReturn, boolean showPayPage) {
            this.mReturn = isReturn;
            this.mShowPayPage = showPayPage;
        }

        @Override
        public void onSuccess(Object result) {
            if (isFinishing()) return;
            hideBottomLoadingView();
            BaseResponse<LePayChannelListBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayChannelListBean>) result;
            }
            updateData(response, mReturn);
            updateChannelUI(mShowPayPage);
        }

        @Override
        public void onError(Object result, int errorCode) {
            if (isFinishing()) return;
            hideBottomLoadingView();
            BaseResponse<LePayChannelListBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayChannelListBean>) result;
            }
            if (errorCode == LePayOnlineCallback.ERROR_NETWORK
                    || errorCode == LePayOnlineCallback.ERROR_DATA) {
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                if (this.mReturn && mLePayChannelListBean == null) {
                    setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                }
                return;
            }
            if (errorCode == LePayOnlineCallback.ERROR_OTHER) {
                if (response == null) {
                    Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                    //未知错误
                    if (this.mReturn && mLePayChannelListBean == null) {
                        setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                    }
                    return;
                }
                if (response.errno == LePayConstants.ERRNO.ERRNO_USER
                        || response.errno == LePayConstants.ERRNO.ERRNO_USER_AUTH_FAILED) {
                    Toast.makeText(BaseApplication.getApplication(), response.errmsg, Toast.LENGTH_SHORT).show();
                }
                if (this.mReturn) {
                    setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                }
            }
        }
    }

    protected class ChannelItemOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag != null) {
                goPay((LePayChannelBean) tag);
            }
        }
    }

    private void loadCashierUrlChannel(LePayChannelBean channelBean) {
        switch (channelBean.getActive()) {
            case LePayConstants.PAY_ACTIVE.NOTACTIVATION:
                // 未激活
                goActivation(channelBean);
                break;
            case LePayConstants.PAY_ACTIVE.INSUFFICIENT:
                // 余额不足
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_payment_money_insufficient, Toast.LENGTH_SHORT).show();
                break;
            case LePayConstants.PAY_ACTIVE.UNAVAILABLE:
                // 不可用 包括（被冻结/未激活/不可再申请/激活申请中）
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_payment_unavailable, Toast.LENGTH_SHORT).show();
                break;
            case LePayConstants.PAY_ACTIVE.AVAILABLE:
                getCashierUrl(channelBean.getName());
                break;
        }
    }

    private void getCashierUrl(String name) {
        if (!TextUtils.isEmpty(name)) {
            if (mCashierUrlTask == null) {
                mCashierUrlTask = new LePayCashierUrlLoadTask(new CashierUrlCallback(), this.mExternLePayInfo, name);
            }
            showBottomLoadingView();
            ExecutorHelper.getExecutor().runnableExecutor(mCashierUrlTask);
        }
    }

    protected class CashierUrlCallback implements LePayOnlineCallback {

        @Override
        public void onSuccess(Object result) {
            if (isFinishing()) return;
            hideBottomLoadingView();
            BaseResponse<LePayCashierUrlBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayCashierUrlBean>) result;
            }
            if (response != null) {
                LePayCashierUrlBean urlBean = response.data;
                if (urlBean == null) {
                    LogHelper.e("[%S] cashier response.data == null", TAG);
                    return;
                }
                goCashier(urlBean.getCashierUrl());
            }
        }

        @Override
        public void onError(Object result, int errorCode) {
            if (isFinishing()) return;
            hideBottomLoadingView();
            BaseResponse<LePayCashierUrlBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayCashierUrlBean>) result;
            }
            if (errorCode == LePayOnlineCallback.ERROR_DATA
                    || errorCode == LePayOnlineCallback.ERROR_NETWORK) {
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (errorCode == LePayOnlineCallback.ERROR_OTHER) {
                if (response == null) {
                    Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                    //未知错误
                    if (mLePayChannelListBean == null) {
                        setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                    }
                    return;
                }
                if (response.errno == LePayConstants.ERRNO.ERRNO_USER
                        || response.errno == LePayConstants.ERRNO.ERRNO_USER_AUTH_FAILED) {
                    Toast.makeText(BaseApplication.getApplication(), response.errmsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadOrderStatus(String orderNo) {
        if (!TextUtils.isEmpty(orderNo)) {
            this.mOrderNo = orderNo;
            if (mOrderStatusTask == null) {
                mOrderStatusTask = new LePayOrderStatusTask(new OrderStatusCallback(), this.mOrderNo);
            }
            if (mPayPageDialog != null && mPayPageDialog.isShowing()) {
                showBottomLoadingView();
            } else {
                showLoadingView();
            }
            ExecutorHelper.getExecutor().runnableExecutor(mOrderStatusTask);
        }
    }

    protected class OrderStatusCallback implements LePayOnlineCallback {
        @Override
        public void onSuccess(Object result) {
            if (isFinishing()) return;
            hideBottomLoadingView();
            hideLoadingView();
            isCheckOrderStatus = false;
            BaseResponse<LePayOrderStatusBean> response = null;
            if (result == null) {
                showSelectStatusDialog();
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                return;
            }
            response = (BaseResponse<LePayOrderStatusBean>) result;
            if (response.data == null) {
                showSelectStatusDialog();
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (LePayConstants.PAY_STATUS.SUCCESS.equals(response.data.getStatus())) {
                hideSelectStatusDialog();
                setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_SUCCESSED);
            } else if (LePayConstants.PAY_STATUS.FAIL.equals(response.data.getStatus())) {
                hideSelectStatusDialog();
                showPayFailDialog();
            } else {
                showSelectStatusDialog();
            }
        }

        @Override
        public void onError(Object result, int errorCode) {
            if (isFinishing()) return;
            hideBottomLoadingView();
            hideLoadingView();
            isCheckOrderStatus = false;
            showSelectStatusDialog();
            BaseResponse<LePayChannelListBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayChannelListBean>) result;
            }
            if (errorCode == LePayOnlineCallback.ERROR_NETWORK
                    || errorCode == LePayOnlineCallback.ERROR_DATA) {
                Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (errorCode == LePayOnlineCallback.ERROR_OTHER) {
                if (response == null) {
                    Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                    //未知错误
                    if (mLePayChannelListBean == null) {
                        hideSelectStatusDialog();
                        setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                    }
                    return;
                }
                if (response.errno == LePayConstants.ERRNO.ERRNO_USER
                        || response.errno == LePayConstants.ERRNO.ERRNO_USER_AUTH_FAILED) {
                    Toast.makeText(BaseApplication.getApplication(), response.errmsg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // ------ UI ----------

    private void updateData(BaseResponse<LePayChannelListBean> response, boolean isReturn) {
        boolean dataError = false;
        if (response == null) {
            LogHelper.e("[%S] ChannelListCallback response == null", TAG);
            dataError = true;
        } else {
            LePayChannelListBean lepayChannelListBean = response.data;
            if (lepayChannelListBean == null) {
                LogHelper.e("[%S]  ChannelListCallback ChannelListBean == null", TAG);
                dataError = true;
            }
        }
        if (dataError) {
            Toast.makeText(BaseApplication.getApplication(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
            if (isReturn && mLePayChannelListBean == null) {
                setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
            }
            return;
        }
        mLePayChannelListBean = response.data;
        if (isFirstShow) {
            // 统计支付页面曝光
            isFirstShow = false;
            Action.uploadExpose(Action.PAY_PAGE_EXPOSE, mFrom);
        }
        //乐乐花支付状态的数据埋点
        ExecutorHelper.getExecutor().runnableExecutor(new Runnable() {
            @Override
            public void run() {
                if (mLePayChannelListBean != null && mLePayChannelListBean.getChannels() != null
                        && mLePayChannelListBean.getChannels().size() > 0) {
                    for (LePayChannelBean channelBean : mLePayChannelListBean.getChannels()) {
                        if (!TextUtils.isEmpty(channelBean.getName())
                                && LePayConstants.PAY_CHANNEL.CHANNEL_Π_NAME.equals(channelBean.getName())) {
                            if (lastActiveStatus == -1) {
                                //第一次记录当前的状态
                                lastActiveStatus = channelBean.getChannelStatus();
                                return;
                            }
                            if (lastActiveStatus != LePayConstants.YOUΠ_ACTIVE.NORMAL
                                    && lastActiveStatus != LePayConstants.YOUΠ_ACTIVE.ACTIVATION_IN
                                    && lastActiveStatus != LePayConstants.YOUΠ_ACTIVE.ACTIVATED_FROZEN) {
                                //如果上次是非激活的状态
                                if (channelBean.getChannelStatus() == LePayConstants.YOUΠ_ACTIVE.NORMAL
                                        || channelBean.getChannelStatus() == LePayConstants.YOUΠ_ACTIVE.ACTIVATION_IN
                                        || channelBean.getChannelStatus() == LePayConstants.YOUΠ_ACTIVE.ACTIVATED_FROZEN) {
                                    //如果是当前状态是激活的状态，则增加数据埋点
                                    Action.uploadCustom(Action.EVENTTYPE_ACTIVE, Action.PAY_PAGE_YOUΠ_ACTIVATED);
                                    return;
                                }
                                //如果是当前状态是非激活的状态，则记录当前状态
                                lastActiveStatus = channelBean.getChannelStatus();
                                return;
                            }
                            //如果上次是激活的状态
                            lastActiveStatus = channelBean.getChannelStatus();
                        }
                    }
                }
            }
        });
    }

    private void updateUI() {
        if (mDialogTitleId != -1) {
            updateUserSelectDialog(mDialogTitleId);
        }
        if (isShowPayFail) {
            updatePayFailDialog();
        }
        updateSelectStatusDialog();
        updatePayPage();
        updateChannelUI(true);
    }

    private void updateChannelUI(boolean showPayPage) {
        if (mLePayChannelListBean == null) {
            LogHelper.d("[%S]  updateChannelUI mLePayChannelListBean == null", TAG);
            return;
        }
        if (mPriceTv != null) {
            String price = String.format(getString(R.string.price_unit), mLePayChannelListBean.getPrice());
            mPriceTv.setText(price);
        }
        List<LePayChannelBean> lepayChannelBeen = mLePayChannelListBean.getChannels();
        if (mChannelListAdapter != null && lepayChannelBeen != null && lepayChannelBeen.size() > 0) {
            mChannelListAdapter.setData(lepayChannelBeen);
        }
        if (showPayPage) {
            showPayPage();
        }
    }

    private void initPayPageV() {
        if (mPayPageDialog == null) {
            mPayPageDialog = new LeBottomSheet(this);
            mPayPageDialog.setStyle(mPayPageView);
            mPayPageDialog.setCancelable(false);
            mPayPageDialog.setCanceledOnTouchOutside(false);
            findViewById(mPayPageDialog);
        }
    }

    private void updatePayPage() {
        findViewById(mPayPageDialog);
    }

    private void showPayPage() {
        if (mPayPageView == null) {
            LogHelper.e("[%S] mPayPageView == null", TAG);
            return;
        }
        initPayPageV();
        if (!mPayPageDialog.isShowing()) {
            mPayPageDialog.show();
        }
    }


    private void hidePayPage() {
        if (isFinishing()) return;
        if (mPayPageDialog != null && mPayPageDialog.isShowing()) {
            mPayPageDialog.dismiss();
            LogHelper.e("[%S] hidePayPage", TAG);
        }
    }

    private void showSelectStatusDialog() {
        hidePayPage();
        if (mSelectStatusDialog == null) {
            String title = getString(R.string.lepay_user_select_pay_result_title);
            mSelectStatusDialog = new LeBottomSheet(this);
            mSelectStatusDialog.setCancelable(false);
            mSelectStatusDialog.setCanceledOnTouchOutside(false);
            mSelectStatusDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new SelectStatusButtonOnClickListener(),
                    new SelectStatusButton1OnClickListener(), null,
                    new String[]{
                            getString(R.string.lepay_user_select_pay_result_success),
                            getString(R.string.lepay_user_select_pay_result_failed)
                    }, title,
                    null, null, getColor(R.color.colorBtnBlue), false);
        }
        if (!mSelectStatusDialog.isShowing()) {
            mSelectStatusDialog.show();
        }
        isShowPayFail = true;
    }

    private void updateSelectStatusDialog() {
        if (mSelectStatusDialog != null) {
            String title = getString(R.string.lepay_user_select_pay_result_title);
            mSelectStatusDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new SelectStatusButtonOnClickListener(),
                    new SelectStatusButton1OnClickListener(), null,
                    new String[]{
                            getString(R.string.lepay_user_select_pay_result_success),
                            getString(R.string.lepay_user_select_pay_result_failed)
                    }, title,
                    null, null, getColor(R.color.colorBtnBlue), false);
        }
    }

    private void hideSelectStatusDialog() {
        if (mSelectStatusDialog != null && mSelectStatusDialog.isShowing()) {
            mSelectStatusDialog.dismiss();
        }
    }

    private class SelectStatusButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // 查询订单状态
            loadOrderStatus(mOrderNo);
        }
    }

    private class SelectStatusButton1OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mSelectStatusDialog.dismiss();
            setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
        }
    }


    private void showPayFailDialog() {
        // 置为false,在执行onStart时不再显示支付失败弹框
        isResultPayFail = false;
        hidePayPage();
        if (mPayFailDialog == null) {
            String title = getString(R.string.lepay_fail);
            mPayFailDialog = new LeBottomSheet(this);
            mPayFailDialog.setCanceledOnTouchOutside(true);
            mPayFailDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new PayFailButtonOnClickListener(),
                    new PayFailButton1OnClickListener(), null,
                    new String[]{
                            getString(R.string.lepay_repayment),
                            getString(R.string.lepay_channel_cancel)
                    }, title,
                    null, null, getColor(R.color.colorBtnBlue), false);
            mPayFailDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isShowPayFail = false;
                    if (isClickReGoPay) {
                        isClickReGoPay = false;
                        return;
                    }
                    setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                }
            });
            mPayFailDialog.getTitle().setTextColor(getColor(R.color.red));
        }
        Action.uploadExpose(Action.PAY_FAIL_EXPOSE);
        mPayFailDialog.show();
        isShowPayFail = true;
    }

    private void updatePayFailDialog() {
        if (mPayFailDialog != null) {
            String title = getString(R.string.lepay_fail);
            mPayFailDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new PayFailButtonOnClickListener(),
                    new PayFailButton1OnClickListener(), null,
                    new String[]{
                            getString(R.string.lepay_repayment),
                            getString(R.string.lepay_channel_cancel)
                    }, title,
                    null, null, getColor(R.color.colorBtnBlue), false);
            mPayFailDialog.getTitle().setTextColor(getColor(R.color.red));
        }
    }

    private class PayFailButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            isClickReGoPay = true;
            mPayFailDialog.dismiss();
            reGoPay();
        }
    }

    private class PayFailButton1OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mPayFailDialog.dismiss();
        }
    }

    private void showUserSelectDialog(int titleId) {
        hidePayPage();
        String title = getString(titleId);
        mDialogTitleId = titleId;
        if (mNetworkDialog == null) {
            mNetworkDialog = new LeBottomSheet(this);
            mNetworkDialog.setCanceledOnTouchOutside(true);
            mNetworkDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new NetworkButtonOnClickListener(),
                    new NetworkButtonOnClickListener(), null,
                    new String[]{
                            getString(R.string.pay_network_error_sure)
                    }, title,
                    null, null, getResources().getColor(R.color.colorWalletTv), false);
            mNetworkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mDialogTitleId != -1) {
                        setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                    }
                }
            });
        }
        mNetworkDialog.show();
    }


    private void updateUserSelectDialog(int titleId) {
        if (mNetworkDialog != null) {
            String title = getString(titleId);
            mNetworkDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new NetworkButtonOnClickListener(),
                    new NetworkButtonOnClickListener(), null,
                    new String[]{
                            getString(R.string.pay_network_error_sure)
                    }, title,
                    null, null, getResources().getColor(R.color.colorWalletTv), false);
        }
    }

    private class NetworkButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mNetworkDialog.dismiss();
        }
    }

    private void showBottomLoadingView() {
        if (mLoadingV != null && !isShowBottomLoadingView()) {
            mLoadingV.setVisibility(View.VISIBLE);
            mLoadingV.appearAnim();
        }
    }

    private void hideBottomLoadingView() {
        if (mLoadingV != null && isShowBottomLoadingView()) {
            mLoadingV.disappearAnim(null);
            mLoadingV.setVisibility(View.GONE);
        }
    }

    private synchronized boolean isShowBottomLoadingView() {
        if (mLoadingV != null) {
            if (mLoadingV.getVisibility() == View.VISIBLE) {
                return true;
            }
        }
        return false;
    }

    // --- init View -----

    private void init() {
        initPayPageV();
        loadPayChannel();
    }

    private void findViewById(Dialog view) {
        if (view == null) {
            LogHelper.e("[%S] findViewById | view == null", TAG);
            return;
        }
        TextView mPayCancelTv = (TextView) view.findViewById(R.id.pay_cancel_tv);
        mPayCancelTv.setText(R.string.lepay_channel_cancel);
        mPayCancelTv.setOnClickListener(this);
        TextView mPayTitleTv = (TextView) view.findViewById(R.id.pay_title_tv);
        mPayTitleTv.setText(R.string.lepay_payment_center);
        mPriceTv = (TextView) view.findViewById(R.id.pay_price_tv);
        mChannelRecyclerV = (RecyclerView) view.findViewById(R.id.pay_channel_list);
        mChannelListAdapter = new LePayChannelListAdapter(new ChannelItemOnclickListener());
        mLoadingV = (LeLoadingView) view.findViewById(R.id.loading);
        mLinearLayoutManager = new LinearLayoutManager(getBaseContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mChannelRecyclerV.setLayoutManager(mLinearLayoutManager);
        DividerItemDecoration itemDecoration =
                new DividerItemDecoration(BaseApplication.getApplication(), getColor(R.color.divider_horizontal_color),
                        DividerItemDecoration.VERTICAL_LIST, getResources().getDimensionPixelSize(R.dimen.divider_width));
        int extraMargin = getResources().getDimensionPixelSize(R.dimen.margin_default);
        itemDecoration.setExtraMargin(extraMargin, extraMargin, 0, 0);
        mChannelRecyclerV.addItemDecoration(itemDecoration);
        mChannelRecyclerV.setAdapter(mChannelListAdapter);
    }

    private int getUid() {
        int uid = -1;
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            uid = ai.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }

    protected void getExternExtra() {
        Intent intent = getIntent();
        if (intent != null) {
            mExternLePayInfo = intent.getStringExtra(LePayConstants.ApiIntentExtraKEY.LEPAY_INFO);
            mFrom = intent.getStringExtra(CommonConstants.EXTRA_FROM);
        }
        if (TextUtils.isEmpty(mFrom)) {
            mFrom = LePayConstants.PAY_FROM.OTHER;
        }
    }
}
