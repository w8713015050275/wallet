package com.letv.wallet.account.service;

import android.os.RemoteException;

import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.aidl.v1.IAccountServiceV1;
import com.letv.wallet.account.task.AccountCreateTask;
import com.letv.wallet.account.task.AccountQueryTask;
import com.letv.wallet.account.task.AccountVerifyTask;
import com.letv.wallet.account.task.BankAvailableTask;
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
    public void verifyAccount(String accountName, String identityNum, String bankNo, String mobile, String msgCode, IAccountCallback callback) throws RemoteException {
        ExecutorHelper.getExecutor().runnableExecutor(new AccountVerifyTask(accountName, identityNum, bankNo, mobile, msgCode, callback));
    }

    @Override
    public void redirect(String[] jType, IAccountCallback callback) throws RemoteException {
        ExecutorHelper.getExecutor().runnableExecutor(new RedirectTask(jType, callback));
    }

    @Override
    public void availableBank(String jType, IAccountCallback callback) throws RemoteException {
        ExecutorHelper.getExecutor().runnableExecutor(new BankAvailableTask(callback));
    }

    @Override
    public void cardbin(String bankNo, IAccountCallback callback) throws RemoteException {
        ExecutorHelper.getExecutor().runnableExecutor(new CardbinTask(bankNo, callback));
    }

    @Override
    public void sendMsg(String mobile, String template, IAccountCallback callback) throws RemoteException {

    }

}
