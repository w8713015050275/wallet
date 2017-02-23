package com.letv.wallet.account.task;


import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DigestUtils;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;

/**
 * Created by lijunying on 16-12-26.
 * 账户开户接口
 */

public class AccountCreateTask extends AccountBaseTask {


    public AccountCreateTask(IAccountCallback callback) {
        super(callback);
    }

    public AccountCreateTask(AccountCommonCallback callback) {
        super(callback);
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(AccountHelper.getInstance().getPhone())) {
            onError(AccountConstant.RspCode.ERRNO_MOBILE_EMPTY, null);
            return;
        }
        super.run();
    }

    @Override
    public BaseResponse onExecute() {
        BaseResponse response = AccountGateway.createAcount();
        if (response != null && response.errno == AccountConstant.RspCode.SUCCESS) {
            AccountUtils.updateCreatedAccountStatus();
        }
        return response;
    }

    @Override
    public boolean checkParamsValidate() {
        return true;
    }


}
