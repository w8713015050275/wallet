package com.letv.wallet.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by changjiajie on 17-1-25.
 */

public class WalletExecutorImp implements WalletExecutor {

    private static final String TAG = WalletExecutorImp.class.getSimpleName();
    private static WalletExecutorImp instance;
    private static int KEEPALIVETIME = 20;

    private static ThreadPoolExecutor mExecutors;

    static class ActionThreadFactory implements ThreadFactory {
        private static final String NAME = "WALLET_THEAD #";
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, NAME + mCount.getAndIncrement());
            return thread;
        }
    }

    private WalletExecutorImp() {
        if (mExecutors == null) {
            mExecutors = (ThreadPoolExecutor) Executors.newCachedThreadPool(new ActionThreadFactory());
            if (mExecutors != null) {
                mExecutors.allowCoreThreadTimeOut(true);
                mExecutors.setKeepAliveTime(KEEPALIVETIME, TimeUnit.SECONDS);
                mExecutors.setThreadFactory(new ActionThreadFactory());
                mExecutors.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            }
        }
    }

    static {
        instance = new WalletExecutorImp();
    }

    public static WalletExecutorImp getInstance() {
        if (instance == null) {
            instance = new WalletExecutorImp();
        }
        return instance;
    }

    @Override
    public void runnableExecutor(Runnable task) {
        if (mExecutors == null) {
            LogHelper.e("[%S] [executor(Runnable task) mExecutors == null]", TAG);
            return;
        }
        mExecutors.execute(task);
    }

    @Override
    public void clearRunnable(Runnable task) {
        if (mExecutors == null) {
            LogHelper.e("[%S] [clearRunnable(Runnable task) mExecutors == null]", TAG);
            return;
        }
        mExecutors.remove(task);
    }

    @Override
    public void clearAllRunnable() {
        if (mExecutors == null) {
            LogHelper.e("[%S] [clearAllRunnable() mExecutors == null]", TAG);
            return;
        }
        mExecutors.shutdownNow();
        mExecutors = null;
    }

}
