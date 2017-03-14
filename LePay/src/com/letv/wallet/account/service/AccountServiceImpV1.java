package com.letv.wallet.account.service;

import android.os.Bundle;
import android.os.RemoteException;

import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.aidl.v1.IAccountServiceV1;
import com.letv.wallet.account.task.AccountCreateTask;
import com.letv.wallet.account.task.AccountQueryTask;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.common.util.ExecutorHelper;

import java.util.ArrayList;

/**
 * Created by lijunying on 17-1-17.
 */

public class AccountServiceImpV1 extends IAccountServiceV1.Stub {

    private final Object createHashLock = new Object();
    private AccountCreateTask createTask = null;
    private final ArrayList<IAccountCallback> callbacks = new ArrayList<>();

    @Override
    public void createAccount(final IAccountCallback callback) throws RemoteException {
        if (callback != null) {
            synchronized (callbacks) {
                if (!callbacks.contains(callback)) {
                    callbacks.add(callback);
                    callback.asBinder().linkToDeath(new DeathRecipient() {
                        @Override
                        public void binderDied() {
                            callbacks.remove(callback);
                        }
                    }, 0);
                }
            }
        }

        if (createTask == null) {
            synchronized (createHashLock) {
                if (createTask == null) {
                    ExecutorHelper.getExecutor().runnableExecutor(createTask = new AccountCreateTask(new AccountCreateTask.AccountCreateRemoteCallBack() {
                        @Override
                        public void onSuccess(Bundle bundle) {
                            synchronized (createHashLock) {
                                createTask = null;

                            }
                            synchronized (callbacks) {
                                for (IAccountCallback listener : callbacks) {
                                    try {
                                        listener.onSuccess(bundle);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                                callbacks.clear();
                            }

                        }

                        @Override
                        public void onFailure(int errorCode, String errorMsg) {
                            synchronized (createHashLock) {
                                createTask = null;

                            }
                            synchronized (callbacks) {
                                for (IAccountCallback listener : callbacks) {
                                    try {
                                        listener.onFailure(errorCode, errorMsg);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                                callbacks.clear();
                            }
                        }
                    }));
                }
            }
        }

    }

    @Override
    public void queryAccount(String qType, IAccountCallback callback) throws RemoteException {
        ExecutorHelper.getExecutor().runnableExecutor((new AccountQueryTask(qType, callback)));
    }

    @Override
    public void redirect(String[] jType, IAccountCallback callback) throws RemoteException {
        ExecutorHelper.getExecutor().runnableExecutor(new RedirectTask(jType, callback));
    }

}
