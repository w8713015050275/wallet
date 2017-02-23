package com.letv.wallet.account.task;

import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.common.http.beans.BaseResponse;

/**
 * Created by lijunying on 17-1-10.
 */

public class AccountVerifyTask extends AccountBaseTask{

    private String accountName;
    private String identityNum;
    private String bankNo;
    private String mobile;
    private String msgCode ;

    private void initParams(String accountName, String identityNum, String bankNo, String mobile, String msgCode) {
        this.accountName = accountName;
        this.identityNum = identityNum;
        this.bankNo = bankNo;
        this.mobile = mobile;
        this.msgCode = msgCode;
    }

    public AccountVerifyTask(String accountName, String identityNum, String bankNo, String mobile, String msgCode, AccountCommonCallback localCallback) {
        super(localCallback);
        initParams(accountName, identityNum, bankNo, mobile, msgCode);
    }

    public AccountVerifyTask(String accountName, String identityNum, String bankNo, String mobile, String msgCode, IAccountCallback callback ){
        super(callback);
        initParams(accountName, identityNum, bankNo, mobile, msgCode);
    }

    @Override
    public BaseResponse onExecute() {
        BaseResponse response = AccountGateway.verifyAccount(accountName, identityNum, bankNo, mobile, msgCode);
        if (response != null && response.errno == AccountConstant.RspCode.SUCCESS) {
            AccountUtils.updateVerifyAccountStatus();
        }
        return response;
    }

    @Override
    public boolean checkParamsValidate() {
        if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(identityNum) || TextUtils.isEmpty(bankNo) || TextUtils.isEmpty(mobile)
                || TextUtils.isEmpty(msgCode)) {
            return false;
        }
        return true;
    }


}
