package com.letv.wallet.account.task;

import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.CardbinAvailableInfo;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.common.http.beans.BaseResponse;

/**
 * Created by lijunying on 16-12-26.
 */

public class CardbinTask extends AccountBaseTask {

    private String bankNo;

    private void initParams(String bankNo) {
        this.bankNo = bankNo;
    }

    public CardbinTask(String bankNo, AccountCommonCallback<CardbinAvailableInfo> callback) {
        super(callback);
        initParams(bankNo);
    }

    public CardbinTask(String bankNo, IAccountCallback callback) {
        super(callback);
        initParams(bankNo);
    }


    @Override
    public BaseResponse onExecute() {
        return AccountGateway.availableCarbin(bankNo);
    }

    @Override
    public boolean checkParamsValidate() {
        if (TextUtils.isEmpty(bankNo)) {
            return false;
        }
        return true;
    }
}
