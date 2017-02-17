package com.letv.wallet.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.LeCheckBox;
import com.letv.shared.widget.LeLoadingDialog;
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
import com.letv.wallet.account.utils.ActionUtils;
import com.letv.wallet.account.utils.AgreementUrlSpan;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;

/**
 * Created by lijunying on 17-2-6.
 */

public class AccountVerifyActivity extends BaseFragmentActivity implements View.OnClickListener, EditTextActionCallback, AccountHelper.OnAccountChangedListener {
    private RealNameEditText editRealName;
    private IdNoEditText editIdNum;
    private CardEditText editCardNum;
    private PhoneEditText editPhone;
    private SmsCodeEditText editSmsCode;
    private CountDownView tvGetSmsCode;
    private LeCheckBox checkAgreement;

    private TextView btnOk;

    private SendMsgTask sendMsgTask;
    private AccountVerifyTask verifyAccountTask;

    private String from ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AccountHelper.getInstance().isLogin(this)) {
            finish(); //未登录返回
        }
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        setContentView(R.layout.account_verify_activity);
        initView();

        Action.uploadExpose(Action.ACCOUNT_VERIFY_PAGE_EXPOSE, (from = ActionUtils.getFromExtra(getIntent())));

    }

    private void initView() {
        editRealName = (RealNameEditText) findViewById(R.id.editRealName);
        editRealName.setCallback(this);
        editIdNum = (IdNoEditText) findViewById(R.id.editIdNo);
        editIdNum.setCallback(this);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tv = (TextView) findViewById(R.id.tvAgreement);
        tv.setText(getClickableSpan());
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString getClickableSpan() {
        String agrementWrap = getString(R.string.account_agreement_tips);
        int start = agrementWrap.indexOf("%1$s");
        String agrement = getString(R.string.account_agreement);
        SpannableString spannableString = new SpannableString(String.format(agrementWrap, agrement));
        spannableString.setSpan(new AgreementUrlSpan(AccountCommonConstant.URL_LEPAY_AGREEMENT), start, start + agrement.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                }else{
                    editPhone.setFocusable(true);
                }
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
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    LogHelper.e("sendSmsCode onError : errorCode = " + errorCode + " errorMsg = " + errorMsg);
                    sendMsgTask = null;
                }

                @Override
                public void onNoNet() {
                    Toast.makeText(PayApplication.getApplication(), R.string.empty_view_no_network, Toast.LENGTH_SHORT).show();
                    sendMsgTask = null;
                    tvGetSmsCode.cancle();
                }
            });
            editSmsCode.setError(null);
            tvGetSmsCode.startTick();
            ExecutorHelper.getExecutor().runnableExecutor(sendMsgTask);
        }
    }

    private void verifyAccount(String accountName, String identityNum, String bankNo, String mobile, String msgCode) {
        if (verifyAccountTask == null) {
            verifyAccountTask = new AccountVerifyTask(accountName, identityNum, bankNo, mobile, msgCode, new AccountCommonCallback() {
                @Override
                public void onSuccess(Object result) {
                    Action.uploadCustom(Action.EVENT_TYPE_VERIFY, Action.ACCOUNT_VERIFY_PAGE_VERIFY);
                    verifyAccountTask = null;
                    hideVerifyDialog();
                    setResult(RESULT_OK);
                    finish();
                    LogHelper.e("verifyAccount onSuccess");
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    verifyAccountTask = null;
                    hideVerifyDialog();
                    LogHelper.e("verifyAccount onError errorCode = " + errorCode +" errorMsg = " +errorMsg);
                    if (errorCode == AccountConstant.RspCode.ERRNO_MSG_CODE_FAILED) {
                        editSmsCode.setError(getString(R.string.account_verify_sms_invalid));
                        return;
                    } else if (errorCode != AccountConstant.RspCode.ERRNO_USER &&  errorCode != AccountConstant.RspCode.ERRNO_USER_AUTH_FAILED) {
                        errorMsg = getString(R.string.account_verify_fail);
                    }
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
     * @param editText
     */
    @Override
    public void onNumberChanged(EditText editText) {
        boolean isShowBtn = false;
        switch (editText.getId()) {
            case R.id.editRealName:
                LogHelper.e("editRealName");
                if (editIdNum.isIdValidate() && editCardNum.isCardValidate() && editPhone.isPhoneValidate() && editSmsCode.isSmsCodeValidate()) {
                    isShowBtn = true;
                }
                break;
            case R.id.editIdNo:
                LogHelper.e("editIdNo");
                if (editRealName.isNameValidate() && editCardNum.isCardValidate() && editPhone.isPhoneValidate() && editSmsCode.isSmsCodeValidate()) {
                    isShowBtn = true;
                }
                break;
            case R.id.editCardNum:
                LogHelper.e("editCardNum");
                if (editIdNum.isIdValidate() && editRealName.isNameValidate() && editPhone.isPhoneValidate() && editSmsCode.isSmsCodeValidate()) {
                    isShowBtn = true;
                }
                break;
            case R.id.editPhone:
                LogHelper.e("editPhone");
                if (editIdNum.isIdValidate() && editCardNum.isCardValidate() && editRealName.isNameValidate() && editSmsCode.isSmsCodeValidate()) {
                    isShowBtn = true;
                }
                break;
            case R.id.editSmsCode:
                LogHelper.e("editSmsCode");
                if (editIdNum.isIdValidate() && editCardNum.isCardValidate() && editPhone.isPhoneValidate() && editRealName.isNameValidate()) {
                    isShowBtn = true;
                }
                break;
        }
        btnOk.setEnabled(isShowBtn);
    }

    private void checkAllInput() {
        //检测所有的edittext,并显示错误提示
        if (!editRealName.checkValidateWithError() || !editIdNum.checkValidateWithError() || !editCardNum.checkValidateWithError() || !editPhone.checkValidateWithError()
                || !editSmsCode.checkValidateWithError()) {
            return;
        }
        if (checkAgreement.isChecked()) {
            showVefifyDialog();
            checkBankCardNet(editCardNum.getCardNum());
        }else {
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
                verifyAccount(editRealName.getText().toString().trim(), editIdNum.getIdNum(), editCardNum.getCardNum(), editPhone.getPhone(), editSmsCode.getText().toString().trim());
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                hideVerifyDialog();
                String msg = null;

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
    public void onAccountLogin() {

    }

    @Override
    public void onAccountLogout() {
        finish(); //账户登出 返回上界面
    }
}
