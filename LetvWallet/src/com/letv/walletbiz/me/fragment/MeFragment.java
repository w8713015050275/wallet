package com.letv.walletbiz.me.fragment;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.BorderedCircleImageView;
import com.letv.tracker2.enums.EventType;
import com.letv.tracker2.enums.Key;
import com.letv.wallet.account.LePayAccountManager;
import com.letv.wallet.account.LePayCommonCallback;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.main.fragment.MainFragment;
import com.letv.walletbiz.me.activity.AccountWebActivity;
import com.letv.walletbiz.me.ui.ToggleTextView;
import com.letv.walletbiz.me.utils.AccountUtils;
import com.letv.walletbiz.order.activity.TotalOrderListActivity;
import com.letv.walletbiz.update.util.UpdateUtil;

import org.xutils.xmain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lijunying on 17-1-9.
 */

public class MeFragment extends MainFragment implements View.OnClickListener, AccountHelper.OnAccountChangedListener {
    private View mRootView;

    private BorderedCircleImageView mUsrIcon;
    private ImageView mSetting;
    private TextView mUsrNickName;
    private ToggleTextView mVerifyFlag;
    private LinearLayout mViewTips, mViewLeLeHuaHome, mViewLeLeHuaBills;
    private TextView mBills;
    private TextView mVipLevels;
    private TextView mLeLeHuaAavailableLimit, mLeLeHuaPaymentAmount;
    private TextView mUsrFeedback;

    private AccountInfo accountInfo;

    private static  final int ACCOUNT_BASIC_RETRY_COUNT = 1;

    private boolean ACCOUNT_FAIL_REASON_PHONE_NULL = false;
    private boolean hasCreateAccount = false;

    private  int retryCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (null == mRootView) {
            mRootView = inflater.inflate(R.layout.main_tab_my, container, false);

            mUsrIcon = (BorderedCircleImageView) mRootView.findViewById(R.id.img_usr_icon);
            mSetting = (ImageView) mRootView.findViewById(R.id.btnSetting);
            mUsrNickName = (TextView) mRootView.findViewById(R.id.tvNickName);
            mVerifyFlag = (ToggleTextView) mRootView.findViewById(R.id.tvVerfify);
            mViewTips = (LinearLayout) mRootView.findViewById(R.id.viewTips);
            mBills = (TextView) mRootView.findViewById(R.id.tvBills);
            mVipLevels = (TextView) mRootView.findViewById(R.id.tvVipLevel);
            mViewLeLeHuaHome = (LinearLayout) mRootView.findViewById(R.id.viewLeLeHuaHome);
            mViewLeLeHuaBills = (LinearLayout) mRootView.findViewById(R.id.viewLeLeHuaBills);
            mLeLeHuaAavailableLimit = (TextView) mRootView.findViewById(R.id.tvLeLeHuaAavailableLimit);
            mLeLeHuaPaymentAmount = (TextView) mRootView.findViewById(R.id.tvPayAmount);
            mUsrFeedback = (TextView) mRootView.findViewById(R.id.tvUsrFeedback);

            mSetting.setOnClickListener(this);
            mUsrIcon.setOnClickListener(this);
            mUsrNickName.setOnClickListener(this);
            mVerifyFlag.setOnClickListener(this);
            mBills.setOnClickListener(this);
            mViewLeLeHuaHome.setOnClickListener(this);
            mViewLeLeHuaBills.setOnClickListener(this);
            mVipLevels.setOnClickListener(this);
            mUsrFeedback.setOnClickListener(this);

        } else if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeAllViews();
        }

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null ;
        switch (v.getId()) {
            case R.id.btnSetting:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("lepay://setting"));
                startActivity(intent);
                break;

            case R.id.img_usr_icon:
            case R.id.tvNickName:
                AccountHelper.getInstance().loginOrJumpLetvAccount(getActivity());
                break;

            case R.id.tvBills:
                intent = new Intent(getActivity(), TotalOrderListActivity.class);
                jumpIntentOnLogin(intent);
                break;

            case R.id.viewLeLeHuaHome:
                if (AccountHelper.getInstance().loginLetvAccountIfNot(getActivity(), null) && checkLeLeHuaValidate()) {
                    AccountUtils.goToLeLeHuaHome(getActivity(), accountInfo.lelehua.active_status);
                }
                break;

            case R.id.viewLeLeHuaBills:
                if (AccountHelper.getInstance().loginLetvAccountIfNot(getActivity(), null) && checkLeLeHuaValidate()) {
                    AccountUtils.goToLeLeHuaBill(getActivity(), accountInfo.lelehua.active_status);
                }
                break;

            case R.id.tvUsrFeedback: //用户反馈
                gotoFeedBack();
                break;

            default:
                if (v.getTag() instanceof AccountInfo.Tips) {
                    AccountInfo.Tips tips = (AccountInfo.Tips) v.getTag();
                    if (TextUtils.isEmpty(tips.jump_param)) {
                        return;
                    }
                    if (tips.jump_param.startsWith("http://") || tips.jump_param.startsWith("https://")) {
                        jumpWeb(tips.jump_param);
                    }else{
                        AppUtils.LaunchApp(getActivity(), tips.package_name, tips.jump_param);
                    }
                }
                break;
        }
    }

    public boolean gotoFeedBack() {
        try {
            Intent intent = new Intent();
            intent.setAction("com.letv.bugservices.reporter");
            intent.putExtra("fromApp", getActivity().getApplication().getPackageName());//fromApp传应用包名
            intent.putExtra("versionId", UpdateUtil.getVersionName(getActivity(), getActivity().getPackageName()));
            startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void startLoadData() {
        if (!checkLogin() || !checkNetWork()) {
            return;
        }
        checkCreateAccount(false);
        //checkVerifyAccount(); //更新状态

        hideBlankPage();
        loadData();
    }

    @Override
    public void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && AccountHelper.getInstance().isLogin(getActivity())) {
            loadData();
        }
    }


    @Override
    public void gotoNext(int type,Bundle bundle) {

    }

    @Override
    public void fragmentDisplay() {
       String from;
        Map<String, Object> props = new HashMap<>();
        Intent intent = null;
        if (getActivity() != null) {
            intent = getActivity().getIntent();
            if (intent != null) {
                from = intent.getStringExtra(WalletConstant.EXTRA_FROM);
                props.put(Key.From.getKeyId(), from);
            }
        }
        Action.uploadCustom(EventType.Expose, Action.ME_PAGE_EXPOSE, props);
        if (intent != null) {
           String NULL = null;
           intent.putExtra(WalletConstant.EXTRA_FROM, NULL);
        }
    }

    @Override
    public void onAccountLogin() {

    }

    @Override
    public void onAccountLogout() {
        accountInfo = null;
        ACCOUNT_FAIL_REASON_PHONE_NULL = false;
        retryCount = 0 ;
        if (mViewTips != null) {
            mViewTips.removeAllViews();
        }
    }

    @Override
    public void onDestroyView() {
        if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        super.onDestroy();
    }

    private boolean checkLogin() {
        if(AccountHelper.getInstance().isLogin(getActivity())){
            return true;
        }
        mUsrIcon.setImageResource(R.drawable.place_holder_star);
        mUsrIcon.setBackgroundResource(R.drawable.place_holder_star);
        mUsrNickName.setText(R.string.me_usr_nologin);
        mLeLeHuaAavailableLimit.setText(R.string.me_lelehua_not_open);  //未激活
        mLeLeHuaPaymentAmount.setText(R.string.me_lelehua_not_open);
        return false ;
    }

    private boolean checkNetWork(){
        if (NetworkHelper.isNetworkAvailable()) {
            return true;
        }
        if (accountInfo == null || accountInfo.basic == null) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
        return false;
    }

    private void updateData(AccountInfo info) {
        if (info != null) {
            accountInfo = info;
            updateAccountBasic();
            updateTips(accountInfo.tips);
            updateAccountLeLeHua();
        }
        if (isEmptyPage()) {
            handErrorPage(AccountConstant.RspCode.ERROR_NETWORK, null); //更新数据后，仍为空， 显示网络异常界面
        }
    }

    private boolean isEmptyPage(){
        if (accountInfo == null || accountInfo.basic == null) {
            return true ;
        }
        return false;
    }

    private void handErrorPage(int errorCode, String errorMsg){
        switch (errorCode) {
            case AccountConstant.RspCode.ERRNO_NO_NETWORK:
                 showBlankPage(BlankPage.STATE_NO_NETWORK);
                break;

            default:   //其他显示网络错误页面
                 showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         hideBlankPage();
                         queryAccount(AccountConstant.QTYPE_ALL);
                     }
                 });
                LogHelper.w("errorCode = " + errorCode + " errorMsg = " + errorMsg);
                break;
        }
    }

    public boolean permitsRetry(int errorCode,  int count) {
        if (errorCode == AccountConstant.RspCode.ERROR_REMOTE_SERVICE_KILLED || errorCode == AccountConstant.RspCode.ERROR_REMOTE_SERVICE_DISCONNECTE) {
            if (count <= ACCOUNT_BASIC_RETRY_COUNT) {
                return true;
            } else {
                LogHelper.w("The Max Retry times has been reached!");
            }
        }
        return false;
    }

    private void updateAccountBasic() {
        if (accountInfo.basic == null) {
            return;
        }

        if (!TextUtils.isEmpty(accountInfo.basic.avatar)) {
            String[] url = accountInfo.basic.avatar.split("\\,");
            xmain.image().bind(mUsrIcon, url[1]);
        }
        if (!TextUtils.isEmpty(accountInfo.basic.memberName)) {
            mUsrNickName.setText(accountInfo.basic.memberName);
        }

       /* if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(accountInfo.basic.verifyStatus)) {
            mVerifyFlag.setChecked(true);
        } else {
            mVerifyFlag.setChecked(false);
        }*/
    }

    private void updateAccountLeLeHua() {
        if (accountInfo.lelehua == null) {
            return;
        }
        if (accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED ||
                accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED_FROZEN) { //已激活
            String limit = getFormateNumber(accountInfo.lelehua.available_limit);
            mLeLeHuaAavailableLimit.setText(limit == null ? "" : String.format(getString(R.string.me_lelehua_limit), limit));
            String oweAmmount = getFormateNumber(accountInfo.lelehua.owe_amount);
            mLeLeHuaPaymentAmount.setText(oweAmmount == null ? "" : String.format(getString(R.string.me_lelehua_owe_amount), oweAmmount));

        } else if (accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVING) { //激活中
            mLeLeHuaAavailableLimit.setText(R.string.me_lelehua_opening);
            mLeLeHuaPaymentAmount.setText(R.string.me_lelehua_opening);
        } else {
            mLeLeHuaAavailableLimit.setText(R.string.me_lelehua_not_open);  //未激活
            mLeLeHuaPaymentAmount.setText(R.string.me_lelehua_not_open);
        }
    }

    private String getFormateNumber(String amount) {
        if (!TextUtils.isEmpty(amount) && !amount.equals("0")) {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
            try {
                return df.format(Double.parseDouble(amount));
            } catch (NumberFormatException e) {
                LogHelper.e(e.getMessage());
            }
        }
        return null;
    }

    private void updateTips(AccountInfo.Tips[] tips){
        mViewTips.removeAllViews();

        if (tips == null || tips.length == 0 || accountInfo == null) {
            return;
        }

        accountInfo.tips = tips;

        LinearLayout child ;
        TextView tvTipTitle , tvTipDesc ;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (AccountInfo.Tips tip : accountInfo.tips) {
            child = (LinearLayout) inflater.inflate(R.layout.me_tips_item, mViewTips, false);
            tvTipTitle = (TextView) child.findViewById(R.id.tvTipTitle);
            tvTipDesc = (TextView) child.findViewById(R.id.tvTipDesc);
            tvTipTitle.setText(tip.title);
            tvTipDesc.setText(tip.jump_desc);
            child.setTag(tip);
            child.setOnClickListener(this);
            mViewTips.addView(child);
        }
    }

    private void jumpIntentOnLogin(final Intent intent) {
        if (intent == null) return;
        if (AccountHelper.getInstance().isLogin(getActivity())) {
            startActivity(intent);
        } else {
            AccountHelper.getInstance().loginLetvAccountIfNot(getActivity(), new AccountManagerCallback() {

                @Override
                public void run(AccountManagerFuture future) {
                    try {
                        if (getActivity() != null && future.getResult() != null && AccountHelper.getInstance().isLogin(getActivity())) {
                            startActivity(intent);
                        }
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private boolean checkAccountStatus(){
        if (!hasCreateAccount && !ACCOUNT_FAIL_REASON_PHONE_NULL) {
            Toast.makeText(getActivity(), R.string.me_lelehua_unavailable, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ACCOUNT_FAIL_REASON_PHONE_NULL) { //未开户，手机号为空，绑手机号
            Toast.makeText(getActivity(), R.string.me_create_account_no_phone, Toast.LENGTH_SHORT).show();
            AccountUtils.goToBindMobile(getActivity());
            ACCOUNT_FAIL_REASON_PHONE_NULL = false;
            return false;
        }
        return true;
    }

    private boolean checkLeLeHuaValidate(){
        if (checkAccountStatus()) {
            if (accountInfo != null && accountInfo.lelehua != null) {
                return true;
            } else { //lelehua is null
                Toast.makeText(getActivity(), R.string.empty_network_error, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private void jumpWeb(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Intent intent = new Intent(getActivity(), AccountWebActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, url);
        startActivity(intent);
    }

    private void loadData() {
        AccountUtils.checkRedirectExpired();
        queryAccount(AccountConstant.QTYPE_ALL);
    }

    private boolean checkCreateAccount(boolean isForceCreate){
        hasCreateAccount = LePayAccountManager.hasCreatedAccount();
        if (!hasCreateAccount && isForceCreate) {
            createAccount();
        }
        return hasCreateAccount;
    }

    private void createAccount() {
        LePayAccountManager.getInstance().createAccount(new LePayCommonCallback() {
            @Override
            public void onSuccess(Object o) {
                hasCreateAccount = true;
                hideLoadingView();
                updateData(accountInfo);
                queryAccount(AccountConstant.QTYPE_TIPS); //开户成功需要更新tip
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                hideLoadingView();
                updateData(accountInfo);
                if (errorCode == AccountConstant.RspCode.ERRNO_MOBILE_EMPTY) {
                    ACCOUNT_FAIL_REASON_PHONE_NULL = true;
                }else {
                    LogHelper.w("createAccount errorCode = " + errorCode);
                }
            }
        });
    }

    private void queryAccount(final String qType) {
        if (isEmptyPage()) {
            showLoadingView();
        }

        LePayAccountManager.getInstance().queryAccount(qType , new LePayCommonCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo info) {
                retryCount = 0;
                if (AccountConstant.QTYPE_TIPS.equals(qType)) {
                    hideLoadingView();
                    updateTips(info == null ? null : info.tips);
                }else if(AccountConstant.QTYPE_ALL.equals(qType)){
                    if (checkCreateAccount(true)) {
                        hideLoadingView();
                        updateData(info);
                    } else {
                        accountInfo = info;
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (isEmptyPage()) {
                    if (permitsRetry(errorCode, retryCount++)) { //service 异常重试一次
                        return;
                    }
                    hideLoadingView();
                    handErrorPage(errorCode, errorMsg); //显示错误页面
                }

            }
        });

    }
}

