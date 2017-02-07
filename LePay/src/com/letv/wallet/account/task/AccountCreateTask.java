package com.letv.wallet.account.task;


import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DigestUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;

import org.w3c.dom.Text;

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
        if (!NetworkHelper.isNetworkAvailable()) {
            onNoNetWork();
            return;
        }
        if (TextUtils.isEmpty(AccountHelper.getInstance().getPhone())) {
            onError(AccountConstant.RspCode.ERRNO_MOBILE_EMPTY, null);
            return;
        }

        BaseResponse response = onExecute();
        if (response == null) {
            onError(AccountConstant.RspCode.ERROR_NETWORK, null);
            return;
        }

        if (response.errno == AccountConstant.RspCode.SUCCESS) {
            SharedPreferencesHelper.putBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid()), true);
            onSuccess(response.data);
        } else {
            onError(response.errno, response.errmsg);
        }

    }

    @Override
    public BaseResponse onExecute() {
        return AccountGateway.createAcount();
    }

    @Override
    public boolean checkParamsValidate() {
        return true;
    }


}
