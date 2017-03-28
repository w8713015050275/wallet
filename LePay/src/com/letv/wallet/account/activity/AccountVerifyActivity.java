package com.letv.wallet.account.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.LeCheckBox;
import com.letv.shared.widget.LeLoadingDialog;
import com.letv.tracker2.enums.Key;
import com.letv.wallet.PayApplication;
import com.letv.wallet.R;
import com.letv.wallet.account.AccountCommonConstant;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.CardbinAvailableInfo;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.AccountVerifyTask;
import com.letv.wallet.account.task.CardbinTask;
import com.letv.wallet.account.task.SendMsgTask;
import com.letv.wallet.account.ui.CardEditText;
import com.letv.wallet.account.ui.CountDownView;
import com.letv.wallet.account.ui.EditTextActionCallback;
import com.letv.wallet.account.ui.IdNoEditText;
import com.letv.wallet.account.ui.PhoneEditText;
import com.letv.wallet.account.ui.RealNameEditText;
import com.letv.wallet.account.ui.SmsCodeEditText;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.account.utils.ActionUtils;
import com.letv.wallet.account.utils.AgreementUrlSpan;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lijunying on 17-2-6.
 */

public class AccountVerifyActivity extends BaseFragmentActivity implements View.OnClickListener, EditTextActionCallback, AccountHelper.OnAccountChangedListener {
    private RealNameEditText editRealName;
    private IdNoEditText editIdNo;
    private CardEditText editCardNum;
    private PhoneEditText editPhone;
    private SmsCodeEditText editSmsCode;
    private CountDownView tvGetSmsCode;
    private LeCheckBox checkAgreement;
    private TextView tvAgreement;

    private TextView btnOk;

    private SendMsgTask sendMsgTask;
    private AccountVerifyTask verifyAccountTask;

    private String from;
    private boolean hasNavigation = false;

    public static final String EXTRA_ACTION_BAR_HAS_NAVIGATION= "hasNavigationIcon";
    public static final String EXTRA_ACTION_BAR_TITLE_COLOR = "titleTextColor";
    public static final String EXTRA_ACTION_BAR_BACKGROUND_COLOR = "colorActionBar";
    public static final String EXTRA_STATUS_BAR_BACKGROUND_COLOR = "colorStatusBar";
    public static final String EXTRA_STATUS_BAR_ICON_COLOR = "colorStatusBarIcon";
    public static final String EXTRA_MENU_HAS_SKIP = "allowSkip";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AccountHelper.getInstance().isLogin(this)) {
            finish(); //未登录返回
        }
        int colorStatusBarIcon = getIntent().getIntExtra(EXTRA_STATUS_BAR_ICON_COLOR, 0);
        if (colorStatusBarIcon == 1) {
            setTheme(R.style.LeEcoTheme);
        }
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        setContentView(R.layout.account_verify_activity);
        initView();

        Action.uploadExpose(Action.ACCOUNT_VERIFY_PAGE_EXPOSE, (from = ActionUtils.getFromExtra(getIntent())));

    }

    private void initView() {
        initActionBar();
        editRealName = (RealNameEditText) findViewById(R.id.editRealName);
        editRealName.setCallback(this);
        editIdNo = (IdNoEditText) findViewById(R.id.editIdNo);
        editIdNo.setCallback(this);
        editCardNum = (CardEditText) findViewById(R.id.editCardNum);
        editCardNum.setCallback(this);
        editPhone = (PhoneEditText) findViewById(R.id.editPhone);
        editPhone.setCallback(this);
        editSmsCode = (SmsCodeEditText) findViewById(R.id.editSmsCode);
        editSmsCode.setCallback(this);
        tvGetSmsCode = (CountDownView) findViewById(R.id.tvGetSmsCode);
        checkAgreement = (LeCheckBox) findViewById(R.id.checkAgreement);
        btnOk = (TextView) findViewById(R.id.btnOk);

        findViewById(R.id.tvAvailableBankList).setOnClickListener(this);
        tvGetSmsCode.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        tvAgreement = (TextView) findViewById(R.id.tvAgreement);
        SpannableString str = getClickableSpan();
        if (str == null) {
            tvAgreement.setVisibility(View.GONE);
            return;
        }
        tvAgreement.setVisibility(View.VISIBLE);
        tvAgreement.setText(str);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initActionBar(){
        Toolbar toolbar = getToolbar();
        Drawable iconNavigation = null;
        if (getIntent().getBooleanExtra(EXTRA_ACTION_BAR_HAS_NAVIGATION, true)) {
            iconNavigation = getDrawable(R.drawable.ic_wallet_action_back);
            hasNavigation = true;
        }
        toolbar.setNavigationIcon(iconNavigation);
        int titleColor = parseColor(getIntent().getStringExtra(EXTRA_ACTION_BAR_TITLE_COLOR));
        toolbar.setTitleTextColor( titleColor == -1 ? getColor(R.color.wallet_actionBar_titleTv_color) : titleColor);
        int colorActionBar = parseColor(getIntent().getStringExtra(EXTRA_ACTION_BAR_BACKGROUND_COLOR));
        toolbar.setBackgroundColor(colorActionBar == -1 ? getColor(R.color.wallet_statusbar_color) : colorActionBar);
        int colorStatusBar = parseColor(getIntent().getStringExtra(EXTRA_STATUS_BAR_BACKGROUND_COLOR));
        getWindow().setStatusBarColor(colorStatusBar == -1 ? getColor(R.color.wallet_statusbar_color) : colorStatusBar);
        if (getIntent().getBooleanExtra(EXTRA_MENU_HAS_SKIP, false)) {
            showMenu().setTextColor(titleColor == -1 ? getColor(R.color.wallet_actionBar_titleTv_color) : titleColor);
        }
    }

    private TextView showMenu(){
        TextView menu = new TextView(this, null, android.support.v7.appcompat.R.attr.actionOverflowMenuStyle);
        menu.setSingleLine();
        menu.setEllipsize(TextUtils.TruncateAt.END);
        menu.setTextAppearance(R.style.TextAppearance_Eui_Widget_ActionBar_Menu);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        menu.setLayoutParams(layoutParams);
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        layoutParams.rightMargin = (int) DensityUtils.dip2px(10);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        menu.setText(R.string.account_verify_menu_skip);
        getToolbar().addView(menu, layoutParams);
        return menu;
    }

    private int parseColor(String strColor){
        int color = -1;
        if (!TextUtils.isEmpty(strColor)) {
            try {
                color = Color.parseColor(strColor);
            } catch (IllegalArgumentException e) {
            }
        }
        return color;
    }

    @Override
    protected void onStart() {
        super.onStart();
        AccountUtils.checkRedirectExpired();
    }

    private static final String SYMBOL_AND = "&";
    private SpannableString getClickableSpan() {
        String agrementWrap = getString(R.string.account_agreement_tips);
        int start = agrementWrap.indexOf("%1$s");
        if (start < 0) {
            return null;
        }
        String agrementLeFinancial = getString(R.string.account_agreement_le_financial);
        String agrementLePayChannel = getString(R.string.account_agreement_le_paychannel);
        String agrement = agrementLeFinancial + SYMBOL_AND + agrementLePayChannel;
        SpannableString spannableString = new SpannableString(String.format(agrementWrap, agrement));
        spannableString.setSpan(new AgreementUrlSpan(AccountCommonConstant.URL_LE_FINANCIAL_AGREEMENT), start, start + agrementLeFinancial.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new AgreementUrlSpan(AccountCommonConstant.URL_LE_PAY_CHANNEL_AGREEMENT), start + agrement.length()- agrementLePayChannel.length(), start + agrement.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOk:
                checkAllInput();
                break;
            case R.id.tvAvailableBankList:
                Intent intent = new Intent(this, AccountWebActivity.class);
                intent.putExtra(CommonConstants.EXTRA_URL, AccountCommonConstant.URL_AVAILABLE_BANK_LIST);
                startActivity(intent);
                break;

            case R.id.tvGetSmsCode:
                if (editPhone.checkValidateWithError()) {
                    sendSmsCode(editPhone.getPhone());
                    return;
                }
                Toast.makeText(getApplication(), R.string.account_verify_phone_invalid, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void sendSmsCode(String mobileNo) {
        if (!NetworkHelper.isNetworkAvailable()) {
            Toast.makeText(PayApplication.getApplication(), R.string.empty_view_no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        if (sendMsgTask == null) {
            sendMsgTask = new SendMsgTask(mobileNo, AccountConstant.SENDMSG_TEMP_APPLY_CERT, new AccountCommonCallback() {
                @Override
                public void onSuccess(Object result) {
                    sendMsgTask = null;
                    tvGetSmsCode.startTick();
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    sendMsgTask = null;
                    if (errorCode == AccountConstant.RspCode.ERRNO_USER_AUTH_FAILED) {
                        Toast.makeText(PayApplication.getApplication(), R.string.account_login_expired, Toast.LENGTH_SHORT).show();
                        finish(); //token过期，返回上一级；
                        return;
                    }
                    if (errorCode != AccountConstant.RspCode.ERRNO_SEND_MSG_FAILED) {
                        LogHelper.e("sendSmsCode onError : errorCode = " + errorCode + " errorMsg = " + errorMsg); //异常情况
                    }
                    Toast.makeText(PayApplication.getApplication(), R.string.account_verify_sms_send_fail, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNoNet() {
                    Toast.makeText(PayApplication.getApplication(), R.string.empty_view_no_network, Toast.LENGTH_SHORT).show();
                    sendMsgTask = null;
                    tvGetSmsCode.cancle();
                }
            });
            editSmsCode.setError(null);
            ExecutorHelper.getExecutor().runnableExecutor(sendMsgTask);
        }
    }

    private void checkSetPwd(){
        if (!TextUtils.isEmpty(from) && from.startsWith("com.letv") && !from.contains(getPackageName())) { //from 从其他app跳转过来，不进入设置密码
             return;
        }
        AccountUtils.goToAccountWeb(AccountVerifyActivity.this, AccountConstant.JTYPE_SET_PAY_PWD);

    }


    private void verifyAccount(String accountName, String identityNum, String bankNo, String mobile, String msgCode) {
        if (verifyAccountTask == null) {
            verifyAccountTask = new AccountVerifyTask(accountName, identityNum, bankNo, mobile, msgCode, new AccountCommonCallback() {
                @Override
                public void onSuccess(Object result) {
                    Toast.makeText(PayApplication.getApplication(), R.string.account_verify_success, Toast.LENGTH_SHORT).show();
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.put(Key.From.getKeyId(), from);
                    Action.uploadCustom(Action.EVENT_TYPE_VERIFY, Action.ACCOUNT_VERIFY_PAGE_VERIFY, props);
                    verifyAccountTask = null;
                    hideVerifyDialog();
                    setResult(RESULT_OK);
                    checkSetPwd();
                    finish();
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.put(Key.From.getKeyId(), from);
                    Action.uploadCustom(Action.EVENT_TYPE_FAIL, Action.ACCOUNT_VERIFY_PAGE_FAIL, props);

                    verifyAccountTask = null;
                    hideVerifyDialog();
                    if (errorCode == AccountConstant.RspCode.ERRNO_USER_AUTH_FAILED) {
                        Toast.makeText(PayApplication.getApplication(), R.string.account_login_expired, Toast.LENGTH_SHORT).show();
                        finish(); //token过期，返回上一级；
                        return;
                    }
                    if (errorCode == AccountConstant.RspCode.ERRNO_MSG_CODE_FAILED) {
                        editSmsCode.setError(getString(R.string.account_verify_sms_invalid));
                        return;
                    } else if (errorCode != AccountConstant.RspCode.ERRNO_USER) {
                        errorMsg = getString(R.string.account_verify_fail);
                    }
                    editSmsCode.getEditText().setText("");
                    editSmsCode.setError(null);
                    tvGetSmsCode.cancle();
                    Toast.makeText(PayApplication.getApplication(), errorMsg, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNoNet() {
                    verifyAccountTask = null;
                    hideVerifyDialog();
                    Toast.makeText(PayApplication.getApplication(), R.string.empty_view_no_network, Toast.LENGTH_SHORT).show();
                }
            });
            ExecutorHelper.getExecutor().runnableExecutor((verifyAccountTask));
        }

    }


    /**
     * focus 当前edittext， 过去其他输入框状态，都合法显示 button
     *
     * @param inputLayout
     */
    @Override
    public void onNumberChanged(TextInputLayout inputLayout) {
        boolean isShowBtn = false;
        switch (inputLayout.getId()) {
            case R.id.editRealName:
                if (editIdNo.isIdValidate() && editCardNum.isCardValidate() && editPhone.isPhoneValidate() && editSmsCode.isSmsCodeValidate()) {
                    isShowBtn = true;
                }
                break;
            case R.id.editIdNo:
                if (editRealName.isNameValidate() && editCardNum.isCardValidate() && editPhone.isPhoneValidate() && editSmsCode.isSmsCodeValidate()) {
                    isShowBtn = true;
                }
                break;
            case R.id.editCardNum:
                if (editIdNo.isIdValidate() && editRealName.isNameValidate() && editPhone.isPhoneValidate() && editSmsCode.isSmsCodeValidate()) {

                    isShowBtn = true;
                }
                break;
            case R.id.editPhone:
                if (editIdNo.isIdValidate() && editCardNum.isCardValidate() && editRealName.isNameValidate() && editSmsCode.isSmsCodeValidate()) {
                    isShowBtn = true;
                }
                break;
            case R.id.editSmsCode:
                if (editIdNo.isIdValidate() && editCardNum.isCardValidate() && editPhone.isPhoneValidate() && editRealName.isNameValidate()) {
                    isShowBtn = true;
                }
                break;
        }
        btnOk.setEnabled(isShowBtn);
    }

    private void checkAllInput() {
        //检测所有的edittext,并显示错误提示
        if (!editRealName.checkValidateWithError() || !editIdNo.checkValidateWithError() || !editCardNum.checkValidateWithError() || !editPhone.checkValidateWithError()
                || !editSmsCode.checkValidateWithError()) {
            return;
        }
        if (checkAgreement.isChecked()) {
            showVefifyDialog();
            checkBankCardNet(editCardNum.getCardNum());
        } else {
            Toast.makeText(PayApplication.getApplication(), R.string.account_verify_please_check_agreement, Toast.LENGTH_SHORT).show();
        }

    }

    private LeLoadingDialog mVerifyDialog;

    private static final int THEME_WHITE_BG = 1;
    private static final int THEME_TRANSPARENT_BG = 0;
    private static final int CONTENT_VIEW_SIZE = 48;


    private void showVefifyDialog() {
        if (mVerifyDialog == null) {
            mVerifyDialog = new LeLoadingDialog(this, THEME_TRANSPARENT_BG, CONTENT_VIEW_SIZE);
            mVerifyDialog.setCancelable(false);
        }
        mVerifyDialog.show();
    }

    private void hideVerifyDialog() {
        if (mVerifyDialog != null && mVerifyDialog.isShowing()) {
            mVerifyDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (mVerifyDialog != null) {
            mVerifyDialog.onDismissDialog4DestroyContext();
        }
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        super.onDestroy();
    }

    public void checkBankCardNet(String cardNum) {
        ExecutorHelper.getExecutor().runnableExecutor(new CardbinTask(cardNum, new AccountCommonCallback<CardbinAvailableInfo>() {
            @Override
            public void onSuccess(CardbinAvailableInfo result) {
                verifyAccount(editRealName.getText().toString().trim(), editIdNo.getIdNum(), editCardNum.getCardNum(), editPhone.getPhone(), editSmsCode.getEditText().getText().toString().trim());
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                hideVerifyDialog();
                String msg = null;

                if (errorCode == AccountConstant.RspCode.ERRNO_USER_AUTH_FAILED) {
                    Toast.makeText(PayApplication.getApplication(), R.string.account_login_expired, Toast.LENGTH_SHORT).show();
                    finish(); //token过期，返回上一级；
                    return;
                }

                switch (errorCode) {
                    case AccountConstant.RspCode.ERRNO_BANK_CARD_ERRO:
                        msg = getResources().getString(R.string.account_verify_card_bank_not_support);
                        break;
                    case AccountConstant.RspCode.ERRNO_USER:
                        msg = errorMsg;
                        break;
                }

                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(PayApplication.getApplication(), R.string.empty_network_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                editCardNum.setError(msg);
            }

            @Override
            public void onNoNet() {
                hideVerifyDialog();
                Toast.makeText(AccountVerifyActivity.this, R.string.empty_view_no_network, Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    public void onBackPressed() {
        if (hasNavigation) {
            super.onBackPressed();
        }
    }

    @Override
    public void onAccountLogin() {

    }

    @Override
    public void onAccountLogout() {
        finish(); //账户登出 返回上界面
    }
}
