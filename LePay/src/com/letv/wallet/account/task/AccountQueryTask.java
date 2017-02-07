package com.letv.wallet.account.task;

import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.common.http.beans.BaseResponse;

/**
 * Created by lijunying on 17-1-10.
 */

public class AccountQueryTask extends AccountBaseTask {
    private String mType ;

    private void initParams(String type) {
        this.mType = type;
    }

    public AccountQueryTask(String type, AccountCommonCallback<AccountInfo> callback) {
        super(callback);
        initParams(type);
    }

    public AccountQueryTask(String type, IAccountCallback callback) {
        super(callback);
        initParams(type);
    }

    @Override
    public BaseResponse onExecute() {
        return AccountGateway.queryAccount(mType);
    }

    @Override
    public boolean checkParamsValidate() {
        if (TextUtils.isEmpty(mType)) {
            return false;
        }
        return true;
    }
}
