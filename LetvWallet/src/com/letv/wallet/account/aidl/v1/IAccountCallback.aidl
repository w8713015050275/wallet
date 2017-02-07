package com.letv.wallet.account.aidl.v1;

import android.os.Bundle;

interface IAccountCallback {
   oneway void onSuccess(in Bundle bundle);
   oneway void onFailure(int errorCode, String errorMsg);
}