package com.letv.wallet.account.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.wallet.PayApplication;
import com.letv.wallet.R;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.AccountVerifyTask;
import com.letv.wallet.account.task.SendMsgTask;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;

/**
 * Created by lijunying on 17-2-6.
 */

public class AccountVerifyActivity extends BaseFragmentActivity implements View.OnClickListener {
    private TextView tvAvailableBankList;
    private TextView tvGetSmsCode;
    private EditText editSmsCode;

    private SendMsgTask sendMsgTask;
    private AccountVerifyTask verifyAccountTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_verify_activity);
        initView();
    }

    private void initView() {
        tvAvailableBankList = (TextView) findViewById(R.id.tvAvailableBankList);

        editSmsCode = (EditText) findViewById(R.id.editSmsCode);
        tvGetSmsCode = (TextView) findViewById(R.id.tvGetSmsCode);

        tvAvailableBankList.setOnClickListener(this);
        tvGetSmsCode.setOnClickListener(this);
    }


    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    public void onClick(View v) {
        LogHelper.e("onClick");
        switch (v.getId()) {
            case R.id.tvAvailableBankList:
                verifyAccount("时荣胜", " 371482198105189151", "4367421217054374228", "13552079861", editSmsCode.getText().toString().trim());
                break;

            case R.id.tvGetSmsCode:
                sendSmsCode("13552079861");
                break;
        }
    }

    private void sendSmsCode(String mobileNo) {
        LogHelper.e("sendSmsCode");
        if (TextUtils.isEmpty(mobileNo)) {
            return;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            Toast.makeText(PayApplication.getApplication(), R.string.empty_no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        if (sendMsgTask == null) {
            sendMsgTask = new SendMsgTask(mobileNo, AccountConstant.SENDMSG_TEMP_APPLY_CERT, new AccountCommonCallback() {
                @Override
                public void onSuccess(Object result) {
                   LogHelper.e("onSuccess");
                    sendMsgTask = null;
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    LogHelper.e("onError");
                    sendMsgTask = null;
                }

                @Override
                public void onNoNet() {
                    Toast.makeText(PayApplication.getApplication(), R.string.empty_no_network, Toast.LENGTH_SHORT).show();
                    sendMsgTask = null;
                }
            });
            ExecutorHelper.getExecutor().runnableExecutor(sendMsgTask);
        }
    }

    private void verifyAccount(String accountName, String identityNum, String bankNo, String mobile, String msgCode) {
        if (!NetworkHelper.isNetworkAvailable()) {
            Toast.makeText(PayApplication.getApplication(), R.string.empty_no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        if (verifyAccountTask == null) {
            verifyAccountTask = new AccountVerifyTask(accountName, identityNum, bankNo, mobile, msgCode , new AccountCommonCallback() {
                @Override
                public void onSuccess(Object result) {
                    LogHelper.e("onSuccess");
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    LogHelper.e("onError " + errorCode + " : " + errorMsg);
                }

                @Override
                public void onNoNet() {
                    Toast.makeText(PayApplication.getApplication(), R.string.empty_no_network, Toast.LENGTH_SHORT).show();
                }
            });

            ExecutorHelper.getExecutor().runnableExecutor((verifyAccountTask));
        }

    }


}
