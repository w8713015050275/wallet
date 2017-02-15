package com.letv.wallet.account.aidl.v1;

import com.letv.wallet.account.aidl.v1.IAccountCallback;

interface IAccountServiceV1 {
    oneway void createAccount(in IAccountCallback callback);

    oneway void queryAccount(String qType, in IAccountCallback callback);

    oneway void redirect(in String[] jType, in IAccountCallback callback);

}