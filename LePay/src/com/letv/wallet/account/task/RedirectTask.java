package com.letv.wallet.account.task;

import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.common.http.beans.BaseResponse;

/**
 * Created by lijunying on 16-12-26.
 */

public class RedirectTask extends AccountBaseTask {
    private String[] mTypes ;
    private StringBuilder mUrlsTypes ; // 请求多个URL时，以 ，分割

    private void initParams(String[] mTypes) {
        this.mTypes = mTypes;
    }

    public RedirectTask(String[] mTypes, AccountCommonCallback<RedirectURL> callback) {
        super(callback);
        initParams(mTypes);
    }

    public RedirectTask(String[] types, IAccountCallback callback) {
        super(callback);
        initParams(types);
    }

    @Override
    public boolean checkParamsValidate() {
        if (mTypes == null || mTypes.length == 0) {
            return false;
        }
        return true;
    }

    @Override
    public BaseResponse onExecute() {

        mUrlsTypes = new StringBuilder();
        for(String type : mTypes) {
            mUrlsTypes.append(type).append(",");
        }
        mUrlsTypes.deleteCharAt(mUrlsTypes.length() - 1);

        return AccountGateway.redirect(mUrlsTypes.toString());
    }

}
