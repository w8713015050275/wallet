package com.letv.wallet.account.task;

/**
 * Created by lijunying on 17-1-3.
 */

public interface AccountCommonCallback<T> {
    void onSuccess(T result);
    void onError(int errorCode, String errorMsg);
    void onNoNet();
}
