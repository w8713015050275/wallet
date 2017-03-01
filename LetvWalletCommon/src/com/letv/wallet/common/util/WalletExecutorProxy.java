package com.letv.wallet.common.util;

/**
 * Created by changjiajie on 17-1-25.
 */

public class WalletExecutorProxy implements WalletExecutor {


    private static WalletExecutorProxy instance;

    private WalletExecutorProxy(WalletExecutor executor) {
        mExecutor = executor;
    }

    public static WalletExecutorProxy getExecutorInstance() {
        if (instance == null) {
            synchronized (WalletExecutorProxy.class) {
                if (instance == null) {
                    instance = new WalletExecutorProxy(WalletExecutorImp.getInstance());
                }
            }
        }
        return instance;
    }

    private WalletExecutor mExecutor;

    @Override
    public void runnableExecutor(Runnable task) {
        mExecutor.runnableExecutor(task);
    }

    @Override
    public void clearRunnable(Runnable task) {
        mExecutor.clearRunnable(task);
    }

    @Override
    public void clearAllRunnable() {
        instance = null;
        mExecutor.clearAllRunnable();
    }
}
