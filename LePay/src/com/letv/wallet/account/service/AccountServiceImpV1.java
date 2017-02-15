package com.letv.wallet.account.service;

import android.os.RemoteException;

import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.aidl.v1.IAccountServiceV1;
import com.letv.wallet.account.task.AccountCreateTask;
import com.letv.wallet.account.task.AccountQueryTask;
import com.letv.wallet.account.task.AccountVerifyTask;
import com.letv.wallet.account.task.CardbinTask;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.common.util.ExecutorHelper;

/**
 * Created by lijunying on 17-1-17.
 */

public class AccountServiceImpV1 extends IAccountServiceV1.Stub {
    @Override
    public void createAccount(IAccountCallback callback) throws RemoteException {
        ExecutorHelper.getExecutor().runnableExecutor(new AccountCreateTask(callback));
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
