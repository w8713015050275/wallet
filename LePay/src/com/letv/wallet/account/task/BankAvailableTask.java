package com.letv.wallet.account.task;

import com.letv.wallet.account.aidl.v1.BankAvailableInfo;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.common.http.beans.BaseResponse;

/**
 * Created by lijunying on 16-12-26.
 *  查询支持的银行卡列表
 */

public class BankAvailableTask extends AccountBaseTask {

    public BankAvailableTask(AccountCommonCallback<BankAvailableInfo> callback) {
        super(callback);
    }

    public BankAvailableTask(IAccountCallback callback) {
        super(callback);
    }

    @Override
    public BaseResponse onExecute() {
        return AccountGateway.availableBank();
    }

    @Override
    public boolean checkParamsValidate() {
        return true;
    }
}
