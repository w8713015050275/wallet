package com.letv.wallet.account.aidl.v1;

import com.letv.wallet.account.aidl.v1.IAccountCallback;

interface IAccountServiceV1 {
    oneway void createAccount(in IAccountCallback callback);

    oneway void queryAccount(String qType, in IAccountCallback callback);

    oneway void verifyAccount(String accountName, String identityNum, String bankNo,
                        String mobile, String msgCode, in IAccountCallback callback);

    oneway void redirect(in String[] jType, in IAccountCallback callback);

    oneway void availableBank(String jType, in IAccountCallback callback);

    oneway void cardbin(String bankNo, in IAccountCallback callback);

    oneway void sendMsg(String mobile, String template , in IAccountCallback callback);
}