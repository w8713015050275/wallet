package com.letv.wallet;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.letv.lepaysdk.ELePayState;
import com.letv.lepaysdk.LePay.ILePayCallback;
import com.letv.lepaysdk.LePayApi;
import com.letv.lepaysdk.LePayConfig;
import com.letv.shared.widget.LeBottomSheet;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.utils.LePayConstants;

public class LePayEntryActivity extends FragmentActivity {

    private static final String TAG = LePayEntryActivity.class.getSimpleName();
    private static String PAYKEY = "GoPay";
    private boolean GOPAY = false;
    private String mExternLePayInfo = null;
    private int mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
    private LeBottomSheet mNetworkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lepay_entry_activity);
        int uid = getUid();
        if (!NetworkHelper.isNetworkAvailable()) {
            //未开启移动网络
            if (!NetworkHelper.isDataNetworkAvailable()) {
                showUserSelectDialog(getString(R.string.pay_no_network));
                return;
            }
            //开启了移动网络并且被禁用
            if (!NetworkHelper.isEnableMobileNetwork(uid)) {
                showUserSelectDialog(getString(R.string.pay_network_error));
                return;
            }
        }
        //链接wifi并且wifi被禁用
        if (NetworkHelper.isWifiAvailable() && !NetworkHelper.isEnableWifi(uid)) {
            //未开启移动网络
            showUserSelectDialog(getString(R.string.pay_network_error));
            return;
        }
        mExternLePayInfo = getExternExtra();
        if (TextUtils.isEmpty(mExternLePayInfo)) {
            setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
            return;
        }
        if (savedInstanceState == null || (savedInstanceState != null && !savedInstanceState.getBoolean(PAYKEY))) {
            LogHelper.d("%S %s", TAG, "掉起支付");
            startPay(mExternLePayInfo);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean(PAYKEY, GOPAY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkDialog != null) {
            mNetworkDialog.dismiss();
        }
    }

    private void showUserSelectDialog(String title) {
        if (mNetworkDialog == null) {
            mNetworkDialog = new LeBottomSheet(this);
            mNetworkDialog.setCloseOnTouchOutside(false);
            mNetworkDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mNetworkDialog.dismiss();
                            finish();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mNetworkDialog.dismiss();
                            finish();
                        }
                    }, null,
                    new String[]{
                            getString(R.string.pay_network_error_sure)
                    }, title,
                    null, null, getResources().getColor(R.color.colorWalletTv), false);
        }
        mNetworkDialog.show();
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

    public void setReturnResult(int result) {
        Intent intent = new Intent();
        intent.putExtra(LePayConstants.ApiReqeustKey.PAY_RETURN_RESULT, result);
        setResult(RESULT_OK, intent);
        GOPAY = false;
        finish();
    }

    protected String getExternExtra() {
        Intent intent = null;
        Bundle bundle = null;
        String value = null;
        intent = getIntent();
        if (intent != null) {
            bundle = intent.getExtras();
            if (bundle != null) {
                value = bundle.getString(LePayConstants.ApiIntentExtraKEY.LEPAY_INFO);
            }
        }
        return value;
    }

    public void startPay(String payInfo) {
        GOPAY = true;
        LePayConfig lePayConfig = new LePayConfig();//参数配置
        lePayConfig.hasShowPaySuccess = false;//是否显示成功提示
        LePayApi.initConfig(LePayEntryActivity.this, lePayConfig);
        LePayApi.doHalfPay(LePayEntryActivity.this, payInfo, new ILePayCallback() {
            @Override
            public void payResult(ELePayState status, String message) {
                mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
                if (ELePayState.CANCEL == status) {    //支付取消
                    mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_CANCLE;
                } else if (ELePayState.FAILT == status) {        //支付失败
                    mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
                } else if (ELePayState.OK == status) {            //支付成功
                    mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_SUCCESSED;
                } else if (ELePayState.WAITTING == status) {    //支付中
                } else if (ELePayState.NONETWORK == status) {    //网络异常
                }
                if (status != ELePayState.OK) {
                    LogHelper.e(TAG, "ELePayState == " + mPayReturnResult + "  message :" + message);
                }
                setReturnResult(mPayReturnResult);
            }
        });
    }
}
