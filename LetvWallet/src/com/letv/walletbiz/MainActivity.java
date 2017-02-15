package com.letv.walletbiz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.shared.widget.LeLicenceDialog;
import com.letv.shared.widget.LeNeverPermissionRequestDialog;
import com.letv.wallet.account.LePayAccountManager;
import com.letv.wallet.account.LePayCommonCallback;
import com.letv.wallet.account.LePayUtils;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LocationHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.activity.CouponListActivity;
import com.letv.walletbiz.main.FragManager;
import com.letv.walletbiz.main.fragment.RecommendFragment;
import com.letv.walletbiz.main.fragment.WalletFragment;
import com.letv.walletbiz.me.fragment.MeFragment;
import com.letv.walletbiz.order.activity.TotalOrderListActivity;
import com.letv.walletbiz.update.util.UpdateUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseWalletFragmentActivity {
    private static final String TAG = "MainActivity";
    private LeBottomSheet mLoginSheet;
    private AccountHelper accountHelper = AccountHelper.getInstance();

    private LeLicenceDialog mLeLicenceDialog;
    private LeNeverPermissionRequestDialog mLeNeverPermissionRequestDialog = null;
    private static final String WALLET_LICENCE_ACCEPT = "wallet_licence_accept";
    public static final int WALLET_LICENCE_REJECT = 0;
    public static final int WALLET_LICENCE_ACCEPT_ONCE = 1;
    public static final int WALLET_LICENCE_ACCEPT_FOREVER = 2;

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    private boolean displayLicence = false;
    private boolean isRequestingPermissin = false;


    private FragManager fragManager;
    private RecommendFragment recommendFragment;
    private WalletFragment walletFragment;
    private MeFragment myFragment;

    private RadioButton recommendRadio;
    private RadioButton walletRadio;
    private RadioButton myRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.activity_main);
        if (!accountHelper.isLogin(this)) {
            showLoginPrompt(this);
        } else {
            accountHelper.getTokenASync(MainActivity.this);
        }
        fragManager = new FragManager(this);
        recommendFragment = new RecommendFragment();
        walletFragment = new WalletFragment();
        myFragment = new MeFragment();
        Action.uploadStartApp();
        findView();
        parseIntent(getIntent());
        if (hasPermission()) {
            LocationHelper.getInstance().getAddress(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }

    private void findView(){
        recommendRadio = (RadioButton) findViewById(R.id.main_tab_recommend_radio);
        walletRadio = (RadioButton) findViewById(R.id.main_tab_wallet_radio);
        myRadio = (RadioButton) findViewById(R.id.main_tab_my_radio);
    }


    private void parseIntent(Intent intent){
        Uri uri = intent.getData();
        int tabId=-1;
        int serviceId=-1;
        if (uri != null) {
            String main_tab = uri.getQueryParameter("main_tab");
            try {
                tabId = Integer.parseInt(main_tab);
            } catch (NumberFormatException e) {
            }
            String service_id= uri.getQueryParameter("service_id");
            try {
                serviceId = Integer.parseInt(service_id);
            } catch (NumberFormatException e) {
            }
        } else {
            tabId = intent.getIntExtra("main_tab", -1);
            serviceId = intent.getIntExtra("service_id",-1);
        }
        initView(tabId);
        fragManager.getTopFragment().gotoNext(serviceId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isLicenceAccept()) {
            if (SharedPreferencesHelper.getInt(WALLET_LICENCE_ACCEPT, WALLET_LICENCE_REJECT) == WALLET_LICENCE_REJECT) {
                //当用户选择的是拒绝接受条款时，直接弹框提示用户接受
                showLeLicenceDialog();

            } else if (SharedPreferencesHelper.getInt(WALLET_LICENCE_ACCEPT, WALLET_LICENCE_REJECT) == WALLET_LICENCE_ACCEPT_ONCE && displayLicence) {
                //当用户选择当次不再提示,并且已经显示过提示框的时候，不在提示用户接受，直接进入接下来的逻辑
                if (hasPermission()) {
                    fragManager.getTopFragment().startLoadData();
                } else if (!isRequestingPermissin) {
                    checkMainPermission(PERMISSIONS_REQUEST_CODE);
                }
            } else {
                //当用户选择不再提示，并且没有显示过提示框的时候，直接提示用户接受
                showLeLicenceDialog();
            }

        } else {
            if (hasPermission()) {
                fragManager.getTopFragment().startLoadData();
            } else if (!isRequestingPermissin) {
                checkMainPermission(PERMISSIONS_REQUEST_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        List<PermissionInfo> leNeverPermissions = new ArrayList<PermissionInfo>();
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                isRequestingPermissin = false;
                if (grantResults.length > 0) {
                    PackageManager packageManager = getPackageManager();
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            try {
                                PermissionInfo info = packageManager.getPermissionInfo(permissions[i], 0);
                                leNeverPermissions.add(info);
                            } catch (NameNotFoundException e) {
                            }
                        }
                    }
                }
                if (leNeverPermissions.size() <= 0) {
                    UpdateUtil.mIsStartedNewly = true;
                    mUpdateHelper.requreyVersion();
                    //initLoadData();
                    fragManager.getTopFragment().startLoadData();
                } else {
                    showLeNeverPermissionRequestDialog(leNeverPermissions);
                }
                break;
            }
        }
    }

    private boolean hasPermission() {
        String[] permissionList = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        boolean result = true;
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        return result;
    }

    public boolean checkMainPermission(int requestCode) {
        boolean result = true;
        String[] permissionList = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ArrayList<String> deniedPermissionList = new ArrayList<String>();
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
                deniedPermissionList.add(permission);
            }
        }
        if (deniedPermissionList.size() > 0) {
            isRequestingPermissin = true;
            ActivityCompat.requestPermissions(this, deniedPermissionList.toArray(new String[0]), requestCode);
        }
        return result;
    }

    private void showLeNeverPermissionRequestDialog(List<PermissionInfo> leNeverPermissions) {
        if (mLeNeverPermissionRequestDialog == null) {
            try {
                mLeNeverPermissionRequestDialog = new LeNeverPermissionRequestDialog(this, leNeverPermissions, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLeNeverPermissionRequestDialog.disappear();
                        mLeNeverPermissionRequestDialog = null;
                        finish();
                    }
                });
                mLeNeverPermissionRequestDialog.setCancelable(false);
            } catch (Exception e) {
            }
        }
        mLeNeverPermissionRequestDialog.appear();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onDestroy() {
        Action.uploadStopApp();
        ExecutorHelper.getExecutor().clearAllRunnable();
        if (mLeLicenceDialog != null) {
            mLeLicenceDialog.dismiss();
        }

        if (mLoginSheet != null && mLoginSheet.isShowing()) {
            mLoginSheet.dismiss();
        }

        super.onDestroy();
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        fragManager.getTopFragment().onNetWorkChanged(isNetworkAvailable);
    }
    private void initView(int type) {

        switch(type){
            case 1:
                recommendRadio.setChecked(true);
                fragManager.showFragmentAdd(R.id.main_fragment_container, recommendFragment);
                break;
            case 2:
                myRadio.setChecked(true);
                fragManager.showFragmentAdd(R.id.main_fragment_container, myFragment);
                break;
            default:
                walletRadio.setChecked(true);
                fragManager.showFragmentAdd(R.id.main_fragment_container, walletFragment);
                break;
        }

        recommendRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    recommendFragment.setInitData(true);
                    fragManager.showFragmentAdd(R.id.main_fragment_container, recommendFragment);
                }

            }

        });
        walletRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    walletFragment.setInitData(true);
                    fragManager.showFragmentAdd(R.id.main_fragment_container, walletFragment);
                }

            }

        });
        myRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    myFragment.setInitData(true);
                    fragManager.showFragmentAdd(R.id.main_fragment_container, myFragment);
                }
            }

        });
    }

    private boolean isLicenceAccept() {
        return SharedPreferencesHelper.getInt(WALLET_LICENCE_ACCEPT, WALLET_LICENCE_REJECT) == WALLET_LICENCE_ACCEPT_FOREVER;
    }

    private void showLeLicenceDialog() {
        if (isLicenceAccept()) {
            return;
        }

        if (mLeLicenceDialog != null) {
            mLeLicenceDialog.dismiss();
        }
        mLeLicenceDialog = new LeLicenceDialog(this, getString(R.string.app_name),
                LeLicenceDialog.TYPE_USER_PRIVACY_CONTACTS_LOCATION_NET)
                .setLeLicenceDialogClickListener(new LeLicenceDialog.LeLicenceDialogClickListener() {

                    @Override
                    public void onClickListener(LeLicenceDialog.KEY key) {
                        switch (key) {
                            case BTN_AGREE:
                                acceptLicense();
                                mUpdateHelper.startUpgradeService();
                                break;
                            case BTN_CANCEL:
                                rejectLicense();
                                break;
                            case OUTSIDE:
                                finish();
                                break;
                        }
                    }
                }).show();
    }

    private void acceptLicense() {
        if (mLeLicenceDialog != null) {
            SharedPreferencesHelper.putInt(WALLET_LICENCE_ACCEPT,
                    mLeLicenceDialog.isChecked() ? WALLET_LICENCE_ACCEPT_FOREVER : WALLET_LICENCE_ACCEPT_ONCE);
            displayLicence = mLeLicenceDialog.isChecked() ? false : true;
            mLeLicenceDialog.dismiss();
            if (!hasPermission()) {
                checkMainPermission(PERMISSIONS_REQUEST_CODE);
            } else {
                fragManager.getTopFragment().startLoadData();
                //initLoadData();
            }
        }
    }

    private void rejectLicense() {
        SharedPreferencesHelper.remove(WALLET_LICENCE_ACCEPT);
        finish();
    }

    private void showLoginPrompt(Context context) {
        mLoginSheet = new LeBottomSheet(context);
        String contextString = context.getResources().getString(
                R.string.wallet_prompt_login_title);
        mLoginSheet.setStyle(
                LeBottomSheet.BUTTON_DEFAULT_STYLE,
                loginListener,
                cancelListener,
                null,
                new String[]{
                        context.getString(R.string.wallet_prompt_login_toLogin),
                        context.getString(R.string.wallet_prompt_login_cancel)
                }, null,
                contextString, null,
                context.getResources().getColor(R.color.colorBtnBlue),
                false);
        mLoginSheet.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_bills) {
            goToTotalOrderList();
        } else if (item.getItemId() == R.id.action_coupon) {
            Action.uploadExposeTab(Action.WALLET_HOME_COUPON);
            Intent intent = new Intent(this, CouponListActivity.class);
            intent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_ICON);
            startActivity(intent);


        }
        return super.onOptionsItemSelected(item);
    }

    private void goToTotalOrderList() {
        Action.uploadExposeTab(Action.WALLET_HOME_TOTALORDER);
        Intent intent = new Intent(this, TotalOrderListActivity.class);
        startActivity(intent);
    }

    private View.OnClickListener loginListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mLoginSheet.dismiss();
            AccountHelper accountHelper = AccountHelper.getInstance();
            accountHelper.addAccount(MainActivity.this, null);
        }
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mLoginSheet.dismiss();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean canStartUpgradeService() {
        if (!isLicenceAccept()) {
            return false;
        }
        return true;
    }

    private boolean ACCOUNT_FAIL_REASON_PHONE_NULL = false;
    private boolean hasCreateAccount = false;
    private boolean hasVerifyAccount = false;

    public void loadAccountData() {
        if (AccountHelper.getInstance().isLogin(this) && NetworkHelper.isNetworkAvailable()) {
            String qType = null;
            if (checkCreateAccount(false) && checkVerifyAccount()) { //用户已开户并已实名， 直接查询卡列表
                qType = AccountConstant.QTYPE_CARD;
            } else {
                qType = AccountConstant.QTYPE_ALL; //查询用户状态
            }
            queryAccount(qType);
        }
    }

    private boolean checkCreateAccount(boolean isForceCreate){
        hasCreateAccount = LePayAccountManager.hasCreatedAccount();
        if (!hasCreateAccount && isForceCreate) {
            LePayAccountManager.getInstance().createAccount(null); //默认开一次户
        }
        return hasCreateAccount;
    }

    private boolean checkVerifyAccount(){
        hasVerifyAccount = LePayAccountManager.hasVerifyAccount();
        return hasVerifyAccount;
    }

    private void queryAccount(final String qType){
        LePayAccountManager.getInstance().queryAccount(qType, new LePayCommonCallback<AccountInfo>() {
            @Override
            public void onSuccess(AccountInfo accountInfo) {
                if (checkCreateAccount(true)) {
                    // set card list = cardlist.size();

                   /* // 跳转卡列表
                    Intent intent = new Intent("com.letv.wallet.cardlist");
                    intent.putExtra("LePayCardBinInfo", accountInfo.cardList);*/


                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }


}
