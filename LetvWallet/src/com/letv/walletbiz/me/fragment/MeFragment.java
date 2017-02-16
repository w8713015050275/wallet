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
import com.letv.wallet.account.LePayAccountManager;
import com.letv.wallet.account.LePayCommonCallback;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.fragment.MainFragment;
import com.letv.walletbiz.me.ui.ToggleTextView;
import com.letv.walletbiz.order.activity.TotalOrderListActivity;

import org.xutils.xmain;

import java.io.IOException;

/**
 * Created by lijunying on 17-1-9.
 */

public class MeFragment extends MainFragment implements View.OnClickListener, AccountHelper.OnAccountChangedListener {
    private View mRootView;

    private BorderedCircleImageView mUsrIcon;
    private Button mSetting;
    private TextView mUsrNickName;
    private ToggleTextView mVerifyFlag;
    private LinearLayout mViewTips, mViewLeLeHuaHome, mViewLeLeHuaBills;
    private ImageView mViewLeLeHuaDivider;
    private TextView mBills;
    private TextView mVipLevels;
    private TextView mLeLeHuaAavailableLimit, mLeLeHuaPaymentAmount;

    private AccountInfo accountInfo;
    private RedirectURL redirectURL;

    private boolean isAccountChanged = false;
    private boolean isCreateAccount = false;
    private boolean isPhoneAvaible = true;

    public static final int LELEHUAHOME = 1;
    public static final int LELEHUABILLS = 2;

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
            mSetting = (Button) mRootView.findViewById(R.id.btnSetting);
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

            case R.id.tvVerfify:
                break;

            case R.id.tvBills:
                intent = new Intent(getActivity(), TotalOrderListActivity.class);
                jumpIntentOnLogin(intent);
                break;

            case R.id.tvVipLevel:
                break;
            case R.id.viewLeLeHuaHome:
                /* intent  = new Intent(getActivity(), WalletMainWebActivity.class);
                if (isCreateAccount) {
                    if (isAccountVerifed()) {
                        if (accountInfo == null || accountInfo.lelehua == null) {
                            return;
                        }
                        jumpWeb(getLeLeHuaURL(LELEHUABILLS, accountInfo.lelehua.active_status));
                    }else{
                        jumpAccountVerify();
                    }
                };*/
                break;
            case R.id.viewLeLeHuaBills:
                //jumpWeb(getLeLeHuaURL(LELEHUABILLS, accountInfo.lelehua.active_status));
                break;
            case R.id.viewTips:
                break;
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
    public boolean displayActionbar() {
        return false;
    }

    @Override
    public void gotoNext(int type) {

    }

    @Override
    public void onAccountLogin() {
        isAccountChanged = true;
        isPhoneAvaible = true; //重新获取手机号
    }

    @Override
    public void onAccountLogout() {
        isAccountChanged = true;
        accountInfo = null;
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
        if (accountInfo == null) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
        return false;
    }

    private void updateAccountInfo(String qType, AccountInfo info) {
        if (info == null || TextUtils.isEmpty(qType)) {
            return;
        }
        switch (qType) {
            case AccountConstant.QTYPE_ALL:
                accountInfo = info;
                isAccountChanged = false;
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

            case AccountConstant.QTYPE_BASIC:
                if (info.basic != null) {
                    accountInfo.basic = info.basic;
                    updateAccountBasic();
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
        }
    }

    private void updateAccountBasic() {
        if (!TextUtils.isEmpty(accountInfo.basic.avatar)) {
            String[] url = accountInfo.basic.avatar.split("\\,");
            xmain.image().bind(mUsrIcon, url[1]);
        }
        if (!TextUtils.isEmpty(accountInfo.basic.memberName)) {
            mUsrNickName.setText(accountInfo.basic.memberName);
        }

        /*if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(basic.verifyStatus)) {
            mVerifyFlag.setChecked(true);
            mViewTips.setVisibility(View.GONE);
        } else {
            mVerifyFlag.setChecked(false);
            mViewTips.setVisibility(View.VISIBLE);
        }*/
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
        if (!NetworkHelper.isNetworkAvailable()) {
            return;
        }

        isCreateAccount = LePayAccountManager.getInstance().hasCreatedAccount();
        if(!isCreateAccount && isPhoneAvaible){ //有手机号码去开户
            createAccount();
            return;
        }

        if (accountInfo == null || isAccountChanged) {
            queryAccount(AccountConstant.QTYPE_ALL);
        }

        //redirectAllType();
    }

    private void createAccount() {
        if (accountInfo == null || accountInfo.basic == null) {
            showLoadingView();
        }
        LePayAccountManager.getInstance().createAccount(new LePayCommonCallback() {
            @Override
            public void onSuccess(Object o) {
                isCreateAccount = true;
                queryAccount(AccountConstant.QTYPE_ALL);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (accountInfo == null || accountInfo.basic == null) {
                    queryAccount(AccountConstant.QTYPE_ALL);
                }

                if (errorCode == AccountConstant.RspCode.ERRNO_MOBILE_EMPTY) {
                    isPhoneAvaible = false;
                }
            }
        });

    }

    private void queryAccount(String type) {
        final String qType;
        if (accountInfo == null || accountInfo.basic == null) {
            showLoadingView();
            qType = AccountConstant.QTYPE_ALL;
        } else {
            qType = type;
        }

        LePayAccountManager.getInstance().queryAccount(qType, new LePayCommonCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo info) {
                if (accountInfo == null || accountInfo.basic == null) {
                    hideLoadingView();
                }

                updateAccountInfo(qType, info);

                if (accountInfo == null || accountInfo.basic == null) { //主账户无信息显示
                    showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideBlankPage();
                            queryAccount(AccountConstant.QTYPE_ALL);
                        }
                    });
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (accountInfo == null || accountInfo.basic == null) {
                    hideLoadingView();
                    if (errorCode == AccountConstant.RspCode.ERRNO_NO_NETWORK) {
                        showBlankPage(BlankPage.STATE_NO_NETWORK);
                    }else{
                        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideBlankPage();
                                queryAccount(AccountConstant.QTYPE_ALL);
                            }
                        });
                    }
                }

            }
        });

    }

    //******************** 误删！！！！！！！！！！！！！！！！！！！！！

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

    private void jumpWeb(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Intent intent = new Intent(getActivity(), WalletMainWebActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, url);
        startActivity(intent);
    }

    private void jumpAccountVerify() {
        startActivity(new Intent("com.letv.wallet.accountverify"));
    }

    private boolean isRedirecting = false;

    private void redirect(final String[] jTypes) {
        if (jTypes == null || isRedirecting) {
            return;
        }

        isRedirecting = true;

        LePayAccountManager.getInstance().redirect(jTypes, new LePayCommonCallback<RedirectURL>() {
            @Override
            public void onSuccess(RedirectURL redirect) {
                isRedirecting = false;
                if (redirect != null) {
                    redirectURL = redirect;
                    SharedPreferencesHelper.putLong(MeConstant.SHAREPRE_REDIRECT_TIME, System.currentTimeMillis());
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                isRedirecting = false;
            }
        });
    }

    private void redirectAllType() {
        if (isRedirectExpired()) {
            String[] queryArray = {AccountConstant.JTYPE_LELEHUA_ACTIVE, AccountConstant.JTYPE_LELEHUA_BILL_LIST, AccountConstant.JTYPE_LELEHUA_HOME
                    , AccountConstant.JTYPE_LELEHUA_NOACTIVE};
            redirect(queryArray);
        }
    }

    private boolean isRedirectExpired() {
        return (redirectURL == null) || (System.currentTimeMillis() - SharedPreferencesHelper.getLong(MeConstant.SHAREPRE_REDIRECT_TIME, -1)) >= MeConstant.REDIRECT_CACHE_EXPIRE;
    }

    private boolean isAccountVerifed(){
            return accountInfo != null && accountInfo.basic != null && AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equalsIgnoreCase(accountInfo.basic.verifyStatus);
    }
*/
}

