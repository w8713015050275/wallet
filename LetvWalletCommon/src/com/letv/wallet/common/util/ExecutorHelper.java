package com.letv.wallet.common.util;

/**
 * Created by changjiajie on 17-1-25.
 */

public class ExecutorHelper {

    public static WalletExecutor getExecutor() {
        return WalletExecutorProxy.getExecutorInstance();
    }
}
