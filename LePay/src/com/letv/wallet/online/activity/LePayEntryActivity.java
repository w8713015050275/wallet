package com.letv.wallet.online.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.letv.shared.widget.LeBottomSheet;
import com.letv.tracker2.enums.EventType;
import com.letv.tracker2.enums.Key;
import com.letv.wallet.R;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.DividerGridItemDecoration;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
    private LinearLayoutManager mLinearLayoutManager;
    private LePayChannelListAdapter mChannelListAdapter;

    private boolean isActivityResult, isShowPayFail, isClickReGoPay;
    private boolean isFirst = true, isFirstShow = true;
    private String mFrom = LePayConstants.PAY_FROM.OTHER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d("[%S] %s", TAG, "onCreate");
        registerNetWorkReceiver();
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        mPayPageView = View.inflate(getApplicationContext(), R.layout.lepay_channel_view, null);
    }

    private boolean checkData(boolean showDialog) {
        if (!checkAppNetWork(showDialog)) {
            return false;
        }
        mDialogTitleId = -1;
        if (mNetworkDialog != null) {
            mNetworkDialog.dismiss();
        }
        if (isActivityResult) {
            return false;
        }
        if (isShowPayFail) {
            return false;
        }
        if (mLePayChannelListBean != null) {
            showPayPage();
        }
        return true;
    }

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
        switch (resultCode) {
            case RESULT_OK:
                switch (requestCode) {
                    case WEB_FORRESULT:
                        isActivityResult = true;
                        if (data != null) {
                            String jumpType = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.JUMP_TYPE);
                            if (jumpType.equals(LePayConstants.JUMP_TYPE.PAY)) {
                                payResult(data);
                            } else if (jumpType.equals(LePayConstants.JUMP_TYPE.ACTIVE)) {
                                activeResult(data);
                            }
                            break;
                        }
                        break;
                }
        }
    }

    private void activeResult(Intent data) {
        String activeStatus = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.ACTIVE_STATUS);
        String payStatus = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.PAYSTATUS);
        if (!TextUtils.isEmpty(activeStatus)) {
            if (LePayConstants.ACTIVE_STATUS.YES.equals(activeStatus)) {
                // 激活成功后 更新支付列表数据，调起支付
                loadPayChannel();
                String decryptUsingStatus = SslUtil.getInstance().decryptData(payStatus);
                if (!TextUtils.isEmpty(decryptUsingStatus)
                        && LePayConstants.PAY_STATUS.AVAILABLE.equals(decryptUsingStatus)) {
                    // 如果支付为可用状态获取支付地址并调起对应的支付
                    if (mCurrentChannelBean != null) {
                        getCashierUrl(mCurrentChannelBean.getName());
                    }
                }
            } else if (LePayConstants.ACTIVE_STATUS.NO.equals(activeStatus)) {
                // 此时状态有可能为激活状态，因此刷新列表数据
                showLoadingView();
                loadPayChannel();
            }
        }
    }

    private void payResult(Intent data) {
        String info = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.INFO);
        if (!TextUtils.isEmpty(info)) {
            String decryptInfo = SslUtil.getInstance().decryptData(info);
            if (!TextUtils.isEmpty(decryptInfo)) {
                String[] strs = decryptInfo.split("\\^");
                if (strs != null && strs.length == 2) {
                    String order_no = strs[0];
                    String pay_status = strs[1];
                    if (LePayConstants.PAY_STATUS.SUCCESS.equals(pay_status)) {
                        if (!TextUtils.isEmpty(order_no)) {
                            // 查询订单状态
                            loadOrderStatus(order_no);
                            return;
                        } else {
                            Map<String, Object> props = new HashMap<String, Object>();
                            props.put(Key.Content.getKeyId(), "orderId");
                            Action.uploadCustom(EventType.Exception.getEventId(), Action.PAY_RESULT_EXCEPTION, props);
                        }
                    } else if (LePayConstants.PAY_STATUS.FAIL.equals(pay_status)) {
                        // 支付失败
                    } else {
                        Map<String, Object> props = new HashMap<String, Object>();
                        props.put(Key.Content.getKeyId(), "paymentStatus");
                        Action.uploadCustom(EventType.Exception.getEventId(), Action.PAY_RESULT_EXCEPTION, props);
                    }
                    showPayFailDialog();
                }
            }
        } else {
            String status = data.getStringExtra(LePayConstants.ApiIntentExtraKEY.PAY_STATUS);
            if (!TextUtils.isEmpty(status)) {
                if (!TextUtils.isEmpty(status) && LePayConstants.PAY_STATUS.FAIL.equals(status)) {
                    showPayFailDialog();
                }
            }
        }
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
                    return;
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
                    startBossPay(channelBean.getChannelId(), mExternLePayInfo);
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

    private void startBossPay(int channelId, String payInfo) {
        String channelStr = Constants.ILepayChannel.alipay_channelId;
        switch (channelId) {
            case LePayConstants.PAY_CHANNEL.CHANNEL_ALIPAY:
                channelStr = Constants.ILepayChannel.alipay_channelId;
                break;
            case LePayConstants.PAY_CHANNEL.CHANNEL_WXPAY:
                channelStr = Constants.ILepayChannel.wx_channelId;
                break;
        }
        LogHelper.d("[%S] start boss pay", TAG);
        LePayApi.createGetdirectpay(this, channelStr, payInfo, new LePay.ILePayCallback() {

            @Override
            public void payResult(ELePayState eLePayState, String s) {
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
                        showPayFailDialog();
                        LogHelper.e("[%S] %s", TAG, "ELePayState == " + mPayReturnResult);
                    }
                }
            }
        });
    }

    public void setReturnResult(int result) {
        switch (result) {
            case LePayConstants.PAY_RETURN_RESULT.PAY_CANCLE:
                // 统计支付取消
                Action.uploadCustom(EventType.Close, Action.PAY_PAGE_CLOSE);
                break;
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
    private void loadPayChannel() {
        if (mPayChannelTask == null) {
            mPayChannelTask = new LePayChannelListLoadTask(new ChannelListCallback(), this.mExternLePayInfo);
        }
        ExecutorHelper.getExecutor().runnableExecutor(mPayChannelTask);
    }

    protected class ChannelListCallback implements LePayOnlineCallback {
        @Override
        public void onSuccess(Object result) {
            if (isFinishing()) return;
            hideLoadingView();
            BaseResponse<LePayChannelListBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayChannelListBean>) result;
            }
            updateData(response);
        }

        @Override
        public void onError(Object result, int errorCode) {
            if (isFinishing()) return;
            hideLoadingView();
            BaseResponse<LePayChannelListBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayChannelListBean>) result;
            }
            switch (errorCode) {
                case LePayOnlineCallback.ERROR_NETWORK:
                case LePayOnlineCallback.ERROR_DATA:
                    Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                    if (mLePayChannelListBean == null) {
                        setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                    }
                    break;
                case LePayOnlineCallback.ERROR_OTHER:
                    if (response == null) {
                        Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                        //未知错误
                        if (mLePayChannelListBean == null) {
                            setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                        }
                        return;
                    }
                    switch (response.errno) {
                        case LePayConstants.ERRNO.ERRNO_USER:
                            Toast.makeText(getBaseContext(), response.errmsg, Toast.LENGTH_SHORT).show();
                            break;
                        case LePayConstants.ERRNO.ERRNO_USER_AUTH_FAILED:
                            Toast.makeText(getBaseContext(), response.errmsg, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                    break;
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
            showLoadingView();
            ExecutorHelper.getExecutor().runnableExecutor(mCashierUrlTask);
        }
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
                    return;
                }
                goCashier(urlBean.getCashierUrl());
            }
        }

        @Override
        public void onError(Object result, int errorCode) {
            if (isFinishing()) return;
            hideLoadingView();
            BaseResponse<LePayCashierUrlBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayCashierUrlBean>) result;
            }
            switch (errorCode) {
                case LePayOnlineCallback.ERROR_DATA:
                case LePayOnlineCallback.ERROR_NETWORK:
                    Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                    return;
                case LePayOnlineCallback.ERROR_OTHER:
                    if (response == null) {
                        Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                        //未知错误
                        if (mLePayChannelListBean == null) {
                            setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                        }
                        return;
                    }
                    switch (response.errno) {
                        case LePayConstants.ERRNO.ERRNO_USER:
                            Toast.makeText(getBaseContext(), response.errmsg, Toast.LENGTH_SHORT).show();
                            break;
                        case LePayConstants.ERRNO.ERRNO_USER_AUTH_FAILED:
                            Toast.makeText(getBaseContext(), response.errmsg, Toast.LENGTH_SHORT).show();
                            break;
                        default: {
                            Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }

    private void loadOrderStatus(String orderNo) {
        if (!TextUtils.isEmpty(orderNo)) {
            this.mOrderNo = orderNo;
            if (mOrderStatusTask == null) {
                mOrderStatusTask = new LePayOrderStatusTask(new OrderStatusCallback(), this.mOrderNo);
            }
            showLoadingView();
            ExecutorHelper.getExecutor().runnableExecutor(mOrderStatusTask);
        }
    }

    protected class OrderStatusCallback implements LePayOnlineCallback {
        @Override
        public void onSuccess(Object result) {
            if (isFinishing()) return;
            hideLoadingView();
            isActivityResult = false;
            BaseResponse<LePayOrderStatusBean> response = null;
            if (result == null) {
                showSelectStatusDialog();
                Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                return;
            }
            response = (BaseResponse<LePayOrderStatusBean>) result;
            if (response.data == null) {
                showSelectStatusDialog();
                Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (LePayConstants.PAY_STATUS.SUCCESS.equals(response.data.getStatus())) {
                setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_SUCCESSED);
            } else if (LePayConstants.PAY_STATUS.FAIL.equals(response.data.getStatus())) {
                showPayFailDialog();
            } else {
                showSelectStatusDialog();
            }
        }

        @Override
        public void onError(Object result, int errorCode) {
            if (isFinishing()) return;
            hideLoadingView();
            isActivityResult = false;
            showSelectStatusDialog();
            BaseResponse<LePayChannelListBean> response = null;
            if (result != null) {
                response = (BaseResponse<LePayChannelListBean>) result;
            }
            switch (errorCode) {
                case LePayOnlineCallback.ERROR_NETWORK:
                case LePayOnlineCallback.ERROR_DATA:
                    Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                    break;
                case LePayOnlineCallback.ERROR_OTHER:
                    if (response == null) {
                        Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
                        //未知错误
                        if (mLePayChannelListBean == null) {
                            setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
                        }
                        return;
                    }
                    switch (response.errno) {
                        case LePayConstants.ERRNO.ERRNO_USER:
                            Toast.makeText(getBaseContext(), response.errmsg, Toast.LENGTH_SHORT).show();
                            break;
                        case LePayConstants.ERRNO.ERRNO_USER_AUTH_FAILED:
                            Toast.makeText(getBaseContext(), response.errmsg, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    }

    // ------ UI ----------

    private void updateData(BaseResponse<LePayChannelListBean> response) {
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
            Toast.makeText(getBaseContext(), R.string.lepay_data_error, Toast.LENGTH_SHORT).show();
            if (mLePayChannelListBean == null) {
                setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
            }
            return;
        }
        mLePayChannelListBean = response.data;
        if (isFirstShow) {
            // 统计支付页面曝光
            isFirstShow = false;
            Action.uploadExpose(Action.PAY_PAGE_EXPOSE, mFrom);
            ExecutorHelper.getExecutor().runnableExecutor(new Runnable() {
                @Override
                public void run() {
                    if (mLePayChannelListBean != null && mLePayChannelListBean.getChannels() != null
                            && mLePayChannelListBean.getChannels().size() > 0) {
                        for (LePayChannelBean channelBean : mLePayChannelListBean.getChannels()) {
                            if (!TextUtils.isEmpty(channelBean.getName())
                                    && LePayConstants.PAY_CHANNEL.CHANNEL_Π_NAME.equals(channelBean.getName())) {
                                if (channelBean.getChannelStatus() == LePayConstants.YOUΠ_ACTIVE.NORMAL
                                        || channelBean.getChannelStatus() == LePayConstants.YOUΠ_ACTIVE.ACTIVATION_IN
                                        || channelBean.getChannelStatus() == LePayConstants.YOUΠ_ACTIVE.ACTIVATED_FROZEN) {
                                    Action.uploadCustom(Action.EVENTTYPE_ACTIVE, Action.PAY_PAGE_YOUΠ_ACTIVATED);
                                }
                            }
                        }
                    }
                }
            });
        }
        updateChannelUI();
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
        updateChannelUI();
    }

    private void updateChannelUI() {
        if (mLePayChannelListBean == null) {
            LogHelper.d("[%S]  updateChannelUI mLePayChannelListBean == null", TAG);
            return;
        }
        if (mPriceTv != null) {
            String price = String.format(getString(R.string.lepay_payment_price_str), mLePayChannelListBean.getPrice());
            mPriceTv.setText(price);
        }
        List<LePayChannelBean> lepayChannelBeen = mLePayChannelListBean.getChannels();
        if (mChannelListAdapter != null && lepayChannelBeen != null && lepayChannelBeen.size() > 0) {
            mChannelListAdapter.setData(lepayChannelBeen);
        }
        showPayPage();
    }

    private void initPayPageV() {
        if (mPayPageDialog == null) {
            mPayPageDialog = new LeBottomSheet(this);
            mPayPageDialog.setStyle(mPayPageView);
            mPayPageDialog.setCanceledOnTouchOutside(false);
            findViewById(mPayPageDialog);
        }
    }

    private void updatePayPage() {
        if (mPayPageView == null) {
            LogHelper.e("[%S] mPayPageView == null", TAG);
            return;
        }
        findViewById(mPayPageDialog);
        if (mPayPageDialog != null) {
            mPayPageDialog.setStyle(mPayPageView);
        }
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
        mSelectStatusDialog.show();
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

    private class SelectStatusButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // 查询订单状态
            mSelectStatusDialog.dismiss();
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
                    finish();
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
        Intent intent = null;
        Bundle bundle = null;
        String value = null;
        String from = null;
        intent = getIntent();
        if (intent != null) {
            value = intent.getStringExtra(LePayConstants.ApiIntentExtraKEY.LEPAY_INFO);
            from = intent.getStringExtra(CommonConstants.EXTRA_FROM);
        }
        mExternLePayInfo = value;
        mFrom = from;
    }
}
