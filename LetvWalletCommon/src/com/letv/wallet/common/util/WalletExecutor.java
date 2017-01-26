package com.letv.wallet.common.util;

/**
 * Created by changjiajie on 17-1-25.
 */

public interface WalletExecutor {

    void runnableExecutor(Runnable task);

    void clearRunnable(Runnable task);

    void clearAllRunnable();

}
