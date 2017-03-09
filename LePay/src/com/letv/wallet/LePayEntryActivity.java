package com.letv.wallet;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.letv.lepaysdk.Constants;
import com.letv.lepaysdk.ELePayState;
import com.letv.lepaysdk.LePayConfig;
import com.letv.lepaysdk.activity.EUICashierAcitivity;
import com.letv.shared.widget.LeBottomSheet;
import com.letv.tracker2.enums.EventType;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.utils.LePayConstants;

public class LePayEntryActivity extends FragmentActivity {

    private static final String TAG = LePayEntryActivity.class.getSimpleName();
    private static final int PAY_REQUESTCODE = 100;
    private String mExternLePayInfo = null;
    private int mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
    private LeBottomSheet mNetworkDialog;
    private int mDialogTitleId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LogHelper.e("[%S] %s", TAG, "onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lepay_entry_activity);
        int uid = getUid();
        if (!NetworkHelper.isNetworkAvailable()) {
            //未开启移动网络
            if (!NetworkHelper.isDataNetworkAvailable()) {
                showUserSelectDialog(R.string.pay_no_network);
                return;
            }
            //开启了移动网络并且被禁用
            if (!NetworkHelper.isEnableMobileNetwork(uid)) {
                showUserSelectDialog(R.string.pay_network_error);
                return;
            }
        }
        //链接wifi并且wifi被禁用
        if (NetworkHelper.isWifiAvailable() && !NetworkHelper.isEnableWifi(uid)) {
            //未开启移动网络
            showUserSelectDialog(R.string.pay_network_error);
            return;
        }
        mDialogTitleId = -1;
        mExternLePayInfo = getExternExtra();
        if (TextUtils.isEmpty(mExternLePayInfo)) {
            setReturnResult(LePayConstants.PAY_RETURN_RESULT.PAY_FAILED);
            return;
        }
        LogHelper.d("[%S] %s", TAG, "调起支付");
        startPay(mExternLePayInfo);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogHelper.e("[%S] %s", TAG, "onConfigurationChanged");
        updateUI();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkDialog != null) {
            mNetworkDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogHelper.d("[%S] %s", TAG, "onActivityResult");
        if (requestCode == PAY_REQUESTCODE) {
            mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
            if (data != null) {
                ELePayState eLePayState = (ELePayState) data.getSerializableExtra(Constants.LePayApiResult.LEPAY_EPAYSTATUS);
                String content = data.getStringExtra(Constants.LePayApiResult.LEPAY_CONTENT);
                if (ELePayState.OK.equals(eLePayState)) {
                    mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_SUCCESSED;
                } else if (ELePayState.FAILT.equals(eLePayState)) {
                    mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_FAILED;
                } else if (ELePayState.CANCEL.equals(eLePayState)) {
                    mPayReturnResult = LePayConstants.PAY_RETURN_RESULT.PAY_CANCLE;
                    Action.uploadCustom(EventType.Close, Action.PAY_PAGE_CLOSE);
                } else if (ELePayState.NONETWORK.equals(eLePayState)) {
                }
                if (eLePayState != ELePayState.OK) {
                    LogHelper.e("[%S] %s", TAG, "ELePayState == " + mPayReturnResult);
                }
            }
            setReturnResult(mPayReturnResult);
        }
    }

    private void updateUI() {
        if (mDialogTitleId != -1) {
            updateUserSelectDialog(mDialogTitleId);
        }
    }

    private void showUserSelectDialog(int titleId) {
        String title = getString(titleId);
        mDialogTitleId = titleId;
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

    private void updateUserSelectDialog(int titleId) {
        if (mNetworkDialog != null) {
            String title = getString(titleId);
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
        LePayConfig lePayConfig = new LePayConfig();//参数配置
        lePayConfig.hasShowPaySuccess = false;//是否显示成功提示
        if (LePayEntryActivity.this == null) {
            LogHelper.e("[%S] %s", TAG, "LePayEntryActivity.this == null");
        }
        Intent intent = new Intent(LePayEntryActivity.this, EUICashierAcitivity.class);
        intent.putExtra(Constants.ApiIntentExtraKEY.LEPAY_INFO, payInfo);
        intent.putExtra(Constants.ApiIntentExtraKEY.LEPAY_CONFIG, lePayConfig);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, PAY_REQUESTCODE);
    }

}
