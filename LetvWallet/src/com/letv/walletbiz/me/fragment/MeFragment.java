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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.shared.widget.BorderedCircleImageView;
import com.letv.tracker.enums.EventType;
import com.letv.tracker.enums.Key;
import com.letv.wallet.account.LePayAccountManager;
import com.letv.wallet.account.LePayCommonCallback;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.main.fragment.MainFragment;
import com.letv.walletbiz.me.ui.ToggleTextView;
import com.letv.walletbiz.order.activity.TotalOrderListActivity;

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
    private ImageView mViewLeLeHuaDivider;
    private TextView mBills;
    private TextView mVipLevels;
    private TextView mLeLeHuaAavailableLimit, mLeLeHuaPaymentAmount;

    private AccountInfo accountInfo;

    private boolean isDataValidate = false;
    private static  final int ACCOUNT_BASIC_RETRY_COUNT = 1;

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
            mViewLeLeHuaDivider = (ImageView) mRootView.findViewById(R.id.viewLeLeHuaDivider);
            mLeLeHuaAavailableLimit = (TextView) mRootView.findViewById(R.id.tvLeLeHuaAavailableLimit);
            mLeLeHuaPaymentAmount = (TextView) mRootView.findViewById(R.id.tvPayAmount);

            mSetting.setOnClickListener(this);
            mUsrIcon.setOnClickListener(this);
            mUsrNickName.setOnClickListener(this);
            mVerifyFlag.setOnClickListener(this);
            mViewTips.setOnClickListener(this);
            mBills.setOnClickListener(this);
            mVipLevels.setOnClickListener(this);
            mViewLeLeHuaHome.setOnClickListener(this);
            mViewLeLeHuaBills.setOnClickListener(this);

            //隐藏乐乐花
            mViewLeLeHuaHome.setVisibility(View.GONE);
            mViewLeLeHuaBills.setVisibility(View.GONE);
            mViewLeLeHuaDivider.setVisibility(View.GONE);

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
                jumpIntentOnLogin(intent);
                break;

            case R.id.img_usr_icon:
            case R.id.tvNickName:
                AccountHelper.getInstance().loginOrJumpLetvAccount(getActivity());
                isDataValidate = false; //跳转乐视界面， 返回更新数据
                break;

            case R.id.tvBills:
                intent = new Intent(getActivity(), TotalOrderListActivity.class);
                jumpIntentOnLogin(intent);
                break;

           /* case R.id.tvVipLevel:
                break;
            case R.id.tvVerfify:
                break;
            case R.id.viewLeLeHuaHome:
                 intent  = new Intent(getActivity(), WalletMainWebActivity.class);
                if (isCreateAccount) {
                    if (isAccountVerifed()) {
                        if (accountInfo == null || accountInfo.lelehua == null) {
                            return;
                        }
                        jumpWeb(getLeLeHuaURL(LELEHUABILLS, accountInfo.lelehua.active_status));
                    }else{
                        jumpAccountVerify();
                    }
                };
                break;
            case R.id.viewLeLeHuaBills:
                //jumpWeb(getLeLeHuaURL(LELEHUABILLS, accountInfo.lelehua.active_status));
                break;

            case R.id.viewTips:
                break;
                */
        }
    }

    @Override
    public void startLoadData() {
        if (!checkLogin() || !checkNetWork()) {
            return;
        }
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
        isDataValidate = false;
        retryCount = 0 ;
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
        mLeLeHuaAavailableLimit.setVisibility(View.GONE);
        mLeLeHuaPaymentAmount.setVisibility(View.GONE);
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

    private void updateAccountInfo(String qType, AccountInfo info) {
        if (info == null || TextUtils.isEmpty(qType)) {
            return;
        }
        switch (qType) {
            case AccountConstant.QTYPE_BASIC:
                updateAccountBasic(info.basic);
                break;

            /*  case AccountConstant.QTYPE_ALL:
                accountInfo = info;;
                if (accountInfo.basic != null) {
                    updateAccountBasic();
                }
                if (accountInfo.lelehua != null) {
                    mViewLeLeHuaHome.setVisibility(View.VISIBLE);
                    mViewLeLeHuaBills.setVisibility(View.VISIBLE);
                    mViewLeLeHuaDivider.setVisibility(View.VISIBLE);
                    updateAccountLeLeHua();
                }else{
                    mViewLeLeHuaHome.setVisibility(View.GONE);
                    mViewLeLeHuaBills.setVisibility(View.GONE);
                    mViewLeLeHuaDivider.setVisibility(View.GONE);
                }
                break;

            case AccountConstant.QTYPE_LELEHUA:
                if (info.lelehua != null) {
                    accountInfo.lelehua = info.lelehua;
                    updateAccountLeLeHua();
                }
                break;

            case AccountConstant.QTYPE_TIPS:

                break;
            */
        }
    }

    private boolean checkEmptyPage(){
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

             default:
                 showNetAbnormalPage();
                break;
        }
    }

    private void showNetAbnormalPage(){
        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBlankPage();
                queryAccount(AccountConstant.QTYPE_BASIC);
            }
        });
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

    private void updateAccountBasic(AccountInfo.BasicAccount info) {
        if (info == null) {
            return;
        }

        accountInfo.basic = info;

        if (!TextUtils.isEmpty(accountInfo.basic.avatar)) {
            String[] url = accountInfo.basic.avatar.split("\\,");
            xmain.image().bind(mUsrIcon, url[1]);
        }
        if (!TextUtils.isEmpty(accountInfo.basic.memberName)) {
            mUsrNickName.setText(accountInfo.basic.memberName);
        }

        isDataValidate = true;
        retryCount = 0 ;

        /*if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(basic.verifyStatus)) {
            mVerifyFlag.setChecked(true);
            mViewTips.setVisibility(View.GONE);
        } else {
            mVerifyFlag.setChecked(false);
            mViewTips.setVisibility(View.VISIBLE);
        }*/
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

    private void loadData() {
        if (isDataValidate) { //非强制更新 & 数据未失效 返回
            return;
        }
        queryAccount(AccountConstant.QTYPE_BASIC);
    }

    private void createAccount() {
        LePayAccountManager.getInstance().createAccount(null);
    }

    private void queryAccount(final String qType) {
        if (accountInfo == null || accountInfo.basic == null) {
            showLoadingView();
        }

        LePayAccountManager.getInstance().queryAccount(qType, new LePayCommonCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo info) {
                if (!LePayAccountManager.hasCreatedAccount()) {
                    createAccount(); //默认开一次户
                }
                if (accountInfo == null || accountInfo.basic == null) {
                    hideLoadingView();
                    accountInfo = info;
                }
                updateAccountInfo(qType, info); //update account info

                if (checkEmptyPage()) { // 页面为空， 显示网络异常界面
                    showNetAbnormalPage();
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (checkEmptyPage()) { //page 为空
                    if (permitsRetry(errorCode, retryCount++)) { //service 异常重试一次
                        return;
                    }
                    hideLoadingView();
                    handErrorPage(errorCode, errorMsg); //显示错误页面
                }

            }
        });

    }

    //******************** 乐乐花 ！！！！！！！！！！！！！！！！！！！！！

    /*private String getLeLeHuaJumpType(int leleHuaStatus) {
        switch (leleHuaStatus) {
            case AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED:
            case AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED_FROZEN:
                return AccountConstant.JTYPE_LELEHUA_HOME;

            case AccountConstant.LELEHUA_ACCOUNT_STATE_NOACTIVATED:
                return AccountConstant.JTYPE_LELEHUA_ACTIVE;

            case AccountConstant.LELEHUA_ACCOUNT_STATE_NOACTIVATED_FROZEN:
                return AccountConstant.JTYPE_LELEHUA_NOACTIVE;

            default:
                return null;
        }
    }

    private boolean isLeLeHuaActivated() {
        if (accountInfo != null && accountInfo.lelehua != null) {
            return accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED ||
                    accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED_FROZEN;

        }
        return false;
    }


    private String getLeLeHuaURL(int jumpType, int leleHuaStatus) {
        if (redirectURL == null) {
            return null;
        }

        switch (leleHuaStatus) {
            case AccountConstant.LELEHUA_ACCOUNT_STATE_NOACTIVATED:
                return redirectURL.lelehua_active; // 跳转到乐乐花激活页面

            case AccountConstant.LELEHUA_ACCOUNT_STATE_NOACTIVATED_FROZEN:
                return redirectURL.lelehua_noactive; // 跳转到乐乐花不可用页面

            default:
                if (LELEHUAHOME == jumpType) {
                    return redirectURL.lelehua_home;
                }
                return redirectURL.lelehua_bill_list;
        }

    }

    private void updateAccountLeLeHua() {
        if (accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED ||
                accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED_FROZEN) {
            //mLeLeHuaAavailableLimit.setText("￥"+accountInfo.lelehua.available_limit);
            //mLeLeHuaPaymentAmount.setText("￥"+accountInfo.lelehua.owe_amount);
        } else {
            mLeLeHuaAavailableLimit.setText(R.string.me_lelehua_not_open);
            mLeLeHuaPaymentAmount.setText(R.string.me_lelehua_not_open);
        }
        mLeLeHuaAavailableLimit.setVisibility(View.VISIBLE);
        mLeLeHuaPaymentAmount.setVisibility(View.VISIBLE);
    }
*/
}

